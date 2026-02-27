package com.galaxyrio.sudokusolver.ui.screen.play

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.galaxyrio.sudokusolver.ui.components.SudokuBoard
import com.galaxyrio.sudokusolver.ui.components.NumberPad
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.width
import com.galaxyrio.sudokusolver.ui.screen.Difficulty

@Composable
fun SudokuGameScreen(
    difficulty: Difficulty,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Basic state for the board, initialized with nulls (empty cells)
    // In a real app, this would come from a ViewModel and SudokuGenerator
    var board by remember { mutableStateOf(List(9) { List<Int?>(9) { null } }) }
    var selectedRow by remember { mutableStateOf<Int?>(null) }
    var selectedCol by remember { mutableStateOf<Int?>(null) }
    var selectedNumber by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Sudoku Game - ${difficulty.name}",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            SudokuBoard(
                board = board,
                onCellClick = { row, col ->
                    selectedRow = row
                    selectedCol = col

                    selectedNumber?.let { number ->
                        val newRow = board[row].toMutableList()
                        newRow[col] = number
                        val newBoard = board.toMutableList()
                        newBoard[row] = newRow
                        board = newBoard
                    }
                },
                selectedRow = selectedRow,
                selectedCol = selectedCol,
                modifier = Modifier
                    .width(300.dp)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            NumberPad(
                selectedNumber = selectedNumber,
                onNumberClick = { number ->
                    selectedNumber = if (selectedNumber == number) null else number
                },
                modifier = Modifier
                    .width(300.dp)
                    .padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onBack) {
                Text("Back to Menu")
            }
        }
    }
}
