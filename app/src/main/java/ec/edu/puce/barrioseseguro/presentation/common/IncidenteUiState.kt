package ec.edu.puce.barrioseseguro.presentation.common

sealed class IncidenteUiState<out T> {
    data object Loading : IncidenteUiState<Nothing>()
    data class Success<T>(val data: T) : IncidenteUiState<T>()
    data class Error(val message: String) : IncidenteUiState<Nothing>()
}
