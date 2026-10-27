package pe.upeu.agrosierra.di
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import pe.upeu.agrosierra.data.remote.*
import pe.upeu.agrosierra.data.repository.*
import pe.upeu.agrosierra.domain.repository.*
import pe.upeu.agrosierra.domain.usecase.*
import pe.upeu.agrosierra.presentation.AgroViewModel
val redModule=module { single { ApiConfig() };single { crearCliente(get(),get(),get()) } }
val dataModule=module {
    single { SesionRepository(get(),get()) }
    single<AgroRepository> { AgroRepositoryOffline(get(),get(),get()) }
}
val domainModule=module { factory { RegistrarLaborUseCase(get()) };factory { SincronizarUseCase(get()) } }
val presentationModule=module { viewModel { AgroViewModel(get(),get(),get(),get(),get()) } }
