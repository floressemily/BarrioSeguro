package ec.edu.puce.barrioseguro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ec.edu.puce.barrioseguro.data.local.entity.IncidenteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidenteDao {

    @Query("SELECT * FROM incidentes ORDER BY timestamp DESC")
    fun observarIncidentes(): Flow<List<IncidenteEntity>>

    @Query("SELECT * FROM incidentes WHERE id = :id LIMIT 1")
    suspend fun obtenerIncidentePorId(id: Int): IncidenteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarIncidente(incidente: IncidenteEntity)

    @Query("DELETE FROM incidentes WHERE id = :id")
    suspend fun eliminarIncidente(id: Int)
}