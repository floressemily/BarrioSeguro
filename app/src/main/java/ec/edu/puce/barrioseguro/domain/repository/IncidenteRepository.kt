package ec.edu.puce.barrioseguro.domain.repository

import ec.edu.puce.barrioseguro.domain.model.Incidente
import kotlinx.coroutines.flow.Flow

interface IncidenteRepository {
    fun observarIncidentes(): Flow<List<Incidente>>

    suspend fun guardarIncidente(incidente: Incidente)

    suspend fun obtenerIncidentePorId(id: Int): Incidente?
}