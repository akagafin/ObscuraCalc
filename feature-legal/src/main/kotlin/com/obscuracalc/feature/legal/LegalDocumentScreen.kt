package com.obscuracalc.feature.legal

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun LegalDocumentScreen(
    title: String,
    assetPath: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var content by remember(assetPath) { mutableStateOf("Loading...") }

    LaunchedEffect(assetPath) {
        content = context.assets.open(assetPath).bufferedReader().use { it.readText() }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        SelectionContainer {
            Text(
                text = "$title\n\n$content",
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
