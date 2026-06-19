package ec.edu.puce.barrioseguro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ec.edu.puce.barrioseguro.domain.repository.IncidenteRepository

class IncidenteViewModelFactory(
    private val repository: IncidenteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncidenteViewModel::class.java)) {
            return IncidenteViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
