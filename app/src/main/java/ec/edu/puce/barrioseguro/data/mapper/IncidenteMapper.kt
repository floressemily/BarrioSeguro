package ec.edu.puce.barrioseguro.data.mapper

import ec.edu.puce.barrioseguro.data.local.entity.IncidenteEntity
import ec.edu.puce.barrioseguro.domain.model.Incidente

fun IncidenteEntity.toDomain(): Incidente {
    return Incidente(
        id = id,
        tipo = tipo,
        descripcion = descripcion,
        latitud = latitud,
        longitud = longitud,
        fotoUri = fotoUri,
        timestamp = timestamp,
        estado = estado
    )
}

fun Incidente.toEntity(): IncidenteEntity {
    return IncidenteEntity(
        id = id,
        tipo = tipo,
        descripcion = descripcion,
        latitud = latitud,
        longitud = longitud,
        fotoUri = fotoUri,
        timestamp = timestamp,
        estado = estado
    )
}