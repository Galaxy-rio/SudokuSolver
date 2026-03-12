package com.galaxyrio.sudokusolver.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.galaxyrio.sudokusolver.ui.components.SudokuBoardState
import libsudoku.wrapping.SudokuGenerator.Difficulty

@Entity(tableName = "games")
data class SudokuEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val difficulty: Difficulty,
    val sudokuBoardState: SudokuBoardState,
    val timeSpent: Long = 0, // Time spent in seconds
    val lastPlayed: Long = System.currentTimeMillis()
)
