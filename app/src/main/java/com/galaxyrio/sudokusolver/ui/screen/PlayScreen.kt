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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

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
                scrollBehavior = scrollBehavior
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
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Recent Games",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    if (savedGames.isEmpty()) {
                         OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
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
                                    text = "No saved games found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Display only the last 3 saved games or similar logic if needed,
                        // but here we just show them in a column inside the card or separate cards.
                        // User asked for "Saved games, using card suite".
                        // Let's list each game as a card.
                        savedGames.forEach { game ->
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
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Difficulty",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Difficulty.entries.forEachIndexed { index, level ->
                            SegmentedButton(
                                selected = difficulty == level,
                                onClick = { difficulty = level },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = Difficulty.entries.size
                                ),
                                label = {
                                    Text(
                                        text = level.name,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp)) // Spacing for FAB
                }
            }
        }
    }
}
