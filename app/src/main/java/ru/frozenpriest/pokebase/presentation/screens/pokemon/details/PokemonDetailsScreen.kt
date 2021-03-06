@file:OptIn(ExperimentalPagerApi::class, ExperimentalFoundationApi::class)

package ru.frozenpriest.pokebase.presentation.screens.pokemon.details

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.frozenpriest.pokebase.R
import ru.frozenpriest.pokebase.domain.model.Move
import ru.frozenpriest.pokebase.domain.model.Pokemon
import ru.frozenpriest.pokebase.presentation.NavigationDestination
import ru.frozenpriest.pokebase.presentation.common.TypesLine
import ru.frozenpriest.pokebase.presentation.common.blackOrWhiteContentColor
import ru.frozenpriest.pokebase.presentation.screens.pokemon.details.pages.AboutPokemon
import ru.frozenpriest.pokebase.presentation.screens.pokemon.details.pages.EvolutionPokemon
import ru.frozenpriest.pokebase.presentation.screens.pokemon.details.pages.MovesPokemon
import ru.frozenpriest.pokebase.presentation.screens.pokemon.details.pages.MovesRow
import ru.frozenpriest.pokebase.presentation.screens.pokemon.details.pages.StatsPokemon
import ru.frozenpriest.pokebase.presentation.screens.pokemon.owned.PokemonItem
import ru.frozenpriest.pokebase.presentation.theme.BlackText
import ru.frozenpriest.pokebase.presentation.theme.BlackTextTransparent
import ru.frozenpriest.pokebase.presentation.withTwoPokemon

@Composable
fun PokemonDetailsScreen(
    viewModel: PokemonDetailsViewModel,
    navController: NavController,
    pokemonId: String
) {
    LaunchedEffect(key1 = null) {
        viewModel.loadPokemon(pokemonId)
    }
    val selectedPokemon = viewModel.selectedPokemon.observeAsState()
    selectedPokemon.value?.let { pokemon ->
        var dominantColor by remember {
            mutableStateOf(Color.White)
        }
        val colorAnimated = animateColorAsState(targetValue = dominantColor)
        val pokemonDrawable =
            rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(pokemon.species.image)
                    .crossfade(true)
                    .build(),
                onSuccess = { state ->
                    viewModel.calculateDominantColor(
                        state.result.drawable,
                        onFinish = { color -> dominantColor = color }
                    )
                },
            )

        var showFightSelector by remember {
            mutableStateOf(false)
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                DetailsTopBar(colorAnimated.value, navController) { showFightSelector = true }
            }
        ) {
            DetailsContent(colorAnimated.value, pokemon, pokemonDrawable, viewModel)

            if (showFightSelector) {
                Box(modifier = Modifier.fillMaxSize()) {
                    SelectOpponentAlert(
                        onPokemonSelected = {
                            showFightSelector = false
                            navController.navigate(
                                NavigationDestination.PokemonBattle.destination.withTwoPokemon(
                                    selectedPokemon.value!!.id,
                                    it
                                )
                            )
                        },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun BoxScope.SelectOpponentAlert(
    onPokemonSelected: (String) -> Unit,
    viewModel: PokemonDetailsViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight(0.5f)
            .align(Alignment.Center)
            .zIndex(100f)
    ) {
        val pokemonState by viewModel.pokemons.observeAsState(emptyList())
        LaunchedEffect(key1 = null) {
            viewModel.loadPokemons()
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(pokemonState) { pokemon ->
                PokemonItem(
                    modifier = Modifier.padding(8.dp),
                    pokemon = pokemon
                ) {
                    onPokemonSelected(pokemon.id)
                }
            }
        }
    }
}

@Composable
private fun DetailsContent(
    dominantColor: Color,
    pokemon: Pokemon,
    pokemonDrawable: AsyncImagePainter,
    viewModel: PokemonDetailsViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dominantColor)
    ) {
        PokemonNameAndTypes(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            pokemon,
            dominantColor
        )
        Image(
            painter = painterResource(id = R.drawable.pokeball_icon),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.50f)
                .align(Alignment.CenterEnd)
                .offset(y = (-100).dp),
            alignment = Alignment.BottomEnd,
            alpha = 0.1f,
            colorFilter = ColorFilter.tint(blackOrWhiteContentColor(background = dominantColor))
        )
        PokemonSheetWithImage(pokemon, pokemonDrawable, viewModel)
    }
}

@Composable
private fun BoxScope.PokemonSheetWithImage(
    pokemon: Pokemon,
    pokemonDrawable: AsyncImagePainter,
    viewModel: PokemonDetailsViewModel
) {
    var showAddMove by remember {
        mutableStateOf(false)
    }
    var moveToAdd by remember {
        mutableStateOf<Move?>(null)
    }

    if (showAddMove) {
        AddMoveAlert(
            onShowChange = { showAddMove = it },
            moveToAdd = moveToAdd,
            onNewMoveSelected = { moveToAdd = it },
            viewModel = viewModel
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .align(Alignment.BottomCenter),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        PokemonStatsPager(pokemon, viewModel) { showAddMove = true }
    }
    Image(
        painter = pokemonDrawable,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .align(Alignment.Center)
            .offset(y = (-100).dp)
    )
}

@Composable
private fun DetailsTopBar(
    dominantColor: Color,
    navController: NavController,
    onFightClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(dominantColor)
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(
                    id = R.string.back
                ),
                tint = blackOrWhiteContentColor(dominantColor)
            )
        }
        IconButton(onClick = {
            onFightClick()
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_fight),
                contentDescription = stringResource(
                    id = R.string.fight_pokemon
                ),
                tint = blackOrWhiteContentColor(dominantColor)
            )
        }
    }
}

@Composable
fun PokemonNameAndTypes(modifier: Modifier, pokemon: Pokemon, dominantColor: Color) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = pokemon.name,
            style = MaterialTheme.typography.h1,
            color = blackOrWhiteContentColor(dominantColor)
        )
        Text(
            text = pokemon.species.name,
            style = MaterialTheme.typography.h2,
            color = blackOrWhiteContentColor(dominantColor)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TypesLine(pokemon.species.types)
    }
}

@Composable
private fun PokemonStatsPager(
    pokemon: Pokemon,
    viewModel: PokemonDetailsViewModel,
    addMoveDialog: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
    ) {

        val pagerState = rememberPagerState()
        val coroutineScope = rememberCoroutineScope()

        val tabNames = listOf(
            R.string.about,
            R.string.base_stats,
            R.string.evolution,
            R.string.moves
        )

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Tabs(tabNames, pagerState, coroutineScope)
        }
        HorizontalPager(count = tabNames.size, state = pagerState) { page ->
            when (page) {
                0 -> {
                    AboutPokemon(pokemon = pokemon)
                }
                1 -> {
                    StatsPokemon(pokemon = pokemon)
                }
                2 -> {
                    EvolutionPokemon(pokemon = pokemon)
                }
                3 -> {
                    MovesPokemon(
                        pokemon = pokemon,
                        removeMove = { viewModel.removeMove(it) },
                        addMove = { addMoveDialog() }
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.AddMoveAlert(
    onShowChange: (Boolean) -> Unit,
    moveToAdd: Move?,
    onNewMoveSelected: (Move) -> Unit,
    viewModel: PokemonDetailsViewModel
) {

    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight(0.5f)
            .align(Alignment.Center)
            .zIndex(100f)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = stringResource(R.string.add_new_move),
                style = MaterialTheme.typography.h1
            )

            val movesState by viewModel.moves.observeAsState(emptyList())
            LaunchedEffect(key1 = null) {
                viewModel.loadMoves()
            }
            LazyColumn(Modifier.weight(1f)) {
                items(movesState) { move ->
                    MovesRow(
                        move = move,
                        modifier = if (move == moveToAdd)
                            Modifier.background(MaterialTheme.colors.primaryVariant)
                        else Modifier
                    ) {
                        onNewMoveSelected(move)
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    modifier = Modifier.padding(4.dp),
                    enabled = moveToAdd != null,
                    onClick = {
                        viewModel.addMove(moveToAdd!!)
                        onShowChange(false)
                    }
                ) {
                    Text(text = stringResource(R.string.add_new_move))
                }
                Button(
                    modifier = Modifier.padding(4.dp),
                    onClick = {
                        onShowChange(false)
                    }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        }
    }
}

@Composable
private fun Tabs(
    tabNames: List<Int>,
    pagerState: PagerState,
    coroutineScope: CoroutineScope
) {
    tabNames.forEachIndexed { index, nameId ->
        Tab(
            selected = pagerState.currentPage == index,
            onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                }
            },
            text = {
                Text(
                    text = stringResource(id = nameId),
                    fontSize = 12.sp,
                    color = getTabTextColor(index, pagerState)
                )
            }
        )
    }
}

@Composable
private fun getTabTextColor(
    index: Int,
    pagerState: PagerState
) = if (index == pagerState.currentPage) BlackText else BlackTextTransparent
