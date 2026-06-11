# BarrioSeguro

Aplicación móvil nativa Android desarrollada en Kotlin para seguridad y conectividad comunitaria.

## Fase 1: Cimientos y arquitectura

Este avance corresponde a la base técnica inicial del proyecto final de Programación Móvil.

## Stack técnico

- Kotlin
- Jetpack Compose
- Material Design 3
- Navigation Compose
- Navegación tipada con `@Serializable`
- Room
- Retrofit
- Coroutines
- Flow
- StateFlow
- Clean Architecture
- MVVM

## Arquitectura inicial

El proyecto se organiza en capas:

```text
core/
domain/
data/
presentation/
```

## Características implementadas (Fase 1)

*   **Arquitectura y Navegación (Mateo):** Configuración de dependencias en catálogo de versiones y grafo de navegación tipada mediante `@Serializable`.
*   **Base de Datos y Dominio (Gustavo):** Esquema de Room, entidades y contratos de repositorio listos para persistencia local.
*   **Red y Estados (William):** Configuración de llamadas API con Retrofit y tipado de estados de la UI (`IncidenteUiState`).
*   **UI Declarativa - Formulario (Emily):** Maquetación de `ReporteScreen` con campos validados e inmunes a destrucción del proceso.
*   **UI Declarativa - Listas Eficientes (Mathias):** Implementación de la vista `HomeScreen` con contenedor perezoso (`LazyColumn`), renderizado de tarjetas de incidentes (`IncidenteItem`) y lógica de estado vacío/lista conectada al grafo de navegación.
