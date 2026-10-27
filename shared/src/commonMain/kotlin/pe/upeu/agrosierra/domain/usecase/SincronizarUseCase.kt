package pe.upeu.agrosierra.domain.usecase
import pe.upeu.agrosierra.domain.repository.AgroRepository
class SincronizarUseCase(private val repo: AgroRepository) { suspend operator fun invoke()=repo.sincronizar() }
