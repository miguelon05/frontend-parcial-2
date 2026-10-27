package pe.upeu.agrosierra.data.remote.dto
import kotlinx.serialization.Serializable
import pe.upeu.agrosierra.domain.model.*
@Serializable data class PaginaDto<T>(val contenido: List<T>,val pagina: Int,val tamanio: Int,val totalElementos: Long,val totalPaginas: Int,val ultima: Boolean)
@Serializable data class ErrorDto(val message: String="Error del servicio",val validationErrors: Map<String,String> = emptyMap())
@Serializable data class LoginDto(val usuario: String,val contrasena: String)
@Serializable data class SesionDto(val token: String,val usuario: String,val rol: String,val privilegios: Set<String>,val nombre: String="",val expiraEn: Long=0,val editaTodas: Boolean=false) {
    fun dominio()=Sesion(token,usuario,rol,privilegios,nombre,expiraEn,editaTodas)
}
@Serializable data class LoteDto(val id: Long,val codigo: String,val nombre: String,val hectareas: Double,val cultivo: String,val valle: String,val campaniaCerrada: Boolean,val dosisMaximaPorHectarea: Double) {
    fun dominio()=Lote(id,codigo,nombre,hectareas,cultivo,valle,campaniaCerrada,dosisMaximaPorHectarea)
}
@Serializable data class LaborDto(val id: Long,val loteId: Long,val tipo: String,val fecha: String,val responsable: String,val cantidad: Double,val observacion: String,val creadoEn: String="") {
    fun dominio()=Labor(id,loteId,TipoLabor.valueOf(tipo),fecha,responsable,cantidad,observacion,false,creadoEn)
}
@Serializable data class LaborPeticionDto(val loteId: Long,val tipo: String,val fecha: String,val cantidad: Double,val observacion: String,val claveCliente: String?=null)
@Serializable data class UsuarioDto(val usuario: String,val rol: String,val privilegios: Set<String>,val nombre: String="",val editaTodas: Boolean=false)
