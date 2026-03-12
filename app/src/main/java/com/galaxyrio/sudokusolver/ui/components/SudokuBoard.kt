package com.galaxyrio.sudokusolver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import com.galaxyrio.sudokusolver.game.serializer.SudokuSerializer
import com.galaxyrio.sudokusolver.game.validator.SudokuValidator
import libsudoku.wrapping.Cell
import libsudoku.wrapping.Sudoku
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SudokuBoardState(
    @Serializable(with = SudokuSerializer::class)
    val sudoku: Sudoku,
    val cells: List<SudokuCellState>,
    @Transient val selectedRow: Int? = null,
    @Transient val selectedCol: Int? = null,
    @Transient val selectedNumber: Int? = null,
) {
    constructor(sudoku: Sudoku): this(
        sudoku = sudoku,
        cells = List(81) {
            val row = it / 9
            val col = it % 9
            val cell = sudoku.board()[row][col]
            SudokuCellState(
                isProvided = cell.isFixed,
                value = cell.number(),
                candidates = if (cell.isFixed) emptySet() else cell.candidates(),
            )
        }
    )

    fun selectPosition(selectedRow: Int, selectedCol: Int) = this.copy(
        cells = List(81) {
            val row = it / 9
            val col = it % 9
            val oldState = this.cells[it]
            oldState.copy(
                isSelected = (row == selectedRow && col == selectedCol),
                isSelectedCross = (row == selectedRow || col == selectedCol),
                isSelectedBox = (row / 3 == selectedRow / 3 && col / 3 == selectedCol / 3),
            )
        },
        selectedRow = selectedRow,
        selectedCol = selectedCol,
    )

    fun unselectPosition() = this.copy(
        cells = List(81) {
            val row = it / 9
            val col = it % 9
            val oldState = this.cells[it]
            oldState.copy(
                isSelected = false,
                isSelectedCross = false,
                isSelectedBox = false,
            )
        },
        selectedRow = null,
        selectedCol = null,
    )

    fun toggleSelectPosition(selectedRow: Int, selectedCol: Int) =
        if (selectedRow == this.selectedRow && selectedCol == this.selectedCol) unselectPosition()
        else selectPosition(selectedRow, selectedCol)

    fun selectNumber(selectedNumber: Int) = this.copy(
        cells = List(81) {
            val row = it / 9
            val col = it % 9
            val oldState = this.cells[it]
            oldState.copy(
                highlightNumber = selectedNumber,
            )
        },
        selectedNumber = selectedNumber,
    )

    fun unselectNumber() = this.copy(
        cells = List(81) {
            val row = it / 9
            val col = it % 9
            val oldState = this.cells[it]
            oldState.copy(
                highlightNumber = null,
            )
        },
        selectedNumber = null,
    )

    fun toggleSelectNumber(selectedNumber: Int) =
        if (selectedNumber == this.selectedNumber) unselectNumber()
        else selectNumber(selectedNumber)

    fun updateSudoku(sudoku: Sudoku) = this.copy(
        sudoku = sudoku,
        cells = List(81) {
            val row = it / 9
            val col = it % 9
            val oldState = this.cells[it]
            val cell = sudoku.board()[row][col]
            oldState.copy(
                value = cell.number(),
                candidates = if (cell.isFixed) emptySet() else cell.candidates(),
                isError = SudokuValidator.checkContradiction(sudoku, row, col),
                errorCandidates = if (cell.isFixed) emptySet() else
                    cell.candidates().filter { SudokuValidator.checkContradiction(sudoku, row, col, it) }.toSet()
            )
        }
    )
}

@Serializable
data class SudokuCellState(
    val isProvided: Boolean,
    val value: Int?,
    val candidates: Set<Int>,
    val isError: Boolean = false,
    val errorCandidates: Set<Int> = emptySet(),
    @Transient val highlightNumber: Int? = null,
    @Transient val isSelected: Boolean = false,
    @Transient val isSelectedCross: Boolean = false,
    @Transient val isSelectedBox: Boolean = false,
)

@Composable
fun SudokuBoard(
    sudokuBoardState: SudokuBoardState,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val thickLine = 2.dp
    val thinLine = 1.dp
    val cornerRadius = 8.dp
    val boardColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(boardColor, RoundedCornerShape(cornerRadius))
            .border(thickLine, boardColor, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .padding(thickLine)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(thickLine)
        ) {
            repeat(3) { blockRow ->
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(thickLine)
                ) {
                    repeat(3) { blockCol ->
                        // Sudoku Block (3x3 Cells)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(boardColor), // Background for thin lines
                            verticalArrangement = Arrangement.spacedBy(thinLine)
                        ) {
                            repeat(3) { cellRowInBlock ->
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(thinLine)
                                ) {
                                    repeat(3) { cellColInBlock ->
                                        val row = blockRow * 3 + cellRowInBlock
                                        val col = blockCol * 3 + cellColInBlock
                                        SudokuCell(
                                            sudokuBoardState.cells[row * 9 + col],
                                            onClick = { onCellClick(row, col) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SudokuCell(
    sudokuCellState: SudokuCellState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (isProvided, value, candidates, isError, errorCandidates, highlightNumber, isSelected, isSelectedCross, isSelectedBox) = sudokuCellState
    val isFixedNumberHighlighted = highlightNumber != null && value != null && highlightNumber == value
    val backgroundColor = when {
        isError && isSelected -> MaterialTheme.colorScheme.error
        isError -> MaterialTheme.colorScheme.errorContainer
        isSelected && isProvided -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isSelectedCross && !isError -> MaterialTheme.colorScheme.surfaceContainerHighest
        isSelectedBox && !isError -> MaterialTheme.colorScheme.surfaceContainer
        isFixedNumberHighlighted && !isError -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        isError && isSelected -> MaterialTheme.colorScheme.onError
        isError -> MaterialTheme.colorScheme.onErrorContainer
        isSelected && isProvided -> MaterialTheme.colorScheme.onPrimary
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        isProvided -> MaterialTheme.colorScheme.onSurface
        isFixedNumberHighlighted -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (value != null) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isProvided) FontWeight.Bold else FontWeight.Bold, // 改为Normal可以增加对比度
                    fontSize = 30.sp
                ),
                color = textColor
            )
        } else if (candidates.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(1.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(3) { rowOffset ->
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(3) { colOffset ->
                            val candidateNum = rowOffset * 3 + colOffset + 1
                            val isCandidateHighlighted = candidateNum == highlightNumber
                            val isErrorCandidate = errorCandidates.contains(candidateNum)
                            val candidateBackgroundColor = when {
                                isErrorCandidate -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            }
                            val candidateColor = when {
                                errorCandidates.contains(candidateNum) -> MaterialTheme.colorScheme.error
                                isCandidateHighlighted -> MaterialTheme.colorScheme.onSecondaryContainer
                                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.secondary
                            }

                            if (candidates.contains(candidateNum)) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .then(
                                            if (isCandidateHighlighted)
                                                Modifier.background(
                                                    candidateBackgroundColor,
                                                    androidx.compose.foundation.shape.CircleShape
                                                )
                                            else Modifier
                                        )
                                ) {
                                    Text(
                                        text = candidateNum.toString(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 10.sp,
                                            lineHeight = 10.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = candidateColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            assert(false)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SudokuBoardPreview() {
    val sampleSudoku = Sudoku(
        List(9) { r ->
            List(9) { c ->
                val i = r * 9 + c
                if (i % 5 == 0) Cell.Fixed((i % 9) + 1) else Cell.Notes(*(1..9).toSet().toTypedArray())
            }
        }
    )

    SudokuBoard(
        sudokuBoardState = SudokuBoardState(sampleSudoku),
        onCellClick = { _, _ -> },
        modifier = Modifier.padding(16.dp)
    )
}
