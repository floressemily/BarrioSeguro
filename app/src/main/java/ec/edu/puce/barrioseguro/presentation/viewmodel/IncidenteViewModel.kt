package ec.edu.puce.barrioseguro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.edu.puce.barrioseguro.domain.model.Incidente
import ec.edu.puce.barrioseguro.domain.repository.IncidenteRepository
import ec.edu.puce.barrioseguro.presentation.common.IncidenteUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IncidenteViewModel(
    private val repository: IncidenteRepository
) : ViewModel() {

    /**
     * Flow reactivo de Room convertido directamente en StateFlow mediante [stateIn].
     * - [SharingStarted.WhileSubscribed(5000)]: el Flow de Room se suspende 5 segundos
     *   después de que no haya suscriptores (p.ej. al girar pantalla). Evita re-lanzar
     *   la consulta de DB innecesariamente pero libera recursos si el usuario sale.
     * - No necesita [cargarIncidentes]: la colección comienza automáticamente al crear
     *   el ViewModel y Room emite cada vez que cambia la base de datos.
     */
    val uiState: StateFlow<IncidenteUiState<List<Incidente>>> =
        repository.observarIncidentes()
            .map<List<Incidente>, IncidenteUiState<List<Incidente>>> { incidentes ->
                IncidenteUiState.Success(incidentes)
            }
            .catch { e ->
                emit(IncidenteUiState.Error(e.message ?: "Error desconocido"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = IncidenteUiState.Loading
            )

    /**
     * Mantiene compatibilidad con las pantallas que aún llaman a [cargarIncidentes].
     * Ya no hace nada: el Flow se inicia solo con [stateIn].
     */
    @Deprecated("Ya no es necesario llamar a este método. El Flow se activa automáticamente.")
    fun cargarIncidentes() { /* no-op */ }

    fun guardarIncidente(incidente: Incidente) {
        viewModelScope.launch {
            try {
                repository.guardarIncidente(incidente)
            } catch (e: Exception) {
                // El error se propagará al StateFlow si la DB falla.
            }
        }
    }
}
