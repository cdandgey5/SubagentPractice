package com.example.dailyquotes.di

import com.example.dailyquotes.data.repository.QuoteRepositoryImpl
import com.example.dailyquotes.domain.repository.QuoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Binds the offline-first implementation to the domain interface, so the UI's
// ViewModel (which injects QuoteRepository) gets QuoteRepositoryImpl at runtime.
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindQuoteRepository(impl: QuoteRepositoryImpl): QuoteRepository
}
