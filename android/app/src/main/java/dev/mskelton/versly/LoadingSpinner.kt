package dev.mskelton.versly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun LoadingSpinner() {
    var showSpinner by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        showSpinner = true
    }

    Box(
        modifier = Modifier.padding(16.dp, 32.dp).fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (showSpinner) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 4.dp)
        }
    }
}
