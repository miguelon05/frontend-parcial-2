package pe.upeu.agrosierra.domain.usecase
import kotlinx.datetime.LocalDate
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.repository.AgroRepository
class RegistrarLaborUseCase(private val repo: AgroRepository) {
    suspend operator fun invoke(labor: Labor): Labor {
        val errores=mutableMapOf<String,String>()
        if(labor.cantidad<=0 || !labor.cantidad.isFinite()) errores["cantidad"]="Ingresa una cantidad mayor que cero"
        if(runCatching { LocalDate.parse(labor.fecha) }.isFailure) errores["fecha"]="Usa una fecha válida AAAA-MM-DD"
        if(labor.observacion.length>500) errores["observacion"]="Máximo 500 caracteres"
        if(errores.isNotEmpty()) throw ErrorApi.Validacion(errores)
        return repo.guardar(labor)
    }
}
