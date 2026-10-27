package pe.upeu.agrosierra
import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import pe.upeu.agrosierra.data.local.BaseLocal
import pe.upeu.agrosierra.db.AgroDb
fun crearBaseLocal(context: Context)=BaseLocal(AgroDb(AndroidSqliteDriver(AgroDb.Schema,context,"agrocampo.db")))
