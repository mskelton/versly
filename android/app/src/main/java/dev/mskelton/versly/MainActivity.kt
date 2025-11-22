package dev.mskelton.versly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import dev.mskelton.versly.api.BASE_URL
import dev.mskelton.versly.api.LocalVerslyService
import dev.mskelton.versly.api.VerslyService
import dev.mskelton.versly.persistence.AppPreferences
import dev.mskelton.versly.persistence.BibleDatabase
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.sync.SyncManager
import dev.mskelton.versly.ui.theme.VerslyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val DEFAULT_TRANSLATION = "ESV"

val LocalToolbarVisibility =
    compositionLocalOf<MutableState<Boolean>> { error("No toolbar visibility state provided") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val retrofit =
            Retrofit.Builder()
                .addConverterFactory(
                    GsonConverterFactory.create(
                        GsonBuilder()
                            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            .create()
                    )
                )
                .baseUrl(BASE_URL)
                .build()

        val verslyService: VerslyService = retrofit.create(VerslyService::class.java)
        val bibleDatabase = BibleDatabase(this, verslyService)
        val appPreferences = AppPreferences(this)

        SyncManager.startPeriodicSync(this)

        lifecycleScope.launch { appPreferences.passage.first() }

        enableEdgeToEdge()
        setContent {
            VerslyTheme {
                CompositionLocalProvider(LocalBibleDatabase provides bibleDatabase) {
                    CompositionLocalProvider(LocalVerslyService provides verslyService) {
                        CompositionLocalProvider(LocalAppPreferences provides appPreferences) {
                            App()
                        }
                    }
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

private val TOP_LEVEL_ROUTES: List<TopLevelRoute> = listOf(Read, Plans, Search)

@Composable
fun MainScreen() {
    val backStack = rememberNavBackStack(Read)
    val toolbarVisible = remember { mutableStateOf(true) }

    CompositionLocalProvider(LocalToolbarVisibility provides toolbarVisible) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                AnimatedVisibility(
                    visible = toolbarVisible.value,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                ) {
                    NavigationBar {
                        TOP_LEVEL_ROUTES.forEach { route ->
                            val isSelected = route == backStack.last()

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (!isSelected) {
                                        backStack.clear()
                                        backStack.add(route)
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
                }
            },
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = { key ->
                        when (key) {
                            is Read -> NavEntry(key) { ReadScreen() }
                            is Plans -> NavEntry(key) { PlansScreen() }
                            is Search -> NavEntry(key) { SearchScreen() }
                            else -> error("Unknown route: $key")
                        }
                    },
                )
            }
        }
    }
}
