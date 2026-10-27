package pe.upeu.agrosierra.data.local
import kotlinx.coroutines.flow.StateFlow
import pe.upeu.agrosierra.domain.model.Sesion
interface AlmacenSeguro {
    val sesion: StateFlow<Sesion?>
    fun guardar(sesion: Sesion)
    fun borrar()
}
