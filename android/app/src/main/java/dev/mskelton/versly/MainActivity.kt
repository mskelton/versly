package dev.mskelton.versly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.runtime.snapshotFlow
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
import dev.mskelton.versly.persistence.MemoryViewModel
import dev.mskelton.versly.persistence.PlansViewModel
import dev.mskelton.versly.persistence.ReadViewModel
import dev.mskelton.versly.persistence.SearchViewModel
import dev.mskelton.versly.persistence.SettingsViewModel
import dev.mskelton.versly.sync.SyncManager
import dev.mskelton.versly.ui.theme.VerslyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

const val DEFAULT_TRANSLATION = "ESV"

val LocalToolbarVisibility =
    compositionLocalOf<MutableState<Boolean>> { error("No toolbar visibility state provided") }

val LocalBackStack = compositionLocalOf<NavBackStack<NavKey>> { error("No back stack provided") }

val LocalReadViewModel = compositionLocalOf<ReadViewModel> { error("No ReadViewModel provided") }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var verslyService: VerslyService

    @Inject
    lateinit var bibleDatabase: BibleDatabase

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SyncManager.startPeriodicSync(this)

        val initialTab = runBlocking { appPreferences.lastTab.first() }

        enableEdgeToEdge()
        setContent {
            VerslyTheme {
                CompositionLocalProvider(
                    LocalBibleDatabase provides bibleDatabase,
                    LocalVerslyService provides verslyService,
                    LocalAppPreferences provides appPreferences,
                ) {
                    App(initialTab = initialTab)
                }
            }
        }
    }
}

@Composable
fun App(initialTab: String) {
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
        MainScreen(initialTab = initialTab)
    }
}

private val TOP_LEVEL_ROUTES: List<TopLevelRoute> = listOf(Read, Plans, Memory, Search, Settings)

private fun tabToRoute(tab: String): TopLevelRoute =
    when (tab) {
        "plans" -> Plans
        "memory" -> Memory
        "search" -> Search
        "settings" -> Settings
        else -> Read
    }

private fun routeToTab(route: TopLevelRoute?): String =
    when (route) {
        is Plans -> "plans"
        is Memory -> "memory"
        is Search -> "search"
        is Settings -> "settings"
        else -> "read"
    }

@Composable
fun MainScreen(initialTab: String) {
    val appPreferences = LocalAppPreferences.current
    val backStack = rememberNavBackStack(tabToRoute(initialTab))

    LaunchedEffect(Unit) {
        snapshotFlow { backStack.lastOrNull { it is TopLevelRoute } as? TopLevelRoute }
            .collect { route -> appPreferences.setLastTab(routeToTab(route)) }
    }

    val toolbarVisible = remember { mutableStateOf(true) }

    val readViewModel =
        hiltViewModel<ReadViewModel, ReadViewModel.Factory>(
            creationCallback = { factory -> factory.create(Read) },
        )
    val plansViewModel =
        hiltViewModel<PlansViewModel, PlansViewModel.Factory>(
            creationCallback = { factory -> factory.create(Plans) },
        )
    val settingsViewModel =
        hiltViewModel<SettingsViewModel, SettingsViewModel.Factory>(
            creationCallback = { factory -> factory.create(Settings) },
        )
    val memoryViewModel =
        hiltViewModel<MemoryViewModel, MemoryViewModel.Factory>(
            creationCallback = { factory -> factory.create(Memory) },
        )

    CompositionLocalProvider(
        LocalToolbarVisibility provides toolbarVisible,
        LocalBackStack provides backStack,
        LocalReadViewModel provides readViewModel,
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
                            entry<Read>(metadata = SheetSceneStrategy.index(0)) {
                                ReadScreen(viewModel = readViewModel)
                            }
                            entry<Plans>(metadata = SheetSceneStrategy.index(1)) {
                                PlansScreen(viewModel = plansViewModel)
                            }
                            entry<Memory>(metadata = SheetSceneStrategy.index(2)) {
                                MemoryScreen(viewModel = memoryViewModel)
                            }
                            entry<Search>(metadata = SheetSceneStrategy.index(3)) { key ->
                                val viewModel =
                                    hiltViewModel<SearchViewModel, SearchViewModel.Factory>(
                                        creationCallback = { factory -> factory.create(key) },
                                    )

                                SearchScreen(viewModel = viewModel)
                            }
                            entry<Settings>(metadata = SheetSceneStrategy.index(4)) {
                                SettingsScreen(viewModel = settingsViewModel)
                            }
                            entry<PickPassage> { key ->
                                BookChapterPickerScreen(passageId = key.current)
                            }
                            entry<PracticeVerse> { key ->
                                val verse = memoryViewModel.getVerseById(key.id)
                                if (verse != null) {
                                    PracticeScreen(verse = verse, viewModel = memoryViewModel)
                                }
                            }
                            entry<PickMemoryVerse> {
                                PickMemoryVerseScreen(viewModel = memoryViewModel)
                            }
                            entry<PickTranslationSheet>(metadata = SheetSceneStrategy.sheet()) {
                                TranslationPickerSheet()
                            }
                        },
                    transitionSpec = {
                        val direction = getSlideDirection(initialState, targetState)
                        horizontalSlideTransition(direction)
                    },
                    popTransitionSpec = {
                        val direction = getSlideDirection(initialState, targetState)
                        horizontalSlidePopTransition(direction)
                    },
                    predictivePopTransitionSpec = {
                        val spec = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)
                        scaleIn(spec, initialScale = 0.85f) + fadeIn(spec) togetherWith
                            scaleOut(spec, targetScale = 0.85f) + fadeOut(spec)
                    },
                )
            }
        }
    }
}

private fun getSlideDirection(
    initialState: Scene<NavKey>,
    targetState: Scene<NavKey>,
): Int {
    val initialIndex = initialState.metadata[SheetSceneStrategy.INDEX_KEY] as? Int ?: return 1
    val targetIndex = targetState.metadata[SheetSceneStrategy.INDEX_KEY] as? Int ?: return 1

    return if (targetIndex >= initialIndex) 1 else -1
}
