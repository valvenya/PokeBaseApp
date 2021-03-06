package ru.frozenpriest.pokebase.di

import ru.frozenpriest.pokebase.domain.DataStoreRepository
import ru.frozenpriest.pokebase.domain.login.LoginRegisterUseCase
import ru.frozenpriest.pokebase.domain.pokemon.AddPokemonUseCase
import ru.frozenpriest.pokebase.domain.pokemon.GetDamageUseCase
import ru.frozenpriest.pokebase.domain.pokemon.GetMovesUseCase
import ru.frozenpriest.pokebase.domain.pokemon.GetOwnedPokemonShortUseCase
import ru.frozenpriest.pokebase.domain.pokemon.GetPokemonDetailsUseCase
import ru.frozenpriest.pokebase.domain.pokemon.GetSpeciesUseCase
import ru.frozenpriest.pokebase.injector.BaseFeatureAPI
import ru.frozenpriest.pokebase.injector.BaseFeatureDependencies
import ru.frozenpriest.pokebase.injector.ComponentHolder
import ru.frozenpriest.pokebase.injector.ComponentHolderDelegate

object AppComponentHolder : ComponentHolder<AppFeatureApi, AppFeatureDependencies> {
    private val componentHolderDelegate = ComponentHolderDelegate<
        AppFeatureApi,
        AppFeatureDependencies,
        AppComponent> { dependencies: AppFeatureDependencies ->
        AppComponent.initAndGet(dependencies)
    }

    internal fun getComponent(): AppComponent = componentHolderDelegate.getComponentImpl()

    override var dependencyProvider: (() -> AppFeatureDependencies)? by componentHolderDelegate::dependencyProvider

    override fun get(): AppFeatureApi = componentHolderDelegate.get()
}

interface AppFeatureDependencies : BaseFeatureDependencies {
    val loginRegisterUseCase: LoginRegisterUseCase
    val dataStoreRepository: DataStoreRepository

    val getOwnedPokemonShortUseCase: GetOwnedPokemonShortUseCase
    val getMovesUseCase: GetMovesUseCase
    val getPokemonDetailsUseCase: GetPokemonDetailsUseCase
    val getSpeciesUseCase: GetSpeciesUseCase
    val addPokemonUseCase: AddPokemonUseCase
    val getDamageUseCase: GetDamageUseCase
}

interface AppFeatureApi : BaseFeatureAPI
