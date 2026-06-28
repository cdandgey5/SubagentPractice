package com.example.dailyquotes.di

import android.content.Context
import androidx.room.Room
import com.example.dailyquotes.data.local.QuoteDao
import com.example.dailyquotes.data.local.QuoteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides Room database/DAO instances.
 *
 * Does NOT bind [com.example.dailyquotes.domain.repository.QuoteRepository] —
 * that binding lives in the network engineer's module alongside
 * `QuoteRepositoryImpl`, which combines this DAO with the remote data source.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    // v1 schema: no migrations exist yet, so fall back to destructive
    // migration for now. Replace with explicit Migration objects before
    // shipping any schema change beyond v1.
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuoteDatabase =
        Room.databaseBuilder(context, QuoteDatabase::class.java, "quotes.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideQuoteDao(db: QuoteDatabase): QuoteDao = db.quoteDao()
}
