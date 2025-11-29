package dev.mskelton.versly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import dev.mskelton.versly.api.LocalVerslyService
import dev.mskelton.versly.api.VerslyService
import dev.mskelton.versly.persistence.AppPreferences
import dev.mskelton.versly.persistence.BibleDatabase
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.PlansViewModel
import dev.mskelton.versly.persistence.ReadViewModel
import dev.mskelton.versly.persistence.SearchViewModel
import dev.mskelton.versly.persistence.SettingsViewModel
import dev.mskelton.versly.sync.SyncManager
import dev.mskelton.versly.ui.theme.VerslyTheme
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val DEFAULT_TRANSLATION = "ESV"

val LocalToolbarVisibility =
    compositionLocalOf<MutableState<Boolean>> { error("No toolbar visibility state provided") }

val LocalBackStack = compositionLocalOf<NavBackStack<NavKey>> { error("No back stack provided") }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var verslyService: VerslyService
    @Inject lateinit var bibleDatabase: BibleDatabase
    @Inject lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SyncManager.startPeriodicSync(this)

        lifecycleScope.launch { appPreferences.passage.first() }

        enableEdgeToEdge()
        setContent {
            VerslyTheme {
                CompositionLocalProvider(
                    LocalBibleDatabase provides bibleDatabase,
                    LocalVerslyService provides verslyService,
                    LocalAppPreferences provides appPreferences,
                ) {
                    App()
                }
            }
        }
    }
}

@Composable
fun App() {
    val bibleDatabase = LocalBibleDatabase.current
    var isInitialized by rememberSaveable { mutableStateOf(bibleDatabase.isInitialized()) }

    LaunchedEffect(Unit) {
        // If the database hasn't been initialized with the default translation,
        // let's download it so the user has something to read when they first
        // open the app.
        if (!isInitialized) {
            withContext(Dispatchers.IO) {
                bibleDatabase.downloadTranslation(DEFAULT_TRANSLATION)
                isInitialized = true
            }
        }
    }

    if (!isInitialized) {
        LoadingScreen(translation = DEFAULT_TRANSLATION)
    } else {
        MainScreen()
    }
}

private val TOP_LEVEL_ROUTES: List<TopLevelRoute> = listOf(Read(), Plans, Search, Settings)

@Composable
fun MainScreen() {
    val backStack = rememberNavBackStack(Read())
    val toolbarVisible = remember { mutableStateOf(true) }

    CompositionLocalProvider(
        LocalToolbarVisibility provides toolbarVisible,
        LocalBackStack provides backStack,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    val currentRoute = backStack.lastOrNull { it is TopLevelRoute }

                    TOP_LEVEL_ROUTES.forEach { route ->
                        val isSelected = route::class == currentRoute?.let { it::class }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) {
                                    backStack.replace(route)
                                }
                            },
                            label = { Text(stringResource(route.label)) },
                            icon = {
                                Icon(
                                    painter = painterResource(route.icon),
                                    contentDescription = null,
                                )
                            },
                        )
                    }
                }
            },
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                val sceneStrategy = rememberSheetSceneStrategy<NavKey>()

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    sceneStrategy = sceneStrategy,
                    entryDecorators =
                        listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                    entryProvider =
                        entryProvider {
                            entry<Read>(metadata = SheetSceneStrategy.index(0)) { key ->
                                val viewModel =
                                    hiltViewModel<ReadViewModel, ReadViewModel.Factory>(
                                        creationCallback = { factory -> factory.create(key) }
                                    )

                                ReadScreen(passageId = key.passageId, viewModel = viewModel)
                            }
                            entry<Plans>(metadata = SheetSceneStrategy.index(1)) { key ->
                                val viewModel =
                                    hiltViewModel<PlansViewModel, PlansViewModel.Factory>(
                                        creationCallback = { factory -> factory.create(key) }
                                    )

                                PlansScreen(viewModel = viewModel)
                            }
                            entry<Search>(metadata = SheetSceneStrategy.index(2)) { key ->
                                val viewModel =
                                    hiltViewModel<SearchViewModel, SearchViewModel.Factory>(
                                        creationCallback = { factory -> factory.create(key) }
                                    )

                                SearchScreen(viewModel = viewModel)
                            }
                            entry<Settings>(metadata = SheetSceneStrategy.index(3)) { key ->
                                val viewModel =
                                    hiltViewModel<SettingsViewModel, SettingsViewModel.Factory>(
                                        creationCallback = { factory -> factory.create(key) }
                                    )

                                SettingsScreen(viewModel = viewModel)
                            }
                            entry<PickPassageSheet>(metadata = SheetSceneStrategy.sheet()) { key ->
                                BookChapterPickerSheet(passageId = key.current)
                            }
                            entry<PickTranslationSheet>(metadata = SheetSceneStrategy.sheet()) {
                                TranslationPickerSheet()
                            }
                        },
                    transitionSpec = {
                        val slideDirection = getSlideDirection(initialState, targetState)
                        slideInHorizontally(initialOffsetX = { it * slideDirection }) +
                            fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { -it * slideDirection }) +
                                fadeOut()
                    },
                    popTransitionSpec = {
                        val slideDirection = getSlideDirection(initialState, targetState)
                        slideInHorizontally(initialOffsetX = { -it * slideDirection }) +
                            fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { it * slideDirection }) +
                                fadeOut()
                    },
                    predictivePopTransitionSpec = {
                        val slideDirection = getSlideDirection(initialState, targetState)
                        slideInHorizontally(initialOffsetX = { -it * slideDirection }) +
                            fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { it * slideDirection }) +
                                fadeOut()
                    },
                )
            }
        }
    }
}

private fun getSlideDirection(initialState: Scene<NavKey>, targetState: Scene<NavKey>): Int {
    val initialIndex = initialState.metadata[SheetSceneStrategy.INDEX_KEY] as? Int ?: -1
    val targetIndex = targetState.metadata[SheetSceneStrategy.INDEX_KEY] as? Int ?: -1

    return if (targetIndex >= initialIndex) 1 else -1
}
