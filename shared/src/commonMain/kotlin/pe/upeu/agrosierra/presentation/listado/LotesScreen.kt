package pe.upeu.agrosierra.presentation.listado
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.agrosierra.presentation.AgroUiState
import pe.upeu.agrosierra.domain.model.Lote
@Composable fun LotesScreen(st: AgroUiState,buscar: (String) -> Unit,valle: (String) -> Unit,detalle: (Lote) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(st.busqueda,buscar,Modifier.fillMaxWidth(),label={Text("Buscar por nombre o código")},singleLine=true,leadingIcon={Icon(Icons.Default.Search,null)})
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            (listOf("")+st.valles).forEach { nombre -> FilterChip(st.valle==nombre,{valle(nombre)},label={Text(nombre.ifBlank {"Todos los valles"})}) }
        }
        EstadoConexion(st)
        Text("${st.lotes.size} lotes",style=MaterialTheme.typography.labelLarge)
        if(st.lotes.isEmpty() && st.sincronizando) { CircularProgressIndicator() }
        else if(st.lotes.isEmpty()) { Text(if(st.ultimaSync==null) "Aún no hay lotes guardados. Sincroniza con el servicio para comenzar." else "No se encontraron lotes con estos filtros.",Modifier.padding(vertical=24.dp)) }
        LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=20.dp)) {
            items(st.lotes,key={it.id}) { l -> Card(onClick={detalle(l)}) {
                Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text(l.codigo,style=MaterialTheme.typography.labelMedium);Text(if(l.campaniaCerrada) "Campaña cerrada" else "Campaña abierta",style=MaterialTheme.typography.labelMedium,color=if(l.campaniaCerrada) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) }
                    Text(l.nombre,style=MaterialTheme.typography.titleMedium)
                    Text("${l.cultivo} · ${l.hectareas} ha",style=MaterialTheme.typography.bodyMedium)
                    Text("Valle ${l.valle}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } }
        }
    }
}
