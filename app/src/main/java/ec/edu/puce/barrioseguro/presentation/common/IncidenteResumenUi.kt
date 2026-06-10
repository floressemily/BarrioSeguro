package ec.edu.puce.barrioseguro.presentation.common

data class IncidenteResumenUi(
    val id: Int,
    val tipo: String,
    val descripcion: String,
    val zona: String,
    val estado: String
)