package com.galaxyrio.sudokusolver.ui.screen.play

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue

import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.game.Sudoku
import com.galaxyrio.sudokusolver.game.generator.SudokuGenerator
import com.galaxyrio.sudokusolver.ui.components.NumberPad
import com.galaxyrio.sudokusolver.ui.components.SudokuBoard
import com.galaxyrio.sudokusolver.ui.screen.Difficulty
import kotlinx.coroutines.launch
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import com.galaxyrio.sudokusolver.game.validator.SudokuValidator
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.CircularProgressIndicator
import com.galaxyrio.sudokusolver.database.AppDatabase
import com.galaxyrio.sudokusolver.database.SudokuEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ripple
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SudokuGameScreen(
    difficulty: Difficulty,
    gameId: Long? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).sudokuDao() }
    val scope = rememberCoroutineScope()
    var currentGameId by remember { mutableStateOf(gameId) }

    // Hint state
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    BackHandler(enabled = true) {
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            scope.launch { scaffoldState.bottomSheetState.hide() }
        } else {
            onBack()
        }
    }

    // Initialize the board using the generator
    // We use a nullable state to show loading until we check the DB
    var sudokuState by remember { mutableStateOf<Sudoku?>(null) }

    LaunchedEffect(key1 = gameId, key2 = difficulty) {
        withContext(Dispatchers.IO) {
            var loadedGame: Sudoku? = null
            if (gameId != null) {
                val savedGame = dao.getGame(gameId)
                if (savedGame != null) {
                    loadedGame = savedGame.sudoku
                }
            }

            if (loadedGame != null) {
                sudokuState = loadedGame
            } else {
                // New Game
                val clues = when (difficulty) {
                    Difficulty.EASY -> 40
                    Difficulty.MEDIUM -> 30
                    Difficulty.HARD -> 24
                }
                val newGame = SudokuGenerator().generate(clues)
                sudokuState = newGame
                // Save immediately to get an ID
                val newId = dao.saveGame(
                    SudokuEntity(
                        difficulty = difficulty,
                        sudoku = newGame
                    )
                )
                currentGameId = newId
            }
        }
    }

    if (sudokuState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var sudoku by remember(sudokuState) { mutableStateOf(sudokuState!!) }

    var selectedRow by remember { mutableStateOf<Int?>(null) }
    var selectedCol by remember { mutableStateOf<Int?>(null) }
    var selectedNumber by remember { mutableStateOf<Int?>(null) }
    var isNoteMode by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(false) }

    // History for Undo
    val history = remember { mutableListOf<Sudoku>() }

    // Hint state
    // Scope is already defined above
    // ScaffoldState moved up for BackHandler access

    fun updateSudoku(newSudoku: Sudoku) {
        history.add(sudoku)
        sudoku = newSudoku

        // Save to DB asynchronously
        scope.launch(Dispatchers.IO) {
            currentGameId?.let { id ->
                dao.saveGame(
                    SudokuEntity(
                        id = id,
                        difficulty = difficulty,
                        sudoku = newSudoku
                    )
                )
            }
        }

        // Check win condition
        val isFull = newSudoku.cells.none { it.value == 0 }
        if (isFull && SudokuValidator.isBoardValid(newSudoku)) {
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
            sudoku = history.removeAt(history.lastIndex)
        }
    }

    // Interaction source for the background click to avoid ripple effect if desired
    val interactionSource = remember { MutableInteractionSource() }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            com.galaxyrio.sudokusolver.ui.components.GameHintPanel(
                onBackClick = {
                    scope.launch { scaffoldState.bottomSheetState.hide() }
                },
                onApplyClick = {
                    /* Apply Hint */
                    scope.launch { scaffoldState.bottomSheetState.hide() }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            )
        },
        sheetPeekHeight = 0.dp,
        modifier = modifier.fillMaxSize()
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

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            val screenWidth = maxWidth

            val boardHeight = screenWidth-32.dp

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Top Bar Area
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TopAppBar(
                        title = { Text("Sudoku") },
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
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = difficulty.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                        )
                    }
                }

                // 2. Board Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(boardHeight)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SudokuBoard(
                        sudoku = sudoku,
                        onCellClick = { row, col ->
                            if (selectedRow == row && selectedCol == col) {
                                selectedRow = null
                                selectedCol = null
                            } else {
                                selectedRow = row
                                selectedCol = col
                            }

                            // If a number is selected in the pad, try to fill it
                            val currentCell = sudoku.getCell(row, col)
                            if (!currentCell.isFixed && selectedNumber != null) {
                                if (isNoteMode) {
                                    if (currentCell.value == 0) {
                                        updateSudoku(sudoku.toggleCandidate(row, col, selectedNumber!!))
                                    }
                                } else {
                                    val newValue = selectedNumber!!
                                    // Only update if value is changing
                                    if (currentCell.value != newValue) {
                                        updateSudoku(sudoku.setCell(row, col, newValue))
                                    }
                                }
                            }
                        },
                        selectedRow = selectedRow,
                        selectedCol = selectedCol,
                        highlightNumber = selectedNumber,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Remaining space for Number Pad and Tool Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            // Deselect cell when clicking empty space
                            selectedRow = null
                            selectedCol = null
                        },
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 3. Middle Area: Number Pad
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Take available space
                        contentAlignment = Alignment.Center
                    ) {
                        NumberPad(
                            selectedNumber = selectedNumber,
                            onNumberClick = { number ->
                                // Logic update: If a cell is selected (Cell First Mode), fill it and Clear selection
                                if (selectedRow != null && selectedCol != null) {
                                    val row = selectedRow!!
                                    val col = selectedCol!!
                                    val currentCell = sudoku.getCell(row, col)

                                    if (!currentCell.isFixed) {
                                        if (isNoteMode) {
                                            if (currentCell.value == 0) {
                                                updateSudoku(sudoku.toggleCandidate(row, col, number))
                                            }
                                        } else {
                                            val newValue = number
                                            if (currentCell.value != newValue) {
                                                updateSudoku(sudoku.setCell(row, col, newValue))
                                            }
                                        }
                                    }
                                    // Cell First detected: clear the number selection so user isn't stuck in "fill mode" for this number
                                    selectedNumber = null
                                } else {
                                    // Digit First Mode: Toggle number selection
                                    selectedNumber = if (selectedNumber == number) null else number
                                }
                            }
                        )
                    }

                    // 4. Tool Bar Area
                    BottomAppBar(
                        actions = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    ToolbarActionButton(
                                        onClick = { undo() },
                                        icon = Icons.AutoMirrored.Filled.Undo,
                                        contentDescription = "Undo"
                                    )
                                }
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    ToolbarActionButton(
                                        onClick = {
                                            if (selectedRow != null && selectedCol != null) {
                                                val row = selectedRow!!
                                                val col = selectedCol!!
                                                val currentCell = sudoku.getCell(row, col)

                                                if (!currentCell.isFixed) {
                                                    if (currentCell.value != 0 || currentCell.candidates.isNotEmpty()) {
                                                        updateSudoku(sudoku.setCell(row, col, 0))
                                                    }
                                                }
                                            }
                                        },
                                        icon = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Delete"
                                    )
                                }
                                // Edit Button
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    ToolbarActionButton(
                                        onClick = { isNoteMode = !isNoteMode },
                                        icon = Icons.Default.Edit,
                                        contentDescription = "Note Mode",
                                        isSelected = isNoteMode
                                    )
                                }
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    ToolbarActionButton(
                                        onClick = {
                                            updateSudoku(
                                                com.galaxyrio.sudokusolver.game.generator.CandidateCalculator.calculateAllCandidates(
                                                    sudoku
                                                )
                                            )
                                        },
                                        icon = Icons.Default.AutoAwesome,
                                        contentDescription = "Auto Candidates"
                                    )
                                }
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    ToolbarActionButton(
                                        onClick = {
                                            scope.launch {
                                                scaffoldState.bottomSheetState.expand()
                                            }
                                        },
                                        icon = Icons.Default.Lightbulb,
                                        contentDescription = "Hint"
                                    )
                                }
                            }
                        }
                    )
                }
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

    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 60.dp),
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
