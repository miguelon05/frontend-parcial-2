package pe.upeu.agrosierra.data.local
import kotlinx.coroutines.flow.StateFlow
interface MonitorRed { val conectado: StateFlow<Boolean> }
