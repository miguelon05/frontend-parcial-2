package pe.upeu.agrosierra
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import pe.upeu.agrosierra.data.local.*
val platformModule=module {
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
    single { crearBaseLocal(androidContext()) }
    single<HttpClientEngine> { OkHttp.create() }
    single<MonitorRed> { MonitorRedAndroid(androidContext()) }
}
