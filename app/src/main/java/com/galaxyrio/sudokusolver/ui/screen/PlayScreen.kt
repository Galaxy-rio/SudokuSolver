package com.galaxyrio.sudokusolver.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlayMenuScreen(
    modifier: Modifier = Modifier,
    initialDifficulty: Difficulty = Difficulty.MEDIUM,
    onStartGame: (Difficulty) -> Unit,
    onContinueGame: (String) -> Unit,
    onDeleteGames: (List<SavedGame>) -> Unit = {}, // callback for deletion
    viewModel: PlayViewModel = viewModel()
) {
    var difficulty by rememberSaveable(initialDifficulty) { mutableStateOf(initialDifficulty) }
    // Real state for existing game
    val savedGames by viewModel.savedGames.collectAsState()

    // Multi-selection state
    var selectedGameIds by rememberSaveable { mutableStateOf(emptySet<String>()) }
    val isSelectionMode = selectedGameIds.isNotEmpty()

    // Handle back press to exit selection mode
    BackHandler(enabled = isSelectionMode) {
        selectedGameIds = emptySet()
    }

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
                        if (isSelectionMode) "${selectedGameIds.size} Selected" else "Sudoku",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = expandedColor,
                    scrolledContainerColor = collapsedColor
                ),
                navigationIcon = {
                     if (isSelectionMode) {
                         androidx.compose.material3.IconButton(onClick = { selectedGameIds = emptySet() }) {
                             Icon(Icons.Default.Close, contentDescription = "Close Selection")
                         }
                     }
                }
            )
        },
        floatingActionButton = {
            AnimatedContent(
                targetState = isSelectionMode,
                transitionSpec = {
                    (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
                },
                label = "fab_transition"
            ) { selectionMode ->
                if (selectionMode) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            val selectedGames = savedGames.filter { it.id in selectedGameIds }
                            viewModel.deleteGames(selectedGames)
                            onDeleteGames(selectedGames)
                            selectedGameIds = emptySet()
                        },
                        icon = { Icon(Icons.Default.Delete, "Delete Selected") },
                        text = { Text(text = "Delete") },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                } else {
                    ExtendedFloatingActionButton(
                        onClick = { onStartGame(difficulty) },
                        icon = { Icon(Icons.Filled.Add, "Start New Game") },
                        text = { Text(text = "New Game") },
                    )
                }
            }
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
                        val isSelected = game.id in selectedGameIds

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            selectedGameIds = if (isSelected) {
                                                selectedGameIds - game.id
                                            } else {
                                                selectedGameIds + game.id
                                            }
                                        } else {
                                            onContinueGame(game.id)
                                        }
                                    },
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            selectedGameIds = selectedGameIds + game.id
                                        }
                                    }
                                ),
                            colors = if (isSelected) {
                                CardDefaults.outlinedCardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            } else {
                                CardDefaults.outlinedCardColors()
                            },
                             border = if (isSelected) {
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                CardDefaults.outlinedCardBorder()
                            }
                        ) {
                            ListItem(
                                headlineContent = { Text("Game ${game.date}") },
                                supportingContent = {
                                    Text("${game.difficulty} • ${game.completionPercentage}% Complete")
                                },
                                leadingContent = {
                                    if (isSelected) {
                                         Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.DateRange,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                trailingContent = {
                                    if (!isSelectionMode) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Resume"
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                                )
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
