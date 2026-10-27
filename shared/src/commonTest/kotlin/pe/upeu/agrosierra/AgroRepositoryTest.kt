package pe.upeu.agrosierra
import app.cash.sqldelight.db.SqlDriver
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.client.request.*
import io.ktor.client.call.body
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlinx.serialization.json.Json
import pe.upeu.agrosierra.db.AgroDb
import pe.upeu.agrosierra.data.local.*
import pe.upeu.agrosierra.data.remote.*
import pe.upeu.agrosierra.data.remote.dto.*
import pe.upeu.agrosierra.data.repository.*
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.usecase.*

expect fun driverEnMemoria(): SqlDriver
class SeguroPrueba: AlmacenSeguro {
    override val sesion=MutableStateFlow<Sesion?>(Sesion("token","tecnico1","TECNICO",setOf("LOTE_LEER","LABOR_LEER","LABOR_CREAR","LABOR_EDITAR"),expiraEn=4102444800))
    override fun guardar(sesion: Sesion) { this.sesion.value=sesion }
    override fun borrar() { sesion.value=null }
}
class AgroRepositoryTest {
    private val lote=Lote(1,"AS-001","Los Andenes",3.0,"Papa","Mantaro",false,2.5)
    private val nueva=Labor(0,1,TipoLabor.RIEGO,"2026-10-27","tecnico1",2.0,"Prueba",true)
    private val cabeceras=headersOf(HttpHeaders.ContentType,"application/json")
    private fun pagina(contenido: String,pagina: Int=0,ultima: Boolean=true)="""{"contenido":$contenido,"pagina":$pagina,"tamanio":100,"totalElementos":1,"totalPaginas":1,"ultima":$ultima}"""
    private val loteJson="""{"id":1,"codigo":"AS-001","nombre":"Los Andenes","hectareas":3.0,"cultivo":"Papa","valle":"Mantaro","campaniaCerrada":false,"dosisMaximaPorHectarea":2.5}"""
    private val laborJson="""{"id":10,"loteId":1,"tipo":"RIEGO","fecha":"2026-10-27","responsable":"tecnico1","cantidad":2.0,"observacion":"Prueba"}"""

    @Test fun convierteDtoSinAcoplarDominioASerializacion() {
        val dto=Json.decodeFromString<LoteDto>(loteJson)
        assertEquals(lote,dto.dominio())
        assertFalse(Json.decodeFromString<LaborDto>(laborJson).dominio().pendiente)
    }
    @Test fun distingueValidacion401y403() {
        val validacion=mapearError(400,ErrorDto("Cantidad inválida",mapOf("cantidad" to "Supera la dosis"))) as ErrorApi.Validacion
        assertEquals("Supera la dosis",validacion.campos["cantidad"])
        assertIs<ErrorApi.NoAutorizado>(mapearError(401,ErrorDto()))
        assertIs<ErrorApi.SinPrivilegio>(mapearError(403,ErrorDto()))
    }
    @Test fun registroSinRedPersisteYSeObservaConIdNegativo()=runTest {
        val driver=driverEnMemoria(); val local=BaseLocal(AgroDb(driver));local.guardar(lote)
        val seguro=SeguroPrueba();var peticiones=0
        val cliente=crearCliente(MockEngine {peticiones++;error("Sin red")},ApiConfig(),seguro)
        try {
            val repo=AgroRepositoryOffline(local,cliente,seguro)
            val guardada=RegistrarLaborUseCase(repo)(nueva)
            assertTrue(guardada.id<0);assertTrue(guardada.pendiente)
            assertEquals(guardada.id,repo.observarLabores(1).first().single().id)
            assertEquals(0,peticiones)
        } finally {cliente.close();driver.close()}
    }
    @Test fun sincronizaEnvioAntesDeDescargaYSustituyeLaFilaEnTransaccion()=runTest {
        val driver=driverEnMemoria();val local=BaseLocal(AgroDb(driver));local.guardar(lote)
        val orden=mutableListOf<String>();val seguro=SeguroPrueba()
        val cliente=crearCliente(MockEngine {r ->
            orden.add("${r.method.value} ${r.url.encodedPath}")
            when(r.url.encodedPath) {
                "/api/v1/labores" -> respond(laborJson,HttpStatusCode.Created,cabeceras)
                "/api/v1/lotes" -> respond(pagina("[$loteJson]"),headers=cabeceras)
                else -> respond(pagina("[$laborJson]"),headers=cabeceras)
            }
        },ApiConfig(),seguro)
        try {
            val repo=AgroRepositoryOffline(local,cliente,seguro);val provisional=repo.guardar(nueva)
            val resumen=repo.sincronizar()
            assertEquals("POST /api/v1/labores",orden.first());assertEquals(1,resumen.enviados)
            assertNull(local.q.obtenerLabor(provisional.id).executeAsOneOrNull())
            val fila=repo.observarLabores(1).first().single();assertEquals(10,fila.id);assertFalse(fila.pendiente)
            repo.sincronizar();assertEquals(1,orden.count {it.startsWith("POST")})
        } finally {cliente.close();driver.close()}
    }
    @Test fun respuesta400ConservaFilaYMensajePorCampo()=runTest {
        val driver=driverEnMemoria();val local=BaseLocal(AgroDb(driver));local.guardar(lote);val seguro=SeguroPrueba()
        val cliente=crearCliente(MockEngine {r -> when(r.method) {
            HttpMethod.Post -> respond("""{"message":"Dosis máxima","validationErrors":{"cantidad":"Máximo 7.5"}}""",HttpStatusCode.BadRequest,cabeceras)
            else -> respond(pagina(if(r.url.encodedPath.endsWith("/lotes")) "[$loteJson]" else "[]"),headers=cabeceras)
        } },ApiConfig(),seguro)
        try {
            val repo=AgroRepositoryOffline(local,cliente,seguro);val p=repo.guardar(nueva)
            val resumen=repo.sincronizar();assertEquals(1,resumen.rechazados)
            assertEquals("Máximo 7.5",(repo.errorPendiente(p.id) as ErrorApi.Validacion).campos["cantidad"])
            assertTrue(repo.observarLabores(1).first().single().pendiente)
        } finally {cliente.close();driver.close()}
    }
    @Test fun token401LimpiaSesionY403LaMantiene()=runTest {
        for(status in listOf(HttpStatusCode.Forbidden,HttpStatusCode.Unauthorized)) {
            val seguro=SeguroPrueba()
            val cliente=crearCliente(MockEngine { respond("""{"message":"Rechazado"}""",status,cabeceras) },ApiConfig(),seguro)
            try { runCatching { cliente.get("lotes") };assertEquals(status==HttpStatusCode.Unauthorized,seguro.sesion.value==null) }
            finally {cliente.close()}
        }
    }
    @Test fun paginaFallidaNoBorraCacheLocal()=runTest {
        val driver=driverEnMemoria();val local=BaseLocal(AgroDb(driver));local.guardar(lote);val seguro=SeguroPrueba()
        val cliente=crearCliente(MockEngine {r -> if(r.url.encodedPath.endsWith("/lotes")) respond(pagina("[$loteJson]"),headers=cabeceras) else respond("{}",HttpStatusCode.InternalServerError,cabeceras) },ApiConfig(),seguro)
        try { val repo=AgroRepositoryOffline(local,cliente,seguro);assertTrue(runCatching {repo.sincronizar()}.isFailure);assertEquals(lote,repo.observarLotes().first().single());assertNull(local.q.leerMetadata("ultimaSync").executeAsOneOrNull()) }
        finally {cliente.close();driver.close()}
    }
    @Test fun noEnviaCambiosDeOtraCuenta()=runTest {
        val driver=driverEnMemoria();val local=BaseLocal(AgroDb(driver));local.guardar(lote);val seguro=SeguroPrueba();var envios=0
        val cliente=crearCliente(MockEngine {r -> if(r.method==HttpMethod.Post) envios++;respond(pagina(if(r.url.encodedPath.endsWith("/lotes")) "[$loteJson]" else "[]"),headers=cabeceras) },ApiConfig(),seguro)
        try {
            val repo=AgroRepositoryOffline(local,cliente,seguro);repo.guardar(nueva)
            seguro.guardar(seguro.sesion.value!!.copy(usuario="jefe1"));repo.sincronizar()
            assertEquals(0,envios);assertEquals(1,local.q.pendientes("tecnico1").executeAsList().size)
        } finally {cliente.close();driver.close()}
    }
    @Test fun validaCantidadYFechaAntesDePersistir()=runTest {
        val driver=driverEnMemoria();val local=BaseLocal(AgroDb(driver));val seguro=SeguroPrueba()
        val cliente=crearCliente(MockEngine {error("No debe consultar red")},ApiConfig(),seguro)
        try {
            val repo=AgroRepositoryOffline(local,cliente,seguro)
            val error=runCatching { RegistrarLaborUseCase(repo)(nueva.copy(cantidad=0.0,fecha="2026-02-30")) }.exceptionOrNull()
            assertIs<ErrorApi.Validacion>(error);assertEquals(setOf("cantidad","fecha"),error.campos.keys)
            assertTrue(local.q.pendientes("tecnico1").executeAsList().isEmpty())
        } finally {cliente.close();driver.close()}
    }
}
