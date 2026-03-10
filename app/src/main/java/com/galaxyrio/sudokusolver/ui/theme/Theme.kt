package com.galaxyrio.sudokusolver.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SudokuSolverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    amoled: Boolean = false,
    colorSeed: Color = Color.Green,
    paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    content: @Composable () -> Unit
) {
    if (dynamicColor) {
        val context = LocalContext.current
        val colorScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    } else {
        DynamicMaterialTheme(
            seedColor = colorSeed,
            isDark = darkTheme,
            style = paletteStyle,
            isAmoled = amoled,
            typography = Typography,
            content = {
                MaterialTheme(
                    colorScheme = MaterialTheme.colorScheme,
                    typography = Typography,
                    motionScheme = MotionScheme.expressive(),
                    content = content
                )
            }
        )
    }
}