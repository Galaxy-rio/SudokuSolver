package com.galaxyrio.sudokusolver.ui.screen

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize.Companion.AnimatedSize
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.galaxyrio.sudokusolver.ui.viewmodel.PlayViewModel
import libsudoku.wrapping.SudokuGenerator.Difficulty

data class SavedGame(
    val id: String,
    val date: String,
    val difficulty: Difficulty,
    val completionPercentage: Int,
    val emptyRemains: Int,
    val timeSpentSeconds: Long,
    val board: List<Int>
)

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class
)
@Composable
fun PlayMenuScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
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

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        if (isSelectionMode) "${selectedGameIds.size} Selected" else "Sudoku",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { selectedGameIds = emptySet() }) {
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
                    with(sharedTransitionScope) {
                        ExtendedFloatingActionButton(
                            modifier = Modifier.sharedBounds(
                                rememberSharedContentState(key = "game_container"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                resizeMode = RemeasureToBounds,
                            ),
                            onClick = { onStartGame(difficulty) },
                            icon = { Icon(Icons.Filled.Add, "Start New Game") },
                            text = { Text(text = "New Game") },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = difficulty.ordinal,
                modifier = Modifier,
                containerColor = MaterialTheme.colorScheme.surfaceContainer
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),

                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                item {
                    Text(
                        text = "Recent Games",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .padding(start = 4.dp)
                    )
                }
                with(sharedTransitionScope) {


                    if (filteredSavedGames.isEmpty()) {
                        item {
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.3f
                                    ),
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
                            val itemKeyContainer = "game_container_${game.id}"
                            val itemKeyThumbnail = "thumbnail_${game.id}"

                            SegmentedListItem(
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
                                },
                                shapes =
                                    if (isSelected) {
                                        ListItemDefaults.shapes(MaterialTheme.shapes.large)
                                    } else {
                                        ListItemDefaults.segmentedShapes(
                                            index = index,
                                            count = filteredSavedGames.size
                                        )
                                    },
                                content = { Text(game.date) },
                                supportingContent = {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(0.dp)
                                    ) {

                                        Text(formatSecondsToTime(game.timeSpentSeconds))
                                        Text("${game.emptyRemains} numbers left")
                                    }

                                },
                                leadingContent = {
                                    val cornerRadius = if (isSelected) 12.dp else 4.dp
                                    SudokuThumbnail(
                                        board = game.board,
                                        cornerRadius = cornerRadius,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .sharedBounds(
                                                rememberSharedContentState(key = itemKeyThumbnail),
                                                resizeMode = RemeasureToBounds,

                                                animatedVisibilityScope = animatedVisibilityScope,
                                            )

                                    )

                                },
                                trailingContent = {
                                    Box(
                                        modifier = Modifier
                                            .height(64.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!isSelectionMode) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Resume"
                                            )
                                        }
                                        if (isSelected) {

                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary
                                            )


                                        }
                                    }


                                },
                                colors =
                                    if (isSelected) {
                                        ListItemDefaults.colors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    } else {
                                        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                                    },
                                modifier = Modifier
                                    .sharedBounds(
                                        rememberSharedContentState(key = itemKeyContainer),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        resizeMode = RemeasureToBounds,
                                        placeholderSize = AnimatedSize,

                                        )
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Spacing for FAB
                    }
                }
            }
        }
    }
}

@Composable
fun SudokuThumbnail(
    board: List<Int>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Dp = 4.dp,
    strokeWidth: Dp = 1.5.dp
) {
    Canvas(modifier = modifier.clip(RoundedCornerShape(cornerRadius))) {
        val boardSize = size.minDimension
        val cellSize = boardSize / 9
        val strokeWidthPx = strokeWidth.toPx()
        val halfStroke = strokeWidthPx / 2

        // Draw background for filled cells
        board.forEachIndexed { index, value ->
            if (value != 0) {
                val row = index / 9
                val col = index % 9
                drawRect(
                    color = color.copy(alpha = 0.5f),
                    topLeft = Offset(col * cellSize, row * cellSize),
                    size = Size(cellSize, cellSize)
                )
            }
        }

        // Draw major grid lines (3x3 blocks)
        for (i in 1..2) {
            val linePos = i * 3 * cellSize
            // Horizontal
            drawLine(
                color = color,
                start = Offset(0f, linePos),
                end = Offset(boardSize, linePos),
                strokeWidth = strokeWidthPx
            )
            // Vertical
            drawLine(
                color = color,
                start = Offset(linePos, 0f),
                end = Offset(linePos, boardSize),
                strokeWidth = strokeWidthPx
            )
        }

        // Border
        drawRoundRect(
            color = color,
            topLeft = Offset(halfStroke, halfStroke),
            size = Size(boardSize - strokeWidthPx, boardSize - strokeWidthPx),
            cornerRadius = CornerRadius(cornerRadius.toPx() - halfStroke),
            style = Stroke(width = strokeWidthPx)
        )
    }
}

@SuppressLint("DefaultLocale")
fun formatSecondsToTime(totalSeconds: Long): String {
    val duration = java.time.Duration.ofSeconds(totalSeconds)
    val hours = duration.toHours()
    val minutes = duration.toMinutesPart()
    val seconds = duration.toSecondsPart()

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)

}

