package com.example.dailyquotes.domain.model

/**
 * Domain model representing a single quote.
 *
 * Pure Kotlin: no Android, Room, or networking imports. This is the type that
 * flows through the UI layer; DTOs and Room entities are mapped to/from it at
 * the data-layer boundaries.
 */
data class Quote(
    val id: String,
    val content: String,
    val author: String,
    val isFavorite: Boolean = false,
)
