package pe.upeu.agrosierra.domain.repository
import kotlinx.coroutines.flow.Flow
import pe.upeu.agrosierra.domain.model.*
data class ResumenSync(val enviados: Int,val recibidos: Int,val rechazados: Int=0)
interface AgroRepository {
    fun observarLotes(valle: String="",busqueda: String=""): Flow<List<Lote>>
    fun observarLabores(loteId: Long): Flow<List<Labor>>
    fun ultimaSincronizacion(): Flow<String?>
    suspend fun guardar(labor: Labor): Labor
    suspend fun eliminar(labor: Labor)
    suspend fun cerrarCampania(id: Long)
    suspend fun sincronizar(): ResumenSync
    fun errorPendiente(id: Long): ErrorApi?
}
