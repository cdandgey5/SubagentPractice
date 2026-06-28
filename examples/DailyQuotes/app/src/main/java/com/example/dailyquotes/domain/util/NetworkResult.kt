package com.example.dailyquotes.domain.util

/**
 * Pure-Kotlin result wrapper for operations that may fail (typically network
 * calls). Kept free of Android and Retrofit imports so the domain layer stays
 * clean; the data layer is responsible for catching IOException/HttpException
 * and translating them into [Error] before they cross this boundary.
 */
sealed interface NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>
}
