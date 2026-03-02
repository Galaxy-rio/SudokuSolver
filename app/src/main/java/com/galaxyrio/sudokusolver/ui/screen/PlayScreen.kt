package com.galaxyrio.sudokusolver.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.galaxyrio.sudokusolver.ui.viewmodel.PlayViewModel


enum class Difficulty {
    EASY, MEDIUM, HARD
}

data class SavedGame(
    val id: String,
    val date: String,
    val difficulty: Difficulty,
    val completionPercentage: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayMenuScreen(
    modifier: Modifier = Modifier,
    initialDifficulty: Difficulty = Difficulty.MEDIUM,
    onStartGame: (Difficulty) -> Unit,
    onContinueGame: (String) -> Unit,
    viewModel: PlayViewModel = viewModel()
) {
    var difficulty by rememberSaveable(initialDifficulty) { mutableStateOf(initialDifficulty) }
    // Real state for existing game
    val savedGames by viewModel.savedGames.collectAsState()

    val filteredSavedGames by remember(savedGames, difficulty) {
        derivedStateOf { savedGames.filter { it.difficulty == difficulty } }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // Define shared colors
    val collapsedColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    val expandedColor = MaterialTheme.colorScheme.surface

    // Determine the container color based on scroll state
    val containerColor = lerp(
        expandedColor,
        collapsedColor,
        scrollBehavior.state.collapsedFraction
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Sudoku",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = expandedColor,
                    scrolledContainerColor = collapsedColor
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onStartGame(difficulty) },
                icon = { Icon(Icons.Filled.Add, "Start New Game") },
                text = { Text(text = "New Game") },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            TabRow(
                selectedTabIndex = difficulty.ordinal,
                containerColor = containerColor,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Difficulty.entries.forEach { level ->
                    Tab(
                        selected = difficulty == level,
                        onClick = { difficulty = level },
                        text = { Text(text = level.name) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    Text(
                        text = "Recent Games",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (filteredSavedGames.isEmpty()) {
                    item {
                         OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                             colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No saved games found for ${difficulty.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredSavedGames.size) { index ->
                        val game = filteredSavedGames[index]
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onContinueGame(game.id) },
                        ) {
                            ListItem(
                                headlineContent = { Text("Game ${game.date}") },
                                supportingContent = {
                                    Text("${game.difficulty} • ${game.completionPercentage}% Complete")
                                },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Outlined.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingContent = {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Resume"
                                    )
                                }
                            )
                        }
                    }
                }

                item {
                     Spacer(modifier = Modifier.height(80.dp)) // Spacing for FAB
                }
            }
        }
    }
}
