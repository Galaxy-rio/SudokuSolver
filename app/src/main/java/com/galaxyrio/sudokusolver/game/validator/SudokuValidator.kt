package com.galaxyrio.sudokusolver.game.validator

import libsudoku.wrapping.Sudoku

object SudokuValidator {
    /**
     * Checks if placing a value at the given row and column would cause any inconsistency
     */
    fun checkContradiction(sudoku: Sudoku, row: Int, col: Int, value: Int): Boolean {
        for ((r, c) in Sudoku.peersOf(row, col)) {
            val cell = sudoku.board()[r][c]
            if (cell.number() == value) {
                return true
            }
        }
        return false
    }

    /**
     * Checks if the value at the given row and column causes any inconsistency
     */
    fun checkContradiction(sudoku: Sudoku, row: Int, col: Int): Boolean {
        val value = sudoku.board()[row][col].number()
        return value ?.let { checkContradiction(sudoku, row, col, value) } ?: false
    }
}
