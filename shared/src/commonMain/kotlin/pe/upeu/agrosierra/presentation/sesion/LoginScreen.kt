package pe.upeu.agrosierra.presentation.sesion
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import pe.upeu.agrosierra.presentation.AgroUiState
@Composable fun LoginScreen(st: AgroUiState,ingresar: (String,String) -> Unit) {
    var usuario by rememberSaveable { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().imePadding(),contentAlignment=Alignment.Center) {
        Column(Modifier.widthIn(max=480.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(24.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Agriculture,null,Modifier.size(48.dp));Text("AgroCampo",style=MaterialTheme.typography.headlineLarge)
                    Text("Las labores de tu cooperativa, siempre a mano.",style=MaterialTheme.typography.bodyLarge)
                }
            }
            Text("Bienvenido a AgroSierra",style=MaterialTheme.typography.titleLarge)
            Text("Inicia sesión para consultar tus lotes y registrar el trabajo de campo.",color=MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(usuario,{usuario=it},Modifier.fillMaxWidth(),label={Text("Usuario")},singleLine=true,enabled=!st.cargando,leadingIcon={Icon(Icons.Default.Person,null)})
            OutlinedTextField(clave,{clave=it},Modifier.fillMaxWidth(),label={Text("Contraseña")},singleLine=true,enabled=!st.cargando,visualTransformation=if(visible) VisualTransformation.None else PasswordVisualTransformation(),leadingIcon={Icon(Icons.Default.Lock,null)},trailingIcon={IconButton({visible=!visible}) {Icon(if(visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,"Mostrar contraseña")}})
            (st.campos["login"] ?: st.error?.let { when(it) {
                is pe.upeu.agrosierra.domain.model.ErrorApi.NoAutorizado -> "Usuario o contraseña incorrectos"
                is pe.upeu.agrosierra.domain.model.ErrorApi.SinConexion -> "No se pudo conectar. Revisa tu conexión e intenta nuevamente"
                else -> it.message
            } })?.let { Text(it,color=MaterialTheme.colorScheme.error) }
            Button({ingresar(usuario,clave)},Modifier.fillMaxWidth(),enabled=!st.cargando) { if(st.cargando) {CircularProgressIndicator(Modifier.size(20.dp),strokeWidth=2.dp);Spacer(Modifier.width(12.dp))};Text(if(st.cargando) "Ingresando…" else "Ingresar") }
            Text("Necesitas conexión para iniciar sesión. Después podrás consultar y registrar labores sin cobertura.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
