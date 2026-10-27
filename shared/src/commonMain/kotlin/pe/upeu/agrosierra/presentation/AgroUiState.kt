package pe.upeu.agrosierra.presentation
import pe.upeu.agrosierra.domain.model.*
data class AgroUiState(
    val sesion: Sesion?=null,val iniciando: Boolean=true,val cargando: Boolean=false,
    val sincronizando: Boolean=false,val lotes: List<Lote> = emptyList(),val labores: List<Labor> = emptyList(),
    val lote: Lote?=null,val valle: String="",val busqueda: String="",val valles: List<String> = emptyList(),
    val ultimaSync: String?=null,val conectado: Boolean=false,val mensaje: String?=null,
    val error: ErrorApi?=null,val campos: Map<String,String> = emptyMap(),val guardado: Boolean=false,val laborGuardada: Labor?=null
)
