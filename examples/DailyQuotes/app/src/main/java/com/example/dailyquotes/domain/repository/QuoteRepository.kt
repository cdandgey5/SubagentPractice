package com.example.dailyquotes.domain.repository

import com.example.dailyquotes.domain.model.Quote
import com.example.dailyquotes.domain.util.NetworkResult
import kotlinx.coroutines.flow.Flow

/**
 * Contract for accessing quotes. Offline-first: [observeQuotes] reflects the
 * local source of truth (Room) and emits on every change; [refresh] pulls from
 * the remote API and upserts locally; [setFavorite] persists a favorite toggle.
 *
 * Implemented in the data layer (QuoteRepositoryImpl); consumed by the UI layer
 * (QuotesViewModel). This interface is the seam both sides build against.
 */
interface QuoteRepository {

    /** Reactive stream of quotes; pass true to observe only favorites. */
    fun observeQuotes(favoritesOnly: Boolean): Flow<List<Quote>>

    /** Fetch latest quotes from remote and upsert into the local store. */
    suspend fun refresh(): NetworkResult<Unit>

    /** Persist the favorite state for the quote with the given id. */
    suspend fun setFavorite(id: String, favorite: Boolean)
}
