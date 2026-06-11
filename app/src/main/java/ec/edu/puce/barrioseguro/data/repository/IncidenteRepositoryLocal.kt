package ec.edu.puce.barrioseguro.data.repository

import ec.edu.puce.barrioseguro.data.local.dao.IncidenteDao
import ec.edu.puce.barrioseguro.data.mapper.toDomain
import ec.edu.puce.barrioseguro.data.mapper.toEntity
import ec.edu.puce.barrioseguro.domain.model.Incidente
import ec.edu.puce.barrioseguro.domain.repository.IncidenteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IncidenteRepositoryLocal(
    private val incidenteDao: IncidenteDao
) : IncidenteRepository {

    override fun observarIncidentes(): Flow<List<Incidente>> {
        return incidenteDao.observarIncidentes()
            .map { incidentes -> incidentes.map { it.toDomain() } }
    }

    override suspend fun guardarIncidente(incidente: Incidente) {
        incidenteDao.guardarIncidente(incidente.toEntity())
    }

    override suspend fun obtenerIncidentePorId(id: Int): Incidente? {
        return incidenteDao.obtenerIncidentePorId(id)?.toDomain()
    }
}