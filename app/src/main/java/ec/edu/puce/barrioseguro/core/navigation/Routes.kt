package ec.edu.puce.barrioseguro.core.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object ReporteRoute

@Serializable
data class DetalleRoute(
    val incidenteId: Int
)