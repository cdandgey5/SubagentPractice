package com.example.dailyquotes.data.local

import com.example.dailyquotes.domain.model.Quote

/** Maps a Room entity to the pure-Kotlin domain model. */
fun QuoteEntity.toDomain(): Quote = Quote(
    id = id,
    content = content,
    author = author,
    isFavorite = isFavorite,
)

/** Maps a domain model to the Room entity persisted locally. */
fun Quote.toEntity(): QuoteEntity = QuoteEntity(
    id = id,
    content = content,
    author = author,
    isFavorite = isFavorite,
)
