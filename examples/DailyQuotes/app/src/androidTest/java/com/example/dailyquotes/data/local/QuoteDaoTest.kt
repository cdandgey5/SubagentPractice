package com.example.dailyquotes.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [QuoteDao] against a real (in-memory) Room database,
 * covering insert/observe/update behavior and the conflict strategy that
 * protects locally-set favorites from being clobbered on refresh.
 */
@RunWith(AndroidJUnit4::class)
class QuoteDaoTest {

    private lateinit var database: QuoteDatabase
    private lateinit var dao: QuoteDao

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            QuoteDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.quoteDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertAll_then_observeAll_returnsInserted() = runBlocking {
        val quotes = listOf(
            QuoteEntity(id = "1", content = "Content one", author = "Author One", isFavorite = false),
            QuoteEntity(id = "2", content = "Content two", author = "Author Two", isFavorite = false),
        )

        dao.insertAll(quotes)
        val result = dao.observeAll().first()

        assertThat(result).hasSize(2)
        assertThat(result.map { it.id }).containsExactly("1", "2")
    }

    @Test
    fun setFavorite_marksRowFavorite_and_observeFavorites_returnsIt() = runBlocking {
        val quote = QuoteEntity(id = "1", content = "Content", author = "Author", isFavorite = false)
        dao.insertAll(listOf(quote))

        dao.setFavorite(id = "1", favorite = true)
        val favorites = dao.observeFavorites().first()

        assertThat(favorites).hasSize(1)
        assertThat(favorites.first().id).isEqualTo("1")
        assertThat(favorites.first().isFavorite).isTrue()
    }

    @Test
    fun insertAll_ignoresConflict_preservesFavorite() = runBlocking {
        val quote = QuoteEntity(id = "1", content = "Content", author = "Author", isFavorite = false)
        dao.insertAll(listOf(quote))
        dao.setFavorite(id = "1", favorite = true)

        // Simulate a refresh re-fetching the same quote as not-favorited.
        val refreshed = QuoteEntity(id = "1", content = "Content", author = "Author", isFavorite = false)
        dao.insertAll(listOf(refreshed))

        val favorites = dao.observeFavorites().first()

        assertThat(favorites).hasSize(1)
        assertThat(favorites.first().id).isEqualTo("1")
        assertThat(favorites.first().isFavorite).isTrue()
    }
}
