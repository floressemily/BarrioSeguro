package ec.edu.puce.barrioseseguro.data.remote.api

import ec.edu.puce.barrioseseguro.data.remote.dto.IncidenteDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface IncidenteApiService {

    @GET("incidentes")
    suspend fun obtenerIncidentes(): List<IncidenteDto>

    @GET("incidentes/{id}")
    suspend fun obtenerIncidentePorId(
        @Path("id") id: Int
    ): IncidenteDto

    @POST("incidentes")
    suspend fun crearIncidente(
        @Body incidente: IncidenteDto
    ): IncidenteDto

    @PUT("incidentes/{id}")
    suspend fun actualizarIncidente(
        @Path("id") id: Int,
        @Body incidente: IncidenteDto
    ): IncidenteDto

    @DELETE("incidentes/{id}")
    suspend fun eliminarIncidente(
        @Path("id") id: Int
    )
}
