package pe.upeu.agrosierra.presentation.theme
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
@Composable fun AgroCampoTheme(darkTheme: Boolean=false,content: @Composable () -> Unit) {
    MaterialTheme(colorScheme=if(darkTheme) darkColorScheme() else lightColorScheme(),content=content)
}
