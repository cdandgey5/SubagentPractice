package com.example.dailyquotes.ui.quotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dailyquotes.domain.model.Quote
import com.example.dailyquotes.ui.theme.DailyQuotesTheme

@Composable
fun QuotesScreen(
    viewModel: QuotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    QuotesContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        onToggleFavorite = viewModel::toggleFavorite,
        onFilterChange = viewModel::setFilter,
    )
}

// Stateless + hoisted so it is previewable and testable.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuotesContent(
    uiState: QuotesUiState,
    onRefresh: () -> Unit,
    onToggleFavorite: (Quote) -> Unit,
    onFilterChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Daily Quotes") },
                actions = {
                    TextButton(onClick = onRefresh) {
                        Text("Refresh")
                    }
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = !uiState.favoritesOnly,
                    onClick = { onFilterChange(false) },
                    label = { Text("All") },
                )
                FilterChip(
                    selected = uiState.favoritesOnly,
                    onClick = { onFilterChange(true) },
                    label = { Text("Favorites") },
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    uiState.isLoading -> CircularProgressIndicator()
                    uiState.error != null -> Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                    )
                    uiState.quotes.isEmpty() -> Text("No quotes yet")
                    else -> QuotesList(
                        quotes = uiState.quotes,
                        onToggleFavorite = onToggleFavorite,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuotesList(
    quotes: List<Quote>,
    onToggleFavorite: (Quote) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(quotes, key = { it.id }) { quote ->
            QuoteRow(quote = quote, onToggleFavorite = onToggleFavorite)
        }
    }
}

@Composable
private fun QuoteRow(
    quote: Quote,
    onToggleFavorite: (Quote) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quote.content,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            IconButton(onClick = { onToggleFavorite(quote) }) {
                Text(
                    text = if (quote.isFavorite) "♥" else "♡",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (quote.isFavorite) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuotesContentPreview() {
    DailyQuotesTheme {
        QuotesContent(
            uiState = QuotesUiState(
                isLoading = false,
                quotes = listOf(
                    Quote(
                        id = "1",
                        content = "The only way to do great work is to love what you do.",
                        author = "Steve Jobs",
                        isFavorite = true,
                    ),
                    Quote(
                        id = "2",
                        content = "Life is what happens when you're busy making other plans.",
                        author = "John Lennon",
                        isFavorite = false,
                    ),
                ),
                favoritesOnly = false,
                error = null,
            ),
            onRefresh = {},
            onToggleFavorite = {},
            onFilterChange = {},
        )
    }
}
