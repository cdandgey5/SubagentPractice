package com.example.dailyquotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * App database, v1.
 *
 * Migration strategy note: this is the initial schema (version = 1), so there
 * is no migration path yet. [com.example.dailyquotes.di.DataModule] wires this
 * database with `fallbackToDestructiveMigration()` for v1 only — once this
 * ships and the schema needs to change, replace that with an explicit
 * `Migration` and bump `version`, since destructive migration would otherwise
 * silently drop users' cached favorites.
 */
@Database(entities = [QuoteEntity::class], version = 1, exportSchema = false)
abstract class QuoteDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
}
