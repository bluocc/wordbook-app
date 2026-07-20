package com.wordbook.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF2E7D32),
    onSecondary = Color.White,
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    onBackground = Color(0xFF1C1B1F),
)

@Composable
fun WordbookTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
