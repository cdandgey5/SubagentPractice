package com.example.dailyquotes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for the `quotes` table. Reads observed by the UI are
 * exposed as [Flow]; writes are `suspend fun`s and main-safe via Room.
 */
@Dao
interface QuoteDao {

    @Query("SELECT * FROM quotes ORDER BY author")
    fun observeAll(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE isFavorite = 1 ORDER BY author")
    fun observeFavorites(): Flow<List<QuoteEntity>>

    // IGNORE so existing favorites aren't clobbered on refresh.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(quotes: List<QuoteEntity>)

    @Query("UPDATE quotes SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)
}
