package pe.upeu.agrosierra.presentation.detalle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.agrosierra.presentation.AgroUiState
import pe.upeu.agrosierra.domain.model.Labor
import kotlin.time.Clock
import kotlin.time.Instant
@Composable fun DetalleScreen(st: AgroUiState,registrar: () -> Unit,editar: (Labor) -> Unit,eliminar: (Labor) -> Unit,cerrar: () -> Unit) {
    val l=st.lote ?: return
    val s=st.sesion ?: return
    var borrar by remember { mutableStateOf<Labor?>(null) }
    var confirmarCierre by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=16.dp)) {
        item { Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Landscape,null,Modifier.size(36.dp));Text(l.nombre,style=MaterialTheme.typography.headlineSmall)
                Text("${l.codigo} · Valle ${l.valle}");Text("${l.cultivo} · ${l.hectareas} hectáreas")
                Text(if(l.campaniaCerrada) "Campaña cerrada" else "Campaña abierta",style=MaterialTheme.typography.labelLarge)
                Text("Dosis máxima: ${l.dosisMaximaPorHectarea} por ha (${l.dosisMaximaPorHectarea*l.hectareas} en total)",style=MaterialTheme.typography.bodySmall)
            }
        } }
        item { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            if("LABOR_CREAR" in s.privilegios && !l.campaniaCerrada) Button(registrar,enabled=!st.cargando) {Icon(Icons.Default.Add,null);Text("Registrar labor")}
            if("CAMPANIA_CERRAR" in s.privilegios && !l.campaniaCerrada) OutlinedButton({confirmarCierre=true},enabled=!st.cargando && !st.sincronizando) {Text("Cerrar campaña")}
        } }
        item { Text("Labores del lote",style=MaterialTheme.typography.titleMedium) }
        if(st.labores.isEmpty()) item { Text("Este lote todavía no tiene labores guardadas.",Modifier.padding(20.dp)) }
        items(st.labores,key={it.id}) { labor ->
            Card { Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text(labor.tipo.name,style=MaterialTheme.typography.titleSmall);Text(labor.fecha,style=MaterialTheme.typography.labelMedium) }
                Text("${labor.cantidad} · ${labor.responsable}",style=MaterialTheme.typography.bodyMedium)
                if(labor.observacion.isNotEmpty()) Text(labor.observacion,style=MaterialTheme.typography.bodySmall)
                if(labor.pendiente) { Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {Icon(Icons.Default.CloudUpload,null,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.tertiary);Text("Pendiente de sincronizar · ID ${labor.id}",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.tertiary)} }
                val enPlazo=runCatching { Clock.System.now()-Instant.parse(labor.creadoEn.let { if(it.endsWith("Z")) it else it+"Z" }) }.getOrNull()?.inWholeHours?.let {it in 0..23} ?: false
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    if("LABOR_EDITAR" in s.privilegios && !l.campaniaCerrada && (s.editaTodas || labor.responsable==s.usuario && (enPlazo || labor.id<0))) TextButton({editar(labor)},enabled=!st.cargando && !st.sincronizando) {Icon(Icons.Default.Edit,null,Modifier.size(18.dp));Spacer(Modifier.width(4.dp));Text("Corregir")}
                    if("LABOR_ELIMINAR" in s.privilegios) TextButton({borrar=labor},enabled=!st.cargando && !st.sincronizando) {Icon(Icons.Default.Delete,null,Modifier.size(18.dp));Text("Eliminar")}
                }
            } }
        }
    }
    if(borrar!=null) AlertDialog(onDismissRequest={borrar=null},title={Text("¿Eliminar esta labor?")},text={Text("Se eliminará del servicio al sincronizar. Si estás sin conexión, la operación quedará pendiente.")},confirmButton={TextButton({eliminar(borrar!!);borrar=null}) {Text("Eliminar")}},dismissButton={TextButton({borrar=null}) {Text("Cancelar")}})
    if(confirmarCierre) AlertDialog(onDismissRequest={confirmarCierre=false},title={Text("¿Cerrar la campaña?")},text={Text("Debe existir una cosecha sincronizada. Después del cierre no podrás registrar ni corregir labores.")},confirmButton={TextButton({cerrar();confirmarCierre=false}) {Text("Cerrar campaña")}},dismissButton={TextButton({confirmarCierre=false}) {Text("Cancelar")}})
}
