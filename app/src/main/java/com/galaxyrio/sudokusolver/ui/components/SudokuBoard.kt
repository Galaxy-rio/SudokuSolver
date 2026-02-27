package com.galaxyrio.sudokusolver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SudokuBoard(
    board: List<List<Int?>>,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedRow: Int? = null,
    selectedCol: Int? = null
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(2.dp, MaterialTheme.colorScheme.onSurface)
    ) {
        // Overlay for 3x3 grid lines
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            repeat(3) {
                Row(modifier = Modifier.weight(1f)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(1.dp, MaterialTheme.colorScheme.onSurface)
                        )
                    }
                }
            }
        }

        // Content layer (on top to receive clicks)
        Column(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            repeat(9) { row ->
                Row(modifier = Modifier.weight(1f)) {
                    repeat(9) { col ->
                        val value = board.getOrNull(row)?.getOrNull(col)
                        val isSelected = row == selectedRow && col == selectedCol

                        SudokuCell(
                            value = value,
                            isSelected = isSelected,
                            onClick = { onCellClick(row, col) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(
                                    width = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SudokuCell(
    value: Int?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (value != null && value != 0) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SudokuBoardPreview() {
    val sampleBoard = List(9) { row ->
        List(9) { col ->
            if ((row + col) % 5 == 0) (row + col) % 9 + 1 else null
        }
    }

    SudokuBoard(
        board = sampleBoard,
        onCellClick = { _, _ -> },
        modifier = Modifier.padding(16.dp)
    )
}
