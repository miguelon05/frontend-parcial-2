package pe.upeu.agrosierra.presentation.perfil
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.agrosierra.domain.model.Sesion
@Composable fun PerfilScreen(s: Sesion,salir: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.fillMaxWidth().padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Person,null,Modifier.size(40.dp));Text(s.nombre.ifBlank {s.usuario},style=MaterialTheme.typography.headlineSmall)
            Text(s.usuario);Text(s.rol.replace('_',' '),style=MaterialTheme.typography.labelLarge)
        } }
        Text("Tus privilegios",style=MaterialTheme.typography.titleMedium)
        s.privilegios.sorted().forEach { Text("• ${it.replace('_',' ').lowercase().replaceFirstChar {c -> c.uppercase()}}") }
        OutlinedButton(salir,Modifier.fillMaxWidth()) {Text("Cerrar sesión")}
    }
}
