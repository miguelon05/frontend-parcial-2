package pe.upeu.agrosierra.data.local
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import pe.upeu.agrosierra.db.AgroDb
import pe.upeu.agrosierra.domain.model.*
class BaseLocal(val db: AgroDb) {
    val q=db.agroDbQueries
    fun lotes(valle: String,busqueda: String)=q.observarLotes(valle,busqueda).asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.dominio() } }
    fun labores(id: Long,usuario: String)=q.observarLaboresPorLote(id,usuario).asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { it.dominio() } }
    fun ultimaSync()=q.leerMetadata("ultimaSync").asFlow().mapToOneOrNull(Dispatchers.Default)
    fun guardar(l: Lote)=q.guardarLote(l.id,l.codigo,l.nombre,l.hectareas,l.cultivo,l.valle,if(l.campaniaCerrada) 1L else 0L,l.dosisMaximaPorHectarea)
    fun guardar(l: Labor,operacion: String="",propietario: String="",clave: String="",error: String="")=q.guardarLabor(l.id,l.loteId,l.tipo.name,l.fecha,l.responsable,l.cantidad,l.observacion,if(l.pendiente) 1L else 0L,l.creadoEn,operacion,propietario,clave,error)
}
fun pe.upeu.agrosierra.db.Lote.dominio()=Lote(id,codigo,nombre,hectareas,cultivo,valle,campaniaCerrada==1L,dosisMaxima)
fun pe.upeu.agrosierra.db.Labor.dominio()=Labor(id,loteId,TipoLabor.valueOf(tipo),fecha,responsable,cantidad,observacion,pendiente==1L,creadoEn,operacion=="DELETE")
