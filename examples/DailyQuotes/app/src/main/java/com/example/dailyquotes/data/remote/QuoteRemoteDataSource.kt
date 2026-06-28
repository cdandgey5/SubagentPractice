package com.example.dailyquotes.data.remote

import com.example.dailyquotes.data.remote.api.QuotableApi
import com.example.dailyquotes.data.remote.dto.toDomain
import com.example.dailyquotes.domain.model.Quote
import com.example.dailyquotes.domain.util.NetworkResult
import java.io.IOException
import javax.inject.Inject
import retrofit2.HttpException

// Thin wrapper over the API that centralizes error handling and returns
// domain-friendly results. The repository uses this, not the API directly.
class QuoteRemoteDataSource @Inject constructor(
    private val api: QuotableApi,
) {
    suspend fun fetchQuotes(): NetworkResult<List<Quote>> = safeCall {
        api.getRandomQuotes().map { it.toDomain() }
    }

    private inline fun <T> safeCall(block: () -> T): NetworkResult<T> = try {
        NetworkResult.Success(block())
    } catch (e: HttpException) {
        NetworkResult.Error(e.message ?: "HTTP error", e.code())
    } catch (e: IOException) {
        NetworkResult.Error("No network connection")
    }
}
