package com.example.dailyquotes.data.remote.dto

import com.example.dailyquotes.domain.model.Quote
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Network DTO: mirrors the api.quotable.io JSON shape ONLY.
// Distinct from QuoteEntity (Room) and Quote (domain) — mapped at the boundary.
@Serializable
data class QuoteDto(
    @SerialName("_id") val id: String,
    @SerialName("content") val content: String,
    @SerialName("author") val author: String,
)

// Map at the boundary so a DTO never reaches a ViewModel.
// New network quotes default to not-favorited; local favorites are preserved by
// the DAO's INSERT-IGNORE on refresh.
fun QuoteDto.toDomain(): Quote = Quote(
    id = id,
    content = content,
    author = author,
    isFavorite = false,
)
