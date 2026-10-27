package pe.upeu.agrosierra.presentation.detalle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.upeu.agrosierra.presentation.AgroUiState
import pe.upeu.agrosierra.domain.model.*
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
@Composable fun LaborForm(st: AgroUiState,edicion: Labor?,guardar: (Labor) -> Unit) {
    val lote=st.lote ?: return
    var tipo by rememberSaveable(edicion?.id) {mutableStateOf(edicion?.tipo?.name ?: "RIEGO")}
    var fecha by rememberSaveable(edicion?.id) {mutableStateOf(edicion?.fecha ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString())}
    var cantidad by rememberSaveable(edicion?.id) {mutableStateOf(edicion?.cantidad?.toString() ?: "")}
    var observacion by rememberSaveable(edicion?.id) {mutableStateOf(edicion?.observacion ?: "")}
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Text(lote.nombre,style=MaterialTheme.typography.titleLarge)
        Text("Tipo de labor",style=MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) { TipoLabor.entries.forEach { t -> FilterChip(tipo==t.name,{tipo=t.name},label={Text(t.name.lowercase().replaceFirstChar {it.uppercase()})}) } }
        st.campos["tipo"]?.let {Text(it,color=MaterialTheme.colorScheme.error)}
        Campo("Fecha (AAAA-MM-DD)",fecha,{fecha=it},st.campos["fecha"],st.cargando)
        Campo("Cantidad aplicada",cantidad,{cantidad=it},st.campos["cantidad"],st.cargando,true)
        if(tipo=="FUMIGACION") Text("Dosis máxima del lote: ${lote.hectareas*lote.dosisMaximaPorHectarea}",style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(observacion,{observacion=it},Modifier.fillMaxWidth(),label={Text("Observación")},minLines=3,enabled=!st.cargando,isError=st.campos.containsKey("observacion"),supportingText={Text(st.campos["observacion"] ?: "${observacion.length}/500 caracteres")})
        Text("Responsable: ${edicion?.responsable ?: st.sesion?.usuario}",style=MaterialTheme.typography.bodyMedium)
        if(!st.conectado) Text("Puedes guardar sin cobertura. La labor quedará pendiente de envío.",style=MaterialTheme.typography.bodySmall)
        Button(onClick={guardar(Labor(edicion?.id ?: 0,lote.id,TipoLabor.valueOf(tipo),fecha,edicion?.responsable ?: st.sesion!!.usuario,cantidad.replace(',','.').toDoubleOrNull() ?: 0.0,observacion,true,edicion?.creadoEn ?: ""))},modifier=Modifier.fillMaxWidth(),enabled=!st.cargando && !st.sincronizando) {Text(if(st.cargando) "Guardando…" else "Guardar labor")}
    }
}
@Composable private fun Campo(label: String,valor: String,cambiar: (String) -> Unit,error: String?,cargando: Boolean,numerico: Boolean=false) {
    OutlinedTextField(valor,cambiar,Modifier.fillMaxWidth(),label={Text(label)},singleLine=true,enabled=!cargando,isError=error!=null,supportingText={if(error!=null) Text(error)},keyboardOptions=KeyboardOptions(keyboardType=if(numerico) KeyboardType.Decimal else KeyboardType.Text))
}
