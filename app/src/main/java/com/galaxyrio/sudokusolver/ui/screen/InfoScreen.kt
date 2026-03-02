package com.galaxyrio.sudokusolver.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import Sudoku.*

@Composable
fun InfoScreen(modifier: Modifier = Modifier) {
    val generator = SudokuGenerator(scala.Option.apply(null))
    val str = generator.generate(generator.`generate$default$1`()).serialize('.')
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Info Screen")
        Text(str)
    }
}

