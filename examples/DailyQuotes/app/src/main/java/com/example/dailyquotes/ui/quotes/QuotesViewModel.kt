package com.example.dailyquotes.ui.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyquotes.domain.model.Quote
import com.example.dailyquotes.domain.repository.QuoteRepository
import com.example.dailyquotes.domain.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI state for [QuotesScreen]: loading / content / filter / error, combined. */
data class QuotesUiState(
    val isLoading: Boolean = false,
    val quotes: List<Quote> = emptyList(),
    val favoritesOnly: Boolean = false,
    val error: String? = null,
)

/**
 * Depends only on [QuoteRepository] (domain interface) — never on Room/Retrofit
 * directly. Observes quotes reactively via [QuoteRepository.observeQuotes] and
 * layers in loading/error state driven by explicit [refresh] calls.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class QuotesViewModel @Inject constructor(
    private val repository: QuoteRepository,
) : ViewModel() {

    private val favoritesOnly = MutableStateFlow(false)
    private val isLoading = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<QuotesUiState> = combine(
        favoritesOnly.flatMapLatest { repository.observeQuotes(it) }
            .catch { e -> error.update { e.message ?: "Something went wrong" } },
        favoritesOnly,
        isLoading,
        error,
    ) { quotes, filter, loading, err ->
        QuotesUiState(
            isLoading = loading,
            quotes = quotes,
            favoritesOnly = filter,
            error = err,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = QuotesUiState(isLoading = true),
    )

    init {
        refresh()
    }

    fun setFilter(favoritesOnly: Boolean) {
        this.favoritesOnly.value = favoritesOnly
    }

    fun toggleFavorite(quote: Quote) {
        viewModelScope.launch {
            repository.setFavorite(quote.id, !quote.isFavorite)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading.value = true
            when (val result = repository.refresh()) {
                is NetworkResult.Error -> error.value = result.message
                is NetworkResult.Success -> error.value = null
            }
            isLoading.value = false
        }
    }
}
