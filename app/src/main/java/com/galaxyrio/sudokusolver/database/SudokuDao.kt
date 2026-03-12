package com.galaxyrio.sudokusolver.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import libsudoku.wrapping.SudokuGenerator.Difficulty
import kotlinx.coroutines.flow.Flow

@Dao
interface SudokuDao {
    @Query("SELECT * FROM games ORDER BY lastPlayed DESC")
    fun getAllGames(): Flow<List<SudokuEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGame(id: Long): SudokuEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGame(game: SudokuEntity): Long

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGame(id: Long)
}
