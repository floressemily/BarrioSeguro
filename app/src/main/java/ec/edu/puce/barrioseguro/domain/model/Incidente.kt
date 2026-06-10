package ec.edu.puce.barrioseguro.domain.model

data class Incidente(
    val id: Int,
    val tipo: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val fotoUri: String?,
    val timestamp: Long,
    val estado: String
)