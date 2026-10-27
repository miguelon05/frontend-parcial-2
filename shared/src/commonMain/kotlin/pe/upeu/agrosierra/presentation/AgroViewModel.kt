package pe.upeu.agrosierra.presentation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.upeu.agrosierra.data.repository.SesionRepository
import pe.upeu.agrosierra.data.local.MonitorRed
import pe.upeu.agrosierra.domain.repository.AgroRepository
import pe.upeu.agrosierra.domain.usecase.*
import pe.upeu.agrosierra.domain.model.*
import kotlin.time.Clock
class AgroViewModel(private val sesiones: SesionRepository,private val repo: AgroRepository,private val registrar: RegistrarLaborUseCase,private val sincronizar: SincronizarUseCase,private val red: MonitorRed): ViewModel() {
    private val mutable=MutableStateFlow(AgroUiState())
    val estado=mutable.asStateFlow()
    private var listado: Job?=null
    private var detalle: Job?=null
    private var expiracion: Job?=null
    private var valles: Job?=null
    init {
        viewModelScope.launch {
            sesiones.sesion.collect { s ->
                listado?.cancel();detalle?.cancel();expiracion?.cancel();valles?.cancel()
                mutable.value=AgroUiState(sesion=s,iniciando=false,conectado=red.conectado.value)
                if(s!=null) {
                    observar();valles=viewModelScope.launch { repo.observarLotes().collect { rows -> mutable.update { it.copy(valles=rows.map { l -> l.valle }.distinct().sorted()) } } }
                    expiracion=viewModelScope.launch { delay(((s.expiraEn-Clock.System.now().epochSeconds)*1000).coerceAtLeast(1));sesiones.salir() }
                    sync()
                }
            }
        }
        viewModelScope.launch { repo.ultimaSincronizacion().collect { fecha -> mutable.update { it.copy(ultimaSync=fecha) } } }
        viewModelScope.launch { red.conectado.collect { conectado -> mutable.update { it.copy(conectado=conectado) };if(conectado && estado.value.sesion!=null) sync() } }
    }
    private fun observar() {
        listado?.cancel();listado=viewModelScope.launch { repo.observarLotes(estado.value.valle,estado.value.busqueda).collect { rows -> mutable.update { st -> st.copy(lotes=rows,lote=st.lote?.let { actual -> rows.find { it.id==actual.id } ?: actual }) } } }
    }
    fun filtrar(valle: String=estado.value.valle,busqueda: String=estado.value.busqueda) { mutable.update { it.copy(valle=valle,busqueda=busqueda) };observar() }
    fun detalle(lote: Lote?) {
        detalle?.cancel();mutable.update { it.copy(lote=lote,labores=emptyList(),campos=emptyMap(),error=null) }
        if(lote!=null) detalle=viewModelScope.launch { repo.observarLabores(lote.id).collect { rows -> mutable.update { it.copy(labores=rows) } } }
    }
    fun ingresar(usuario: String,contrasena: String) {
        if(usuario.isBlank() || contrasena.isBlank()) { mutable.update { it.copy(campos=mapOf("login" to "Ingresa tu usuario y contraseña")) };return }
        trabajo { sesiones.ingresar(usuario.trim(),contrasena) }
    }
    fun salir() { sesiones.salir() }
    fun limpiar() { mutable.update { it.copy(mensaje=null,error=null,campos=emptyMap(),guardado=false,laborGuardada=null) } }
    private fun error(e: Throwable) {
        if(e is CancellationException) throw e
        val api=e as? ErrorApi ?: ErrorApi.Servidor()
        mutable.update { it.copy(error=api,campos=(api as? ErrorApi.Validacion)?.campos ?: emptyMap(),mensaje=null) }
        if(api is ErrorApi.NoEncontrado) sync()
    }
    private fun trabajo(bloque: suspend () -> Unit) {
        if(estado.value.cargando) return
        viewModelScope.launch {
            mutable.update { it.copy(cargando=true,error=null,campos=emptyMap(),mensaje=null,guardado=false) }
            try { bloque() } catch(e: Exception) { error(e) } finally { mutable.update { it.copy(cargando=false) } }
        }
    }
    fun sync() {
        if(estado.value.sincronizando || estado.value.sesion==null) return
        mutable.update { it.copy(sincronizando=true,error=null,mensaje=null) }
        viewModelScope.launch {
            try { val r=sincronizar();mutable.update { it.copy(mensaje="Sincronización: ${r.enviados} enviados, ${r.recibidos} recibidos"+(if(r.rechazados>0) ". ${r.rechazados} requieren corrección" else "")) } }
            catch(e: Exception) { error(e) }
            finally { mutable.update { it.copy(sincronizando=false) } }
        }
    }
    fun guardar(l: Labor) = trabajo {
        val guardada=registrar(l)
        mutable.update { it.copy(laborGuardada=guardada) }
        if(!red.conectado.value) { mutable.update { it.copy(guardado=true,mensaje="Labor guardada en el dispositivo · Pendiente de sincronizar") };return@trabajo }
        try {
            val r=sincronizar()
            val motivo=repo.errorPendiente(guardada.id)
            if(motivo!=null) { mutable.update { it.copy(error=motivo,campos=(motivo as? ErrorApi.Validacion)?.campos ?: emptyMap(),guardado=false) } }
            else mutable.update { it.copy(guardado=true,mensaje="Labor guardada y sincronizada. ${r.enviados} enviados") }
        } catch(e: ErrorApi.SinConexion) { mutable.update { it.copy(guardado=true,mensaje="Labor guardada en el dispositivo · Pendiente de sincronizar") } }
          catch(e: ErrorApi.TiempoAgotado) { mutable.update { it.copy(guardado=true,mensaje="Labor guardada · Se enviará al recuperar la conexión") } }
    }
    fun eliminar(l: Labor)=trabajo {
        repo.eliminar(l)
        try { sincronizar();mutable.update { it.copy(mensaje="Eliminación sincronizada") } }
        catch(e: ErrorApi.SinConexion) { mutable.update { it.copy(mensaje="Eliminación pendiente de sincronizar") } }
        catch(e: ErrorApi.TiempoAgotado) { mutable.update { it.copy(mensaje="Eliminación guardada; sincroniza más tarde") } }
    }
    fun cerrar()=trabajo { repo.cerrarCampania(estado.value.lote!!.id);mutable.update { it.copy(lote=it.lote?.copy(campaniaCerrada=true),mensaje="Campaña cerrada") } }
}
