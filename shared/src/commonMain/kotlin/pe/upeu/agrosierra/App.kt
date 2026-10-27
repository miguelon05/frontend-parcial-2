package pe.upeu.agrosierra
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import pe.upeu.agrosierra.presentation.*
import pe.upeu.agrosierra.presentation.theme.AgroCampoTheme
import pe.upeu.agrosierra.presentation.sesion.LoginScreen
import pe.upeu.agrosierra.presentation.listado.*
import pe.upeu.agrosierra.presentation.detalle.*
import pe.upeu.agrosierra.presentation.perfil.PerfilScreen
import pe.upeu.agrosierra.domain.model.Labor
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun App(vm: AgroViewModel=koinViewModel()) {
    val st by vm.estado.collectAsState()
    var oscuro by rememberSaveable { mutableStateOf(false) }
    var destino by rememberSaveable { mutableStateOf("Inicio") }
    var formulario by rememberSaveable { mutableStateOf(false) }
    var edicion by remember { mutableStateOf<Labor?>(null) }
    val drawer=rememberDrawerState(DrawerValue.Closed)
    val scope=rememberCoroutineScope()
    LaunchedEffect(st.sesion?.usuario) { destino="Inicio";formulario=false;edicion=null }
    LaunchedEffect(st.guardado) { if(st.guardado) { formulario=false;edicion=null } }
    AgroCampoTheme(oscuro) {
        Surface(Modifier.fillMaxSize()) {
            if(st.iniciando) { Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) { CircularProgressIndicator() } }
            else if(st.sesion==null) LoginScreen(st,vm::ingresar)
            else ModalNavigationDrawer(drawerState=drawer,drawerContent={
                ModalDrawerSheet {
                    Row(Modifier.fillMaxWidth().padding(24.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                        Surface(shape=CircleShape,color=MaterialTheme.colorScheme.primaryContainer) { Icon(Icons.Default.Agriculture,null,Modifier.padding(10.dp).size(28.dp)) }
                        Column { Text("AgroCampo",style=MaterialTheme.typography.titleLarge);Text("Gestión de labores agrícolas",style=MaterialTheme.typography.bodyMedium) }
                    }
                    HorizontalDivider();Spacer(Modifier.height(12.dp))
                    listOf("Inicio" to Icons.Default.Home,"Lotes" to Icons.Default.Landscape,"Perfil" to Icons.Default.Person).forEach { (nombre,icono) ->
                        NavigationDrawerItem(label={Text(nombre)},icon={Icon(icono,null)},selected=destino==nombre,onClick={destino=nombre;vm.detalle(null);formulario=false;scope.launch {drawer.close()}},modifier=Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
                    }
                    Spacer(Modifier.weight(1f));HorizontalDivider()
                    Row(Modifier.fillMaxWidth().padding(24.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.DarkMode,null);Text("Modo oscuro",Modifier.weight(1f));Switch(oscuro,{oscuro=it})
                    }
                }
            }) {
                Scaffold(topBar={
                    TopAppBar(title={Text(if(formulario) if(edicion==null) "Registrar labor" else "Corregir labor" else st.lote?.nombre ?: destino)},navigationIcon={
                        IconButton(onClick={if(formulario) {formulario=false;vm.limpiar()} else if(st.lote!=null) vm.detalle(null) else scope.launch {drawer.open()}}) {
                            Icon(if(formulario || st.lote!=null) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,if(formulario || st.lote!=null) "Volver" else "Abrir menú")
                        }
                    },actions={IconButton(onClick=vm::sync,enabled=!st.sincronizando && !st.cargando) {Icon(Icons.Default.Sync,"Sincronizar")}},colors=TopAppBarDefaults.topAppBarColors(containerColor=MaterialTheme.colorScheme.primaryContainer))
                }) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        if(st.sincronizando || st.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
                        Mensajes(st,vm::sync,vm::limpiar)
                        when {
                            formulario && st.lote!=null -> LaborForm(st,edicion,vm::guardar)
                            st.lote!=null -> DetalleScreen(st,registrar={edicion=null;formulario=true;vm.limpiar()},editar={edicion=it;formulario=true;vm.limpiar()},eliminar=vm::eliminar,cerrar=vm::cerrar)
                            destino=="Inicio" -> InicioScreen(st,{destino="Lotes"},vm::sync)
                            destino=="Lotes" -> LotesScreen(st,{vm.filtrar(busqueda=it)},{vm.filtrar(valle=it)},vm::detalle)
                            else -> PerfilScreen(st.sesion!!,vm::salir)
                        }
                    }
                }
            }
        }
    }
}
@Composable private fun Mensajes(st: AgroUiState,reintentar: () -> Unit,limpiar: () -> Unit) {
    val mensaje=st.error?.message ?: st.mensaje ?: return
    Surface(color=if(st.error!=null) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,modifier=Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal=16.dp,vertical=8.dp)) {
            Text(mensaje,style=MaterialTheme.typography.bodySmall)
            Row {
                if(st.error is pe.upeu.agrosierra.domain.model.ErrorApi.Servidor || st.error is pe.upeu.agrosierra.domain.model.ErrorApi.TiempoAgotado || st.error is pe.upeu.agrosierra.domain.model.ErrorApi.SinConexion) TextButton(reintentar,enabled=!st.sincronizando) { Text("Reintentar") }
                TextButton(limpiar) {Text("Entendido")}
            }
        }
    }
}
