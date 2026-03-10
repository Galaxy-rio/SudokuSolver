package com.galaxyrio.sudokusolver.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

enum class SettingsCategory(val title: String, val icon: ImageVector, val subtitle: String, val idx: Int) {
    APPEARANCE("Appearance", Icons.Default.Palette,"Theme", 0),
    GAME("Game", Icons.Default.SportsEsports,"Input, Rules", 1),
    ASSISTANCE("Assistance", Icons.AutoMirrored.Filled.Help,"Learning", 2),
    FILES("Files", Icons.Default.Folder,"Import & Export", 3),
    LANGUAGE("Language", Icons.Default.Language,"English", 4),
    ABOUT("About", Icons.Default.Info,"Version", 5)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onNavigateTo: (SettingsCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Settings", modifier= Modifier.padding(start = 4.dp)) },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer),
                // scrollBehavior = scrollBehavior // 设置列表较短，暂时不需要滚动收缩
            )
        }
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {

            items(SettingsCategory.entries) { category ->
                SegmentedListItem(
                    onClick = {onNavigateTo(category)},
                    shapes = ListItemDefaults.segmentedShapes(index = category.idx, count = 6),

                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(50.dp),
                            contentAlignment = Alignment.Center
                        ){
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                            ){}
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.title,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    },
                    content = { Text(category.title) },
                    supportingContent = { Text(category.subtitle) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),


                )
            }
        }
    }
}
