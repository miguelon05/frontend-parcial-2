package pe.upeu.agrosierra.domain.model
data class Sesion(val token: String,val usuario: String,val rol: String,val privilegios: Set<String>,val nombre: String="",val expiraEn: Long=0,val editaTodas: Boolean=false)
