package com.healthtracker.offline.ui.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Common UI state patterns and utilities for ViewModels.
 * 
 * Provides standardized state management for loading, error, and success states.
 */

/**
 * Generic UI state wrapper for async operations
 */
sealed class AsyncUiState<out T> {
    object Idle : AsyncUiState<Nothing>()
    object Loading : AsyncUiState<Nothing>()
    data class Success<T>(val data: T) : AsyncUiState<T>()
    data class Error(val exception: Throwable, val message: String? = null) : AsyncUiState<Nothing>()
    
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isIdle: Boolean get() = this is Idle
    
    fun getDataOrNull(): T? = if (this is Success) data else null
    fun getErrorOrNull(): Throwable? = if (this is Error) exception else null
}

/**
 * UI state for operations that can have loading, error, and message states
 */
data class OperationUiState(
    val isLoading: Boolean = false,
    val error: UiError? = null,
    val message: UiMessage? = null
) {
    fun withLoading(loading: Boolean = true) = copy(isLoading = loading)
    fun withError(error: UiError) = copy(isLoading = false, error = error, message = null)
    fun withMessage(message: UiMessage) = copy(isLoading = false, error = null, message = message)
    fun withSuccess(message: String? = null) = copy(
        isLoading = false, 
        error = null, 
        message = message?.let { UiMessage.Success(it) }
    )
    fun clear() = copy(isLoading = false, error = null, message = null)
}

/**
 * Standardized error representation for UI
 */
sealed class UiError {
    data class Generic(val message: String) : UiError()
    data class Network(val message: String) : UiError()
    data class Validation(val message: String, val field: String? = null) : UiError()
    data class NotFound(val message: String) : UiError()
    data class Permission(val message: String) : UiError()
    data class Storage(val message: String) : UiError()
    
    fun getMessage(): String = when (this) {
        is Generic -> message
        is Network -> message
        is Validation -> message
        is NotFound -> message
        is Permission -> message
        is Storage -> message
    }
}

/**
 * Standardized message representation for UI
 */
sealed class UiMessage {
    data class Success(val message: String) : UiMessage()
    data class Info(val message: String) : UiMessage()
    data class Warning(val message: String) : UiMessage()
    
    fun getMessage(): String = when (this) {
        is Success -> message
        is Info -> message
        is Warning -> message
    }
    
    fun getType(): MessageType = when (this) {
        is Success -> MessageType.SUCCESS
        is Info -> MessageType.INFO
        is Warning -> MessageType.WARNING
    }
}

/**
 * Message types for UI styling
 */
enum class MessageType {
    SUCCESS,
    INFO,
    WARNING
}

/**
 * Form validation state
 */
data class FormValidationState(
    val isValid: Boolean = true,
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalError: String? = null
) {
    fun hasErrors(): Boolean = !isValid || fieldErrors.isNotEmpty() || generalError != null
    
    fun getFieldError(field: String): String? = fieldErrors[field]
    
    fun withFieldError(field: String, error: String) = copy(
        isValid = false,
        fieldErrors = fieldErrors + (field to error)
    )
    
    fun withGeneralError(error: String) = copy(
        isValid = false,
        generalError = error
    )
    
    fun clearFieldError(field: String) = copy(
        fieldErrors = fieldErrors - field,
        isValid = fieldErrors.size <= 1 && generalError == null
    )
    
    fun clear() = FormValidationState()
}

/**
 * List UI state with loading, error, and empty states
 */
data class ListUiState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiError? = null,
    val isEmpty: Boolean = false,
    val hasMore: Boolean = false
) {
    fun withLoading(loading: Boolean = true) = copy(isLoading = loading, error = null)
    fun withRefreshing(refreshing: Boolean = true) = copy(isRefreshing = refreshing, error = null)
    fun withError(error: UiError) = copy(isLoading = false, isRefreshing = false, error = error)
    fun withItems(items: List<T>, hasMore: Boolean = false) = copy(
        items = items,
        isLoading = false,
        isRefreshing = false,
        error = null,
        isEmpty = items.isEmpty(),
        hasMore = hasMore
    )
}

/**
 * Search UI state
 */
data class SearchUiState<T>(
    val query: String = "",
    val results: List<T> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val error: UiError? = null,
    val suggestions: List<String> = emptyList()
) {
    fun withQuery(query: String) = copy(query = query, hasSearched = false)
    fun withSearching(searching: Boolean = true) = copy(isSearching = searching, error = null)
    fun withResults(results: List<T>) = copy(
        results = results,
        isSearching = false,
        hasSearched = true,
        error = null
    )
    fun withError(error: UiError) = copy(
        isSearching = false,
        hasSearched = true,
        error = error
    )
    fun withSuggestions(suggestions: List<String>) = copy(suggestions = suggestions)
    fun clear() = SearchUiState<T>()
}

/**
 * Helper class for managing UI state in ViewModels
 */
class UiStateManager<T> {
    private val _state = MutableStateFlow<AsyncUiState<T>>(AsyncUiState.Idle)
    val state: StateFlow<AsyncUiState<T>> = _state.asStateFlow()
    
    fun setLoading() {
        _state.value = AsyncUiState.Loading
    }
    
    fun setSuccess(data: T) {
        _state.value = AsyncUiState.Success(data)
    }
    
    fun setError(exception: Throwable, message: String? = null) {
        _state.value = AsyncUiState.Error(exception, message)
    }
    
    fun setIdle() {
        _state.value = AsyncUiState.Idle
    }
    
    suspend fun <R> executeAsync(
        operation: suspend () -> R,
        onSuccess: (R) -> T
    ) {
        setLoading()
        try {
            val result = operation()
            setSuccess(onSuccess(result))
        } catch (e: Exception) {
            setError(e, e.message)
        }
    }
}

/**
 * Extension functions for Result handling in ViewModels
 */
fun <T> Result<T>.toUiState(): AsyncUiState<T> {
    return fold(
        onSuccess = { AsyncUiState.Success(it) },
        onFailure = { AsyncUiState.Error(it, it.message) }
    )
}

fun <T> Result<T>.toOperationState(successMessage: String? = null): OperationUiState {
    return fold(
        onSuccess = { 
            OperationUiState().withSuccess(successMessage)
        },
        onFailure = { 
            OperationUiState().withError(UiError.Generic(it.message ?: "Operation failed"))
        }
    )
}

/**
 * Utility for converting exceptions to UI errors
 */
fun Throwable.toUiError(): UiError {
    return when (this) {
        is java.io.IOException -> UiError.Storage(message ?: "Storage error occurred")
        is SecurityException -> UiError.Permission(message ?: "Permission denied")
        is IllegalArgumentException -> UiError.Validation(message ?: "Invalid input")
        is NoSuchElementException -> UiError.NotFound(message ?: "Item not found")
        else -> UiError.Generic(message ?: "An unexpected error occurred")
    }
}