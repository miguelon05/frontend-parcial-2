package pe.upeu.agrosierra.data.remote
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.*
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.upeu.agrosierra.data.local.AlmacenSeguro
import pe.upeu.agrosierra.data.remote.dto.ErrorDto
import pe.upeu.agrosierra.domain.model.ErrorApi
private val formatoJson=Json { ignoreUnknownKeys=true }
fun crearCliente(engine: HttpClientEngine,config: ApiConfig,seguro: AlmacenSeguro): HttpClient=HttpClient(engine) {
    expectSuccess=false
    install(ContentNegotiation) { json(formatoJson) }
    install(HttpTimeout) { requestTimeoutMillis=15000;connectTimeoutMillis=10000;socketTimeoutMillis=15000 }
    defaultRequest {
        url(config.baseUrl)
        contentType(ContentType.Application.Json)
        seguro.sesion.value?.token?.let { header(HttpHeaders.Authorization,"Bearer $it") }
    }
    HttpResponseValidator {
        validateResponse { response ->
            if(response.status.value>=400) {
                val detalle=runCatching { formatoJson.decodeFromString<ErrorDto>(response.bodyAsText()) }.getOrDefault(ErrorDto())
                if(response.status.value==401) seguro.borrar()
                throw mapearError(response.status.value,detalle)
            }
        }
        handleResponseExceptionWithRequest { cause,_ ->
            when(cause) {
                is ErrorApi -> throw cause
                is kotlinx.coroutines.CancellationException -> throw cause
                is HttpRequestTimeoutException,is ConnectTimeoutException,is SocketTimeoutException -> throw ErrorApi.TiempoAgotado()
                is kotlinx.serialization.SerializationException -> throw ErrorApi.Servidor("El servicio devolvió datos incompatibles")
                else -> throw ErrorApi.SinConexion()
            }
        }
    }
}
