package com.galaxyrio.sudokusolver.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.galaxyrio.sudokusolver.game.Sudoku
import com.galaxyrio.sudokusolver.ui.screen.Difficulty

@Entity(tableName = "games")
data class SudokuEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val difficulty: Difficulty,
    val sudoku: Sudoku,
    val timeSpent: Long = 0, // Time spent in seconds
    val lastPlayed: Long = System.currentTimeMillis()
)

