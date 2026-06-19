package ec.edu.puce.barrioseguro.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.edu.puce.barrioseguro.domain.model.Incidente
import ec.edu.puce.barrioseguro.domain.repository.IncidenteRepository
import ec.edu.puce.barrioseseguro.presentation.common.IncidenteUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalleViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: IncidenteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<IncidenteUiState<Incidente>>(IncidenteUiState.Loading)
    val uiState: StateFlow<IncidenteUiState<Incidente>> = _uiState.asStateFlow()

    init {
        cargarDetalle()
    }

    fun cargarDetalle() {
        val incidenteId = savedStateHandle.get<Int>("incidenteId")
        if (incidenteId == null) {
            _uiState.value = IncidenteUiState.Error("ID de incidente no proporcionado")
            return
        }

        viewModelScope.launch {
            _uiState.value = IncidenteUiState.Loading
            try {
                val incidente = repository.obtenerIncidentePorId(incidenteId)
                if (incidente != null) {
                    _uiState.value = IncidenteUiState.Success(incidente)
                } else {
                    _uiState.value = IncidenteUiState.Error("Incidente #$incidenteId no encontrado")
                }
            } catch (e: Exception) {
                _uiState.value = IncidenteUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
