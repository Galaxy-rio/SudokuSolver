package com.galaxyrio.sudokusolver.database

import androidx.room.TypeConverter
import com.galaxyrio.sudokusolver.ui.components.SudokuBoardState
import libsudoku.wrapping.SudokuGenerator.Difficulty
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun fromSudokuBoardState(sudokuBoardState: SudokuBoardState): String =
        Json.encodeToString(sudokuBoardState)

    @TypeConverter
    fun toSudokuBoardState(data: String): SudokuBoardState =
        Json.decodeFromString(data)

    @TypeConverter
    fun fromDifficulty(difficulty: Difficulty): String =
        difficulty.name

    @TypeConverter
    fun toDifficulty(data: String): Difficulty =
        try {
            Difficulty.valueOf(data)
        } catch (e: Exception) {
            Difficulty.EASY
        }
}
