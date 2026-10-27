package pe.upeu.agrosierra.data.repository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import pe.upeu.agrosierra.data.local.*
import pe.upeu.agrosierra.data.remote.dto.*
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.repository.*
import kotlin.time.Clock
import kotlin.random.Random
class AgroRepositoryOffline(private val local: BaseLocal,private val http: HttpClient,private val seguro: AlmacenSeguro): AgroRepository {
    private val mutex=Mutex()
    private fun sesion()=seguro.sesion.value ?: throw ErrorApi.NoAutorizado()
    override fun observarLotes(valle: String,busqueda: String)=local.lotes(valle,busqueda)
    override fun observarLabores(loteId: Long)=local.labores(loteId,sesion().usuario)
    override fun ultimaSincronizacion()=local.ultimaSync()
    override fun errorPendiente(id: Long)=local.q.obtenerLabor(id).executeAsOneOrNull()?.error.orEmpty()
    override suspend fun guardar(labor: Labor): Labor=withContext(Dispatchers.Default) { mutex.withLock {
        val s=sesion()
        val anterior=local.q.obtenerLabor(labor.id).executeAsOneOrNull()
        if(anterior?.pendiente==1L && anterior.propietario!=s.usuario) throw ErrorApi.SinPrivilegio("Hay cambios de otro usuario en esta labor")
        val nuevo=if(labor.id==0L) labor.copy(id=minOf(-1L,local.q.siguienteIdLocal().executeAsOne()-1L),responsable=s.usuario,creadoEn=Clock.System.now().toString(),pendiente=true) else labor.copy(pendiente=true)
        val op=if(nuevo.id<0) "POST" else "PUT"
        local.guardar(nuevo,op,s.usuario,anterior?.claveCliente?.takeIf { it.isNotEmpty() } ?: (1..32).map { "0123456789abcdef"[Random.nextInt(16)] }.joinToString(""))
        nuevo
    } }
    override suspend fun eliminar(labor: Labor)=withContext(Dispatchers.Default) { mutex.withLock {
        val s=sesion()
        val anterior=local.q.obtenerLabor(labor.id).executeAsOneOrNull()
        if(anterior?.pendiente==1L && anterior.propietario!=s.usuario) throw ErrorApi.SinPrivilegio()
        if(labor.id<0) local.q.borrarLabor(labor.id) else local.guardar(labor.copy(pendiente=true),"DELETE",s.usuario)
    } }
    override suspend fun cerrarCampania(id: Long) {
        mutex.withLock {
            val pendientes=local.q.pendientes(sesion().usuario).executeAsList()
            if(pendientes.any { it.loteId==id }) throw ErrorApi.Conflicto("Sincroniza las labores pendientes de este lote antes de cerrar")
            val lote=http.post("lotes/$id/cerrar-campania").body<LoteDto>().dominio()
            withContext(Dispatchers.Default) { local.guardar(lote) }
        }
    }
    private suspend inline fun <reified T> paginas(ruta: String): List<T> {
        val resultado=mutableListOf<T>();var pagina=0
        do {
            val p=http.get(ruta) { parameter("pagina",pagina);parameter("tamanio",100) }.body<PaginaDto<T>>()
            resultado.addAll(p.contenido);pagina++
            if(p.ultima) break
        } while(true)
        return resultado
    }
    override suspend fun sincronizar(): ResumenSync=withContext(Dispatchers.Default) { mutex.withLock {
        val s=sesion();var enviados=0;var rechazados=0
        // La cola se procesa antes de iniciar cualquier descarga.
        for(p in local.q.pendientes(s.usuario).executeAsList()) {
            try {
                val peticion=LaborPeticionDto(p.loteId,p.tipo,p.fecha,p.cantidad,p.observacion,p.claveCliente)
                when(p.operacion) {
                    "POST","PUT" -> {
                        val respuesta=(if(p.operacion=="POST") http.post("labores") {setBody(peticion)} else http.put("labores/${p.id}") {setBody(peticion)}).body<LaborDto>().dominio()
                        local.db.transaction { local.q.borrarLabor(p.id);local.guardar(respuesta) }
                    }
                    "DELETE" -> {
                        try { http.delete("labores/${p.id}") } catch(_: ErrorApi.NoEncontrado) { /* Ya eliminado: operación idempotente. */ }
                        local.q.borrarLabor(p.id)
                    }
                }
                enviados++
            } catch(e: ErrorApi) {
                if(e is ErrorApi.NoAutorizado || e is ErrorApi.SinConexion || e is ErrorApi.TiempoAgotado || e is ErrorApi.Servidor) throw e
                if(e is ErrorApi.NoEncontrado) { local.q.borrarLabor(p.id) }
                else { local.guardar(p.dominio(),p.operacion,p.propietario,p.claveCliente,e.message);rechazados++ }
            }
        }
        val lotes=paginas<LoteDto>("lotes")
        val labores=lotes.flatMap { paginas<LaborDto>("lotes/${it.id}/labores") }
        // No borrar caché hasta que TODAS las páginas hayan llegado.
        local.db.transaction {
            local.q.borrarLotes();lotes.forEach { local.guardar(it.dominio()) }
            local.q.borrarSincronizadas()
            labores.forEach { dto ->
                if(local.q.obtenerLabor(dto.id).executeAsOneOrNull()?.pendiente!=1L) local.guardar(dto.dominio())
            }
            local.q.guardarMetadata("ultimaSync",Clock.System.now().toString())
        }
        ResumenSync(enviados,lotes.size+labores.size,rechazados)
    } }
}
