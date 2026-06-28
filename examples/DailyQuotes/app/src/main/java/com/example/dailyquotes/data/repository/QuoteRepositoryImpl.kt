package com.example.dailyquotes.data.repository

import com.example.dailyquotes.data.local.QuoteDao
import com.example.dailyquotes.data.local.toDomain
import com.example.dailyquotes.data.local.toEntity
import com.example.dailyquotes.data.remote.QuoteRemoteDataSource
import com.example.dailyquotes.domain.model.Quote
import com.example.dailyquotes.domain.repository.QuoteRepository
import com.example.dailyquotes.domain.util.NetworkResult
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Offline-first: Room is the single source of truth. The UI observes the DB;
// refresh() pulls the network and caches into the DB, so the Flow re-emits.
class QuoteRepositoryImpl @Inject constructor(
    private val dao: QuoteDao,
    private val remote: QuoteRemoteDataSource,
) : QuoteRepository {

    override fun observeQuotes(favoritesOnly: Boolean): Flow<List<Quote>> =
        (if (favoritesOnly) dao.observeFavorites() else dao.observeAll())
            .map { list -> list.map { it.toDomain() } }

    override suspend fun refresh(): NetworkResult<Unit> =
        when (val result = remote.fetchQuotes()) {
            is NetworkResult.Success -> {
                // INSERT-IGNORE in the DAO preserves any locally-set favorites.
                dao.insertAll(result.data.map { it.toEntity() })
                NetworkResult.Success(Unit)
            }
            is NetworkResult.Error -> result
        }

    override suspend fun setFavorite(id: String, favorite: Boolean) =
        dao.setFavorite(id, favorite)
}
