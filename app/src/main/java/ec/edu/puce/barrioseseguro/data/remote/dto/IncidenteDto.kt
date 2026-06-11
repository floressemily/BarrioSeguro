package ec.edu.puce.barrioseseguro.data.remote.dto

data class IncidenteDto(
    val id: Int,
    val tipo: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val fotoUri: String?,
    val timestamp: Long,
    val estado: String
)
