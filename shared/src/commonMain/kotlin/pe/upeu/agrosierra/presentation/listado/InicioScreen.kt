package pe.upeu.agrosierra.presentation.listado
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.agrosierra.presentation.AgroUiState
@Composable fun InicioScreen(st: AgroUiState,verLotes: () -> Unit,sync: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Agriculture,null,Modifier.size(40.dp));Text("AgroCampo",style=MaterialTheme.typography.headlineSmall)
                Text("Sistema de gestión de labores de campo para la cooperativa AgroSierra.")
                Text("Hola, ${st.sesion?.nombre?.ifBlank { st.sesion.usuario }}",style=MaterialTheme.typography.titleMedium)
            }
        }
        Text("Qué puedes hacer",style=MaterialTheme.typography.titleMedium)
        Card(onClick=verLotes) { Row(Modifier.fillMaxWidth().padding(20.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)) { Icon(Icons.Default.Landscape,null);Column { Text("Consultar lotes",style=MaterialTheme.typography.titleSmall);Text("Revisa cultivos, campañas y labores de los tres valles.",style=MaterialTheme.typography.bodySmall) } } }
        Card(onClick=sync) { Row(Modifier.fillMaxWidth().padding(20.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)) { Icon(Icons.Default.Sync,null);Column { Text("Sincronizar labores",style=MaterialTheme.typography.titleSmall);Text("Envía los cambios del dispositivo y recibe la información actualizada.",style=MaterialTheme.typography.bodySmall) } } }
        EstadoConexion(st)
    }
}
@Composable fun EstadoConexion(st: AgroUiState) {
    Surface(color=MaterialTheme.colorScheme.surfaceVariant,shape=MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            Text(if(st.conectado) "Red disponible" else "Sin conexión · Modo de campo",style=MaterialTheme.typography.labelLarge)
            Text(st.ultimaSync?.let { "Última sincronización: ${it.take(19).replace('T',' ')} UTC" } ?: "Todavía no se ha sincronizado este dispositivo",style=MaterialTheme.typography.bodySmall)
        }
    }
}
