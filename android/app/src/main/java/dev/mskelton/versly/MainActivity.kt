package dev.mskelton.versly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import dev.mskelton.versly.api.LocalVerslyService
import dev.mskelton.versly.api.VerslyService
import dev.mskelton.versly.persistence.AppPreferences
import dev.mskelton.versly.persistence.BibleDatabase
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.ui.theme.VerslyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

const val DEFAULT_TRANSLATION = "ESV"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val retrofit = Retrofit.Builder().baseUrl("https://versly.mskelton.dev/api/").build()
        val verslyService: VerslyService = retrofit.create(VerslyService::class.java)
        val bibleDatabase = BibleDatabase(this, verslyService)
        val appPreferences = AppPreferences(this)

        lifecycleScope.launch {
            appPreferences.selectedBook.first()
            appPreferences.selectedChapter.first()
            appPreferences.selectedTranslation.first()
        }

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

@Composable
fun MainScreen() {
    val appPreferences = LocalAppPreferences.current
    val scope = rememberCoroutineScope()

    val selectedDestination by appPreferences.selectedDestination.collectAsState(initial = -1)

    if (selectedDestination == -1) {
        LoadingSpinner()
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    AppDestination.entries.forEachIndexed { index, destination ->
                        NavigationBarItem(
                            selected = selectedDestination == index,
                            onClick = {
                                scope.launch {
                                    appPreferences.setSelectedDestination(destination.ordinal)
                                }
                            },
                            label = { Text(stringResource(destination.label)) },
                            icon = {
                                Icon(
                                    painter =
                                        if (selectedDestination == destination.ordinal) {
                                            painterResource(destination.iconSelected)
                                        } else {
                                            painterResource(destination.icon)
                                        },
                                    contentDescription =
                                        stringResource(destination.contentDescription),
                                )
                            },
                        )
                    }
                }
            },
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedDestination) {
                    AppDestination.READ.ordinal -> ReadScreen()
                    AppDestination.PLANS.ordinal -> PlansScreen()
                    AppDestination.PROFILE.ordinal -> ProfileScreen()
                }
            }
        }
    }
}
