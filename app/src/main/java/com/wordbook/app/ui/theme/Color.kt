package com.wordbook.app.ui.theme

import androidx.compose.ui.graphics.Color

val Red = Color(0xFFD32F2F)
val DeepOrange = Color(0xFFE64A19)
val Yellow = Color(0xFFF9A825)
val Green = Color(0xFF2E7D32)
val Blue = Color(0xFF1565C0)
val Grey = Color(0xFF9E9E9E)
val DarkGrey = Color(0xFF424242)
val LightGrey = Color(0xFFEEEEEE)
val CardBackground = Color(0xFFFAFAFA)

fun qualityColor(quality: Int): Color = when (quality) {
    1 -> Red
    2 -> DeepOrange
    3 -> Yellow
    4 -> Green
    5 -> Blue
    else -> Grey
}
