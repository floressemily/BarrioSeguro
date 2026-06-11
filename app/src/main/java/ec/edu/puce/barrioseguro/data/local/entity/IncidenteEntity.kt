package ec.edu.puce.barrioseguro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incidentes")
data class IncidenteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tipo: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val fotoUri: String?,
    val timestamp: Long,
    val estado: String
)