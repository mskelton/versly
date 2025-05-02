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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.mskelton.versly.persistence.BibleDatabase
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.ui.theme.VerslyTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bibleDatabase = BibleDatabase(this)

        enableEdgeToEdge()
        setContent {
            VerslyTheme {
                CompositionLocalProvider(LocalBibleDatabase provides bibleDatabase) {
                    App()
                }
            }
        }
    }
}

@Composable
fun App() {
    MainScreen()
}

@Preview
@Composable
fun MainScreen() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.READ) }
    val destinations = listOf(
        AppDestination.READ,
        AppDestination.PLANS,
        AppDestination.PROFILE,
    )

    Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
        BottomNavigationBar(tabs = destinations,
            selectedTab = currentDestination,
            onTabSelected = { currentDestination = it })
    }) { innerPadding ->
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
    tabs: List<AppDestination>, selectedTab: AppDestination, onTabSelected: (AppDestination) -> Unit
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
                        }, contentDescription = stringResource(tab.contentDescription)
                    )
                },
            )
        }
    }
}
