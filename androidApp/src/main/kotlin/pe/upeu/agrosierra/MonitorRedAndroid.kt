package pe.upeu.agrosierra
import android.content.Context
import android.net.*
import kotlinx.coroutines.flow.MutableStateFlow
import pe.upeu.agrosierra.data.local.MonitorRed
class MonitorRedAndroid(context: Context): MonitorRed {
    private val cm=context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private fun activo()=cm.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)==true
    override val conectado=MutableStateFlow(activo())
    init { cm.registerDefaultNetworkCallback(object: ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { conectado.value=true }
        override fun onLost(network: Network) { conectado.value=activo() }
        override fun onCapabilitiesChanged(network: Network,caps: NetworkCapabilities) { conectado.value=caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) }
    }) }
}
