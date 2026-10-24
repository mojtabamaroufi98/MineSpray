package com.minespray.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF121416); val Surf = Color(0xFF1C1F23); val Border = Color(0xFF2E3338)
val Amber = Color(0xFFFFB300); val Water = Color(0xFF29B6F6)
val Ok = Color(0xFF43A047); val Warn = Color(0xFFFFA000); val Crit = Color(0xFFE53935); val Off = Color(0xFF8A9199)
val TextHi = Color(0xFFECEFF1); val TextLo = Color(0xFFA0A8B0)

@Composable
fun MineSprayTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = darkColorScheme(primary = Amber, onPrimary = Color.Black, background = Bg, surface = Surf,
        onBackground = TextHi, onSurface = TextHi, secondary = Water, surfaceVariant = Surf, outline = Border),
    content = content
)
