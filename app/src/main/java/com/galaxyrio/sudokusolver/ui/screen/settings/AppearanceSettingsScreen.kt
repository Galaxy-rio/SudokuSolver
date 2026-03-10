package com.galaxyrio.sudokusolver.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.data.ThemeMode
import com.galaxyrio.sudokusolver.ui.viewmodel.SettingsViewModel
import com.materialkolor.PaletteStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppearanceSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val themeMode by viewModel.themeMode.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val useDynamicColors by viewModel.useDynamicColors.collectAsState()
    val paletteStyle by viewModel.paletteStyle.collectAsState()

    var showPaletteStyleDialog by remember { mutableStateOf(false) }

    if (showPaletteStyleDialog) {
        AlertDialog(
            onDismissRequest = { showPaletteStyleDialog = false },
            title = { Text("Palette Style") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    PaletteStyle.entries.forEach { style ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setPaletteStyle(style)
                                    showPaletteStyleDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (style == paletteStyle),
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = style.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaletteStyleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Appearance", modifier = Modifier.padding(start = 4.dp)) },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {

            item {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, top = 0.dp, bottom = 8.dp)
                )
            }



            item {
                SegmentedListItem(
                    onClick = { null },
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),

                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = ListItemDefaults.SegmentedGap)
                ) {
                    Column(modifier = Modifier) {
                        Text("Accent Color", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        ColorPicker(
                            isDynamic = useDynamicColors,
                            selectedColor = themeColor,
                            onDynamicSelected = { viewModel.setUseDynamicColors(true) },
                            onColorSelected = {
                                viewModel.setUseDynamicColors(false)
                                viewModel.setThemeColor(it)
                            }
                        )
                    }
                }

            }


            item {
                SegmentedListItem(
                    onClick = { showPaletteStyleDialog = true },
                    shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxSize(),

                    supportingContent = { Text(paletteStyle.name) },

                    ) {
                    Text("Palette Style")
                }

            }


            item {
                Text(
                    text = "Mode",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    ThemeModeOption(
                        "System Default",
                        themeMode == ThemeMode.SYSTEM
                    ) { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                    ThemeModeOption("Light", themeMode == ThemeMode.LIGHT) {
                        viewModel.setThemeMode(
                            ThemeMode.LIGHT
                        )
                    }
                    ThemeModeOption("Dark", themeMode == ThemeMode.DARK) {
                        viewModel.setThemeMode(
                            ThemeMode.DARK
                        )
                    }
                }
            }


        }
    }
}

@Composable
fun ThemeModeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ColorPicker(
    isDynamic: Boolean,
    selectedColor: Color,
    onDynamicSelected: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color(0xFF6750A4), // Purple
        Color(0xFFB3261E), // Red
        Color(0xFFE27C33), // Orange
        Color(0xFF7D5260), // Pink
        Color(0xFF3F51B5), // Indigo
        Color(0xFF009688), // Teal
        Color(0xFF4CAF50), // Green
        Color(0xFFFFEB3B), // Yellow
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable { onDynamicSelected() }
                    .then(
                        if (isDynamic) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BrightnessAuto,
                    contentDescription = "Dynamic",
                    tint = if (isDynamic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(colors) { color ->
            val isSelected = !isDynamic && selectedColor == color
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onColorSelected(color) }
                    .then(
                        if (isSelected) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
