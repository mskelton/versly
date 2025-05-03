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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dev.mskelton.versly.api.LocalVerslyService
import dev.mskelton.versly.api.VerslyService
import dev.mskelton.versly.persistence.BibleDatabase
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.ui.theme.VerslyTheme
import retrofit2.Retrofit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VerslyTheme {
                val context = LocalContext.current
                val bibleDatabase = BibleDatabase(context)
                val retrofit =
                    Retrofit.Builder().baseUrl("https://versly.mskelton.dev/api/").build()
                val verslyService: VerslyService = retrofit.create(VerslyService::class.java)

                CompositionLocalProvider(LocalBibleDatabase provides bibleDatabase) {
                    CompositionLocalProvider(LocalVerslyService provides verslyService) {
                        App()
                    }
                }
            }
        }
    }
}

@Composable
fun App() {
    val bibleDatabase = LocalBibleDatabase.current
    val verslyService = LocalVerslyService.current
    var isInitialized by rememberSaveable {
        mutableStateOf(bibleDatabase.isInitialized())
    }

    LaunchedEffect(Unit) {
        // If the database hasn't been initialized with the default translation,
        // let's download it so the user has something to read when they first open the app.
        if (!isInitialized) {
            bibleDatabase.download(verslyService, DEFAULT_TRANSLATION)
            isInitialized = true
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
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.READ) }
    val destinations = listOf(
        AppDestination.READ,
        AppDestination.PLANS,
        AppDestination.PROFILE,
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(tabs = destinations,
                selectedTab = currentDestination,
                onTabSelected = { currentDestination = it })
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentDestination) {
                AppDestination.READ -> ReadScreen()
                AppDestination.PLANS -> PlansScreen()
                AppDestination.PROFILE -> ProfileScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    tabs: List<AppDestination>,
    selectedTab: AppDestination,
    onTabSelected: (AppDestination) -> Unit,
) {
    NavigationBar {
        tabs.forEachIndexed { _, tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                label = { Text(stringResource(tab.label)) },
                icon = {
                    Icon(
                        painter = if (selectedTab == tab) {
                            painterResource(tab.iconSelected)
                        } else {
                            painterResource(tab.icon)
                        },
                        contentDescription = stringResource(tab.contentDescription),
                    )
                },
            )
        }
    }
}
