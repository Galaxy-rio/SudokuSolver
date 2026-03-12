package com.galaxyrio.sudokusolver.ui.screen.play

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.galaxyrio.sudokusolver.database.AppDatabase
import com.galaxyrio.sudokusolver.database.SudokuEntity
import com.galaxyrio.sudokusolver.ui.components.NumberPad
import com.galaxyrio.sudokusolver.ui.components.SudokuBoard
import com.galaxyrio.sudokusolver.ui.components.SudokuBoardState
import libsudoku.wrapping.Cell
import libsudoku.wrapping.Sudoku
import libsudoku.wrapping.SudokuGenerator
import libsudoku.wrapping.SudokuGenerator.Difficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SudokuGameScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    difficulty: Difficulty,
    gameId: Long? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).sudokuDao() }
    val scope = rememberCoroutineScope()
    var currentGameId by remember { mutableStateOf(gameId) }

    var timeSpentSeconds by remember { mutableLongStateOf(0L) }
    var isTimerRunning by remember { mutableStateOf(true) }

    // Hint state
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Initialize the board using the generator
    // We use a nullable state to show loading until we check the DB
    var nullableSudokuBoardState by remember { mutableStateOf<SudokuBoardState?>(null) }

    LaunchedEffect(key1 = gameId, key2 = difficulty) {
        withContext(Dispatchers.IO) {
            var loadedBoardState: SudokuBoardState? = null
            if (gameId != null) {
                val savedGame: SudokuEntity? =
                    try {
                        dao.getGame(gameId)
                    } catch (e: Exception) {
                        Log.e("SudokuGameScreen", "Failed to load game: $gameId wit reason: ${e.message}")
                        null
                    }
                if (savedGame != null) {
                    loadedBoardState = savedGame.sudokuBoardState
                    timeSpentSeconds = savedGame.timeSpent
                }
            }

            if (loadedBoardState != null) {
                nullableSudokuBoardState = loadedBoardState
            } else {
                // New Game
                val newBoardState = SudokuBoardState(SudokuGenerator().generate(difficulty))
                // Save immediately to get an ID
                val newId = dao.saveGame(
                    SudokuEntity(
                        difficulty = difficulty,
                        sudokuBoardState = newBoardState,
                        timeSpent = 0
                    )
                )
                currentGameId = newId
                nullableSudokuBoardState = newBoardState
                timeSpentSeconds = 0
            }
        }
    }

    if (nullableSudokuBoardState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var sudokuBoardState by remember(nullableSudokuBoardState) { mutableStateOf(nullableSudokuBoardState!!) }

    var isNoteMode by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(false) }

    // Timer Logic
    LaunchedEffect(isTimerRunning, showWinDialog) {
        if (isTimerRunning && !showWinDialog) {
            val startTime = System.currentTimeMillis()
            val startSeconds = timeSpentSeconds
            while (true) {
                delay(1000L)
                val elapsed = (System.currentTimeMillis() - startTime) / 1000
                timeSpentSeconds = startSeconds + elapsed
            }
        }
    }

    // Lifecycle observer to pause timer when app is backgrounded
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                isTimerRunning = false
                // Save on pause
                scope.launch(Dispatchers.IO) {
                    dao.saveGame(SudokuEntity(
                        id = currentGameId!!,
                        difficulty = difficulty,
                        sudokuBoardState = sudokuBoardState,
                        timeSpent = timeSpentSeconds,
                    ))
                }
            } else if (event == Lifecycle.Event.ON_RESUME) {
                isTimerRunning = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Save on exit
            scope.launch(Dispatchers.IO) {
                withContext(NonCancellable) {
                    dao.saveGame(SudokuEntity(
                        id = currentGameId!!,
                        difficulty = difficulty,
                        sudokuBoardState = sudokuBoardState,
                        timeSpent = timeSpentSeconds,
                    ))
                }
            }
        }
    }

    // History for Undo
    val history = remember { mutableListOf<Sudoku>() }

    // Hint state
    // Scope is already defined above
    // ScaffoldState moved up for BackHandler access

    fun updateSudoku(newSudoku: Sudoku) {
        history.add(sudokuBoardState.sudoku)
        sudokuBoardState = sudokuBoardState.updateSudoku(newSudoku)

        // Save to DB asynchronously
        scope.launch(Dispatchers.IO) {
            dao.saveGame(SudokuEntity(
                id = currentGameId!!,
                difficulty = difficulty,
                sudokuBoardState = sudokuBoardState,
                timeSpent = timeSpentSeconds,
            ))
        }

        // Check win condition
        if (newSudoku.isBoardComplete && newSudoku.isConsistent) {
            showWinDialog = true
            // Clear saved game on win
            scope.launch(Dispatchers.IO) {
                currentGameId?.let { id ->
                    dao.deleteGame(id)
                }
            }
        }
    }

    fun undo() {
        if (history.isNotEmpty()) {
            sudokuBoardState.updateSudoku(history.removeAt(history.lastIndex))
        }
    }

    // Interaction source for the background click to avoid ripple effect if desired
    val interactionSource = remember { MutableInteractionSource() }

    with(sharedTransitionScope) {
        Scaffold(
            modifier = Modifier,
            topBar = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TopAppBar(
                        title = {
                            Text("Sudoku")
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                            )
                        }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                        )
                    )

                    Row(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = difficulty.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(start = 20.dp)
                        )
                        Text(
                            text = formatTime(timeSpentSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(end = 20.dp)
                        )
                    }
                }
            },
            bottomBar = {
                BottomAppBar(
                    actions = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Undo Button
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                ToolbarActionButton(
                                    onClick = { undo() },
                                    icon = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = "Undo",
                                )
                            }
                            // Delete Button
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                ToolbarActionButton(
                                    onClick = {
                                        if (sudokuBoardState.selectedRow != null && sudokuBoardState.selectedCol != null) {
                                            updateSudoku(sudokuBoardState.sudoku.setCell(sudokuBoardState.selectedRow!!, sudokuBoardState.selectedCol!!, Cell.Notes()))
                                        }
                                    },
                                    icon = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Delete"
                                )
                            }
                            // Node Mode Button
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                ToolbarActionButton(
                                    onClick = { isNoteMode = !isNoteMode },
                                    icon = Icons.Default.Edit,
                                    contentDescription = "Note Mode",
                                    isSelected = isNoteMode
                                )
                            }
                            // Auto Candidates Button
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                ToolbarActionButton(
                                    onClick = {
                                        updateSudoku(Sudoku(sudokuBoardState.sudoku.rebuildNotes()))
                                    },
                                    icon = Icons.Default.AutoAwesome,
                                    contentDescription = "Auto Candidates"
                                )
                            }
                            // Hint Button
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                ToolbarActionButton(
                                    onClick = {
                                        showBottomSheet = true
                                    },
                                    icon = Icons.Default.Lightbulb,
                                    contentDescription = "Hint"
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (showWinDialog) {
                AlertDialog(
                    onDismissRequest = { showWinDialog = false },
                    title = { Text(text = "Well Done!") },
                    text = { Text(text = "You have successfully solved the Sudoku puzzle.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showWinDialog = false
                                onBack()
                            }
                        ) {
                            Text("Awesome")
                        }
                    }
                )
            }

            val backgroundModifier = if (gameId != null) {
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .sharedBounds(
                        rememberSharedContentState(key = "game_container_$gameId"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
            } else {
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .sharedBounds(
                        rememberSharedContentState(key = "game_container"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    )

            }

            val boardModifier = if (gameId != null) {
                Modifier.sharedElement(
                    rememberSharedContentState(key = "thumbnail_$gameId"),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
                    .fillMaxSize()
            } else {
                Modifier.sharedElement(
                    rememberSharedContentState(key = "thumbnail"),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
                    .fillMaxSize()
            }

            BoxWithConstraints(
                modifier = backgroundModifier,
                contentAlignment = Alignment.TopCenter
            ) {
                val screenWidth = maxWidth

                val boardHeight = screenWidth - 32.dp

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Board Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(boardHeight)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SudokuBoard(
                            sudokuBoardState = sudokuBoardState,
                            onCellClick = { row, col ->
                                sudokuBoardState.toggleSelectPosition(row, col)
                                if (sudokuBoardState.selectedNumber != null) {
                                    if (isNoteMode) {
                                        updateSudoku(sudokuBoardState.sudoku.toggleNote(row, col, sudokuBoardState.selectedNumber!!))
                                    } else {
                                        val newValue = sudokuBoardState.selectedNumber!!
                                        if (sudokuBoardState.sudoku.board()[row][col].number() != newValue) {
                                            updateSudoku(sudokuBoardState.sudoku.fillNumber(row, col, newValue))
                                        }
                                    }
                                }
                            },
                            modifier = boardModifier,
                        )
                    }

                    // Remaining space for Number Pad
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                // Deselect cell when clicking empty space
                                sudokuBoardState.unselectNumber()
                            },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Middle Area: Number Pad
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f), // Take available space
                            contentAlignment = Alignment.Center
                        ) {
                            NumberPad(
                                selectedNumber = sudokuBoardState.selectedNumber,
                                onNumberClick = { number ->
                                    sudokuBoardState.toggleSelectNumber(number)
                                    // Logic update: If a cell is selected (Cell First Mode), fill it and Clear selection
                                    if (sudokuBoardState.selectedRow != null && sudokuBoardState.selectedCol != null) {
                                        val row = sudokuBoardState.selectedRow!!
                                        val col = sudokuBoardState.selectedCol!!
                                        val cell = sudokuBoardState.sudoku.board()[row][col]
                                        if (cell.number() == null) {
                                            if (isNoteMode) {
                                                updateSudoku(sudokuBoardState.sudoku.toggleNote(row, col, number))
                                            } else {
                                                if (cell.number() != number) {
                                                    updateSudoku(sudokuBoardState.sudoku.fillNumber(row, col, number))
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                com.galaxyrio.sudokusolver.ui.components.GameHintPanel(
                    onBackClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    },
                    onApplyClick = {
                        /* Apply Hint */
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                )
            }
        }
    }
}

@Composable
private fun ToolbarActionButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1.0f,
        label = "scale"
    )

    val containerColor =
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 400.dp),  //大于屏幕宽度的半径确保整个按钮都有水波纹效果
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .size(48.dp)
                .background(containerColor, androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}
