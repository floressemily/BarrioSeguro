package ec.edu.puce.barrioseguro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.edu.puce.barrioseguro.domain.model.Incidente
import ec.edu.puce.barrioseguro.domain.repository.IncidenteRepository
import ec.edu.puce.barrioseseguro.presentation.common.IncidenteUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class IncidenteViewModel(
    private val repository: IncidenteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<IncidenteUiState<List<Incidente>>>(IncidenteUiState.Loading)
    val uiState: StateFlow<IncidenteUiState<List<Incidente>>> = _uiState.asStateFlow()

    fun cargarIncidentes() {
        viewModelScope.launch {
            _uiState.value = IncidenteUiState.Loading
            repository.observarIncidentes()
                .catch { e ->
                    _uiState.value = IncidenteUiState.Error(e.message ?: "Error desconocido")
                }
                .collect { incidentes ->
                    _uiState.value = IncidenteUiState.Success(incidentes)
                }
        }
    }

    fun guardarIncidente(incidente: Incidente) {
        viewModelScope.launch {
            try {
                repository.guardarIncidente(incidente)
            } catch (e: Exception) {
                // Se podría manejar el error aquí si es necesario
            }
        }
    }
}
