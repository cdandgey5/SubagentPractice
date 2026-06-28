package com.example.dailyquotes.data.remote.api

import com.example.dailyquotes.data.remote.dto.QuoteDto
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit service for api.quotable.io. suspend functions (native Retrofit), never Call<T>.
interface QuotableApi {
    @GET("quotes/random")
    suspend fun getRandomQuotes(@Query("limit") limit: Int = 20): List<QuoteDto>
}
