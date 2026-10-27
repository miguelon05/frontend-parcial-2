package pe.upeu.agrosierra.data.repository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import pe.upeu.agrosierra.data.local.AlmacenSeguro
import pe.upeu.agrosierra.data.remote.dto.*
import pe.upeu.agrosierra.domain.model.Sesion
class SesionRepository(private val http: HttpClient,private val seguro: AlmacenSeguro) {
    val sesion=seguro.sesion
    suspend fun ingresar(usuario: String,contrasena: String): Sesion {
        val s=http.post("auth/login") { setBody(LoginDto(usuario,contrasena)) }.body<SesionDto>().dominio()
        seguro.guardar(s);return s
    }
    suspend fun perfil(): UsuarioDto=http.get("usuarios/me").body()
    fun salir()=seguro.borrar()
}
