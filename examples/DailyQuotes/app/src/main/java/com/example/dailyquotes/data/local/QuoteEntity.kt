package com.example.dailyquotes.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a persisted quote. Lives entirely in the data layer; never
 * exposed above the repository. Mapped to/from [com.example.dailyquotes.domain.model.Quote]
 * via the functions in QuoteMappers.kt.
 */
@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey val id: String,
    val content: String,
    val author: String,
    val isFavorite: Boolean,
)
