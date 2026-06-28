package com.example.dailyquotes.ui.quotes

import com.example.dailyquotes.domain.model.Quote
import com.example.dailyquotes.domain.repository.QuoteRepository
import com.example.dailyquotes.domain.util.NetworkResult
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Sets the Main dispatcher to a [StandardTestDispatcher] for the duration of
 * each test so code that launches on `Dispatchers.Main` (e.g. viewModelScope)
 * runs deterministically under [runTest].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: StandardTestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

/**
 * In-memory fake implementing the [QuoteRepository] contract. Doubles as
 * living documentation: no mocking framework, just a backing
 * [MutableStateFlow] that [observeQuotes] filters/maps over, plus a
 * preset [NetworkResult] returned by [refresh].
 */
private class FakeQuoteRepository : QuoteRepository {

    private val backing = MutableStateFlow<List<Quote>>(emptyList())

    /** What [refresh] returns; defaults to a no-op success. */
    var refreshResult: NetworkResult<Unit> = NetworkResult.Success(Unit)

    /** What [refresh] populates the backing store with on success. */
    var quotesOnRefresh: List<Quote> = emptyList()

    fun setQuotes(quotes: List<Quote>) {
        backing.value = quotes
    }

    override fun observeQuotes(favoritesOnly: Boolean) =
        backing.map { quotes -> if (favoritesOnly) quotes.filter { it.isFavorite } else quotes }

    override suspend fun refresh(): NetworkResult<Unit> {
        if (refreshResult is NetworkResult.Success) {
            backing.value = quotesOnRefresh
        }
        return refreshResult
    }

    override suspend fun setFavorite(id: String, favorite: Boolean) {
        backing.value = backing.value.map { quote ->
            if (quote.id == id) quote.copy(isFavorite = favorite) else quote
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class QuotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeQuoteRepository

    @Before
    fun setUp() {
        repository = FakeQuoteRepository()
    }

    @Test
    fun refresh_success_populatesQuotes() = runTest {
        val quotes = listOf(
            Quote(id = "1", content = "Content one", author = "Author One"),
            Quote(id = "2", content = "Content two", author = "Author Two"),
        )
        repository.refreshResult = NetworkResult.Success(Unit)
        repository.quotesOnRefresh = quotes

        val viewModel = QuotesViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.quotes).hasSize(2)
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun refresh_error_setsErrorMessage() = runTest {
        repository.refreshResult = NetworkResult.Error("boom")

        val viewModel = QuotesViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.error).isEqualTo("boom")
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun setFilter_favoritesOnly_filtersList() = runTest {
        val quotes = listOf(
            Quote(id = "1", content = "Favorited", author = "Author One", isFavorite = true),
            Quote(id = "2", content = "Not favorited", author = "Author Two", isFavorite = false),
        )
        repository.refreshResult = NetworkResult.Success(Unit)
        repository.quotesOnRefresh = quotes

        val viewModel = QuotesViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.setFilter(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.favoritesOnly).isTrue()
        assertThat(state.quotes).isNotEmpty()
        assertThat(state.quotes.all { it.isFavorite }).isTrue()
    }
}
