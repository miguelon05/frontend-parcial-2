package pe.upeu.agrosierra.domain.model
sealed class ErrorApi(override val message: String): Exception(message) {
    class Validacion(val campos: Map<String,String>,mensaje: String="Revisa los campos del formulario"): ErrorApi(mensaje)
    class NoAutorizado(mensaje: String="Tu sesión ha vencido. Vuelve a ingresar"): ErrorApi(mensaje)
    class SinPrivilegio(mensaje: String="No tienes permiso para esta operación"): ErrorApi(mensaje)
    class NoEncontrado(mensaje: String="El recurso ya no existe"): ErrorApi(mensaje)
    class Conflicto(mensaje: String): ErrorApi(mensaje)
    class Servidor(mensaje: String="El servicio no está disponible. Intenta nuevamente"): ErrorApi(mensaje)
    class SinConexion: ErrorApi("Sin conexión. Los cambios quedan guardados en este dispositivo")
    class TiempoAgotado: ErrorApi("El servidor tarda en responder. Intenta nuevamente")
}
