package com.galaxyrio.sudokusolver.ui.screen.play

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
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
import com.galaxyrio.sudokusolver.ui.components.GameToolbar
import com.galaxyrio.sudokusolver.ui.components.GameTopBar
import com.galaxyrio.sudokusolver.ui.components.NumberPad
import com.galaxyrio.sudokusolver.ui.components.SudokuBoard
import com.galaxyrio.sudokusolver.ui.screen.Difficulty
import kotlinx.coroutines.launch
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import com.galaxyrio.sudokusolver.game.validator.SudokuValidator

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SudokuGameScreen(
    difficulty: Difficulty,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    // Initialize the board using the generator
    var sudoku by remember(difficulty) {
        val clues = when (difficulty) {
            Difficulty.EASY -> 40
            Difficulty.MEDIUM -> 30
            Difficulty.HARD -> 24
        }
        mutableStateOf(SudokuGenerator().generate(clues))
    }

    var selectedRow by remember { mutableStateOf<Int?>(null) }
    var selectedCol by remember { mutableStateOf<Int?>(null) }
    var selectedNumber by remember { mutableStateOf<Int?>(null) }
    var isNoteMode by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(false) }

    // History for Undo
    val history = remember { mutableListOf<Sudoku>() }

    // Hint state
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    fun updateSudoku(newSudoku: Sudoku) {
        history.add(sudoku)
        sudoku = newSudoku

        // Check win condition
        val isFull = newSudoku.cells.none { it.value == 0 }
        if (isFull && SudokuValidator.isBoardValid(newSudoku)) {
             showWinDialog = true
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
                    .height(300.dp)
            )
        },
        sheetPeekHeight = 0.dp,
        // Make the scrim transparent to allow interaction with the content behind if desired?
        // Standard bottom sheet usually pushes content up or overlays without scrim blocking interaction if not modal.
        // However, BottomSheetScaffold creates a persistent bottom sheet.
        // If we want it to behave like a standard sheet that doesn't block interaction, we don't strictly need to do anything special,
        // but default implementation might have a scrim or not depending on Material3 version.
        // Actually BottomSheetScaffold in M3 doesn't have a scrim by default for standard sheets, it just overlays.
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
            val screenHeight = maxHeight
            val screenWidth = maxWidth

            val topBarHeight = screenHeight / 8
            val boardHeight = screenWidth-32.dp

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Top Bar Area
                GameTopBar(
                    title = "Sudoku - ${difficulty.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(topBarHeight)
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                )

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
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
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
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GameToolbar(
                            isNoteMode = isNoteMode,
                            onUndoClick = { undo() },
                            onDeleteClick = {
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
                            onNoteModeClick = { isNoteMode = !isNoteMode },
                            onAutoCandidatesClick = {
                                updateSudoku(
                                    com.galaxyrio.sudokusolver.game.generator.CandidateCalculator.calculateAllCandidates(sudoku)
                                )
                            },
                            onHintClick = {
                                scope.launch {
                                    scaffoldState.bottomSheetState.expand()
                                }
                            }
                        )

                        // Bottom spacing + Gesture inset
                        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                        Spacer(modifier = Modifier.height(24.dp + bottomPadding))
                    }
                }
            }

        }
    }
}
