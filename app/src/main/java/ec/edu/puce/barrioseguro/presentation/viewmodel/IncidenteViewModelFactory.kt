package ec.edu.puce.barrioseguro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import ec.edu.puce.barrioseguro.BarrioSeguroApplication

class IncidenteViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[APPLICATION_KEY] as BarrioSeguroApplication
        val repository = application.repository

        if (modelClass.isAssignableFrom(IncidenteViewModel::class.java)) {
            return IncidenteViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(DetalleViewModel::class.java)) {
            val savedStateHandle = extras.createSavedStateHandle()
            return DetalleViewModel(savedStateHandle, repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
