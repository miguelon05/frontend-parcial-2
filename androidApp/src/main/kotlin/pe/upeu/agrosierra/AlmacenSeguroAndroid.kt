package pe.upeu.agrosierra
import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import pe.upeu.agrosierra.data.local.AlmacenSeguro
import pe.upeu.agrosierra.domain.model.Sesion
@Suppress("DEPRECATION")
class AlmacenSeguroAndroid(context: Context): AlmacenSeguro {
    private val preferencias=EncryptedSharedPreferences.create(context,"sesion_segura",MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    override val sesion=MutableStateFlow<Sesion?>(recuperar())
    private fun recuperar(): Sesion?=runCatching {
        val token=preferencias.getString("token",null) ?: return null
        val exp=preferencias.getLong("expira",0)
        if(exp<=System.currentTimeMillis()/1000) { preferencias.edit().clear().commit(); return null }
        Sesion(token,preferencias.getString("usuario","")!!,preferencias.getString("rol","")!!,preferencias.getStringSet("privilegios",emptySet())!!.toSet(),preferencias.getString("nombre","")!!,exp,preferencias.getBoolean("editaTodas",false))
    }.getOrNull()
    override fun guardar(sesion: Sesion) {
        val exp=if(sesion.expiraEn>0) sesion.expiraEn else runCatching {
            val payload=String(Base64.decode(sesion.token.split('.')[1],Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
            JSONObject(payload).getLong("exp")
        }.getOrElse { throw IllegalArgumentException("El token no contiene vencimiento") }
        val s=sesion.copy(expiraEn=exp,editaTodas=sesion.editaTodas || "LABOR_ELIMINAR" in sesion.privilegios)
        check(preferencias.edit().putString("token",s.token).putString("usuario",s.usuario).putString("rol",s.rol).putString("nombre",s.nombre).putStringSet("privilegios",s.privilegios).putLong("expira",s.expiraEn).putBoolean("editaTodas",s.editaTodas).commit())
        this.sesion.value=s
    }
    override fun borrar() { preferencias.edit().clear().commit();sesion.value=null }
}
