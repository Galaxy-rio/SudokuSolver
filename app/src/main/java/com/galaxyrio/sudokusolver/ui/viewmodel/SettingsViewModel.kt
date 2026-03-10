package com.galaxyrio.sudokusolver.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.galaxyrio.sudokusolver.data.PaletteStyleOption
import com.galaxyrio.sudokusolver.data.SettingsRepository
import com.galaxyrio.sudokusolver.data.ThemeMode
import com.materialkolor.PaletteStyle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)

    val themeMode: StateFlow<ThemeMode> = repository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val themeColor: StateFlow<Color> = repository.themeColorArgb.map { Color(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Color.Blue)

    val paletteStyle: StateFlow<PaletteStyle> = repository.paletteStyle.map {
        when(it) {
            PaletteStyleOption.TonalSpot -> PaletteStyle.TonalSpot
            PaletteStyleOption.Neutral -> PaletteStyle.Neutral
            PaletteStyleOption.Vibrant -> PaletteStyle.Vibrant
            PaletteStyleOption.Expressive -> PaletteStyle.Expressive
            PaletteStyleOption.Rainbow -> PaletteStyle.Rainbow
            PaletteStyleOption.FruitSalad -> PaletteStyle.FruitSalad
            PaletteStyleOption.Monochrome -> PaletteStyle.Monochrome
            PaletteStyleOption.Fidelity -> PaletteStyle.Fidelity
            PaletteStyleOption.Content -> PaletteStyle.Content
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PaletteStyle.TonalSpot)

    val useDynamicColors: StateFlow<Boolean> = repository.useDynamicColors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isAmoled: StateFlow<Boolean> = repository.isAmoled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setThemeMode(mode: ThemeMode) {
        repository.setThemeMode(mode)
    }

    fun setThemeColor(color: Color) {
        repository.setThemeColorArgb(color.toArgb())
    }

    fun setPaletteStyle(style: PaletteStyle) {
        val option = when(style) {
            PaletteStyle.TonalSpot -> PaletteStyleOption.TonalSpot
            PaletteStyle.Neutral -> PaletteStyleOption.Neutral
            PaletteStyle.Vibrant -> PaletteStyleOption.Vibrant
            PaletteStyle.Expressive -> PaletteStyleOption.Expressive
            PaletteStyle.Rainbow -> PaletteStyleOption.Rainbow
            PaletteStyle.FruitSalad -> PaletteStyleOption.FruitSalad
            PaletteStyle.Monochrome -> PaletteStyleOption.Monochrome
            PaletteStyle.Fidelity -> PaletteStyleOption.Fidelity
            PaletteStyle.Content -> PaletteStyleOption.Content
            else -> PaletteStyleOption.TonalSpot
        }
        repository.setPaletteStyle(option)
    }

    fun setUseDynamicColors(enabled: Boolean) {
        repository.setUseDynamicColors(enabled)
    }

    fun setIsAmoled(enabled: Boolean) {
        repository.setIsAmoled(enabled)
    }
}
