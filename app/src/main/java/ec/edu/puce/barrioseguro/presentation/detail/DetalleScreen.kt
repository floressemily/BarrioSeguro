package ec.edu.puce.barrioseguro.presentation.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// Modelo de vista auxiliar — stub estático para Fase 1
// En Fase 2 se reemplaza por un UiState alimentado desde ViewModel + Room/Retrofit
// ---------------------------------------------------------------------------

private data class DetalleUiModel(
    val id: Int,
    val tipo: String,
    val descripcion: String,
    val estado: String,
    val latitud: String,
    val longitud: String,
    val fechaHora: String,
    val reportadoPor: String,
    val nivelRiesgo: NivelRiesgo
)

private enum class NivelRiesgo(val label: String) {
    ALTO("Alto"),
    MEDIO("Medio"),
    BAJO("Bajo")
}

/** Genera datos de muestra según el id recibido por navegación tipada */
private fun stubIncidente(id: Int) = DetalleUiModel(
    id = id,
    tipo = "Robo a persona",
    descripcion = "Se reporta intento de robo en la esquina de la Av. Naciones Unidas " +
            "y calle República. El incidente ocurrió en horas de la noche. " +
            "Los testigos describen a dos individuos en motocicleta sin placas visibles.",
    estado = "Activo",
    latitud = "-0.2105",
    longitud = "-78.4877",
    fechaHora = "11 jun 2026 · 20:45",
    reportadoPor = "Ciudadano anónimo",
    nivelRiesgo = NivelRiesgo.ALTO
)

// ---------------------------------------------------------------------------
// Entrada pública — firma fija para el NavGraph
// ---------------------------------------------------------------------------

@Composable
fun DetalleScreen(
    incidenteId: Int,
    onNavigateBack: () -> Unit
) {
    val incidente = stubIncidente(incidenteId)
    DetalleContent(
        incidente = incidente,
        onNavigateBack = onNavigateBack
    )
}

// ---------------------------------------------------------------------------
// Scaffold principal
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetalleContent(
    incidente: DetalleUiModel,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Detalle de alerta",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Incidente #${incidente.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Fase 2: compartir */ }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Compartir alerta",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        DetalleBody(
            paddingValues = paddingValues,
            incidente = incidente,
            onNavigateBack = onNavigateBack
        )
    }
}

// ---------------------------------------------------------------------------
// Cuerpo Stateless con scroll completo
// ---------------------------------------------------------------------------

@Composable
private fun DetalleBody(
    paddingValues: PaddingValues,
    incidente: DetalleUiModel,
    onNavigateBack: () -> Unit
) {
    // Estado local que controla la sección expandida de descripción
    var descripcionExpandida by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── 1. Encabezado hero con tipo e indicador de riesgo ──────────────
        EncabezadoHero(incidente = incidente)

        // ── 2. Chips de metadatos rápidos ─────────────────────────────────
        MetadatosRow(incidente = incidente)

        // ── 3. Descripción con toggle de expansión ─────────────────────────
        SeccionDescripcion(
            descripcion = incidente.descripcion,
            expandida = descripcionExpandida,
            onToggle = { descripcionExpandida = !descripcionExpandida }
        )

        // ── 4. Ubicación geográfica ────────────────────────────────────────
        SeccionUbicacion(incidente = incidente)

        // ── 5. Información del estado ──────────────────────────────────────
        SeccionEstado(incidente = incidente)

        // ── 6. Aviso de integración — se elimina en Fase 2 ────────────────
        AvisoFase1()

        Spacer(modifier = Modifier.height(8.dp))

        // ── 7. Acciones ───────────────────────────────────────────────────
        AccionesSection(onNavigateBack = onNavigateBack)
    }
}

// ---------------------------------------------------------------------------
// Componentes de sección
// ---------------------------------------------------------------------------

@Composable
private fun EncabezadoHero(incidente: DetalleUiModel) {
    val riesgoColor = when (incidente.nivelRiesgo) {
        NivelRiesgo.ALTO -> Color(0xFFB71C1C)
        NivelRiesgo.MEDIO -> Color(0xFFE65100)
        NivelRiesgo.BAJO -> Color(0xFF1B5E20)
    }
    val riesgoFondo = when (incidente.nivelRiesgo) {
        NivelRiesgo.ALTO -> Color(0xFFFFEBEE)
        NivelRiesgo.MEDIO -> Color(0xFFFFF3E0)
        NivelRiesgo.BAJO -> Color(0xFFE8F5E9)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Ícono del tipo de incidente
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = incidente.tipo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Incidente #${incidente.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Badge de nivel de riesgo
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = riesgoFondo
                    ) {
                        Text(
                            text = "Riesgo ${incidente.nivelRiesgo.label}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = riesgoColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadatosRow(incidente: DetalleUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetadatoChip(
            icon = Icons.Filled.AccessTime,
            texto = incidente.fechaHora,
            modifier = Modifier.weight(1f)
        )
        MetadatoChip(
            icon = Icons.Filled.Person,
            texto = incidente.reportadoPor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetadatoChip(
    icon: ImageVector,
    texto: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = texto,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun SeccionDescripcion(
    descripcion: String,
    expandida: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionHeader(
                icon = Icons.Filled.Info,
                titulo = "Descripción del incidente"
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )

            // Texto con animación de expansión
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expandida) Int.MAX_VALUE else 3
                )
            }

            // Botón toggle Ver más / Ver menos
            OutlinedButton(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Text(
                    text = if (expandida) "Ver menos" else "Ver descripción completa",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun SeccionUbicacion(incidente: DetalleUiModel) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(
                icon = Icons.Filled.LocationOn,
                titulo = "Ubicación del incidente"
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )

            // Placeholder de mapa — en Fase 2 se integra Google Maps / MapKit
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Vista de mapa — Fase 2",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Coordenadas en texto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CoordenadaItem(
                    etiqueta = "Latitud",
                    valor = incidente.latitud,
                    modifier = Modifier.weight(1f)
                )
                CoordenadaItem(
                    etiqueta = "Longitud",
                    valor = incidente.longitud,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CoordenadaItem(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SeccionEstado(incidente: DetalleUiModel) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(
                icon = Icons.Filled.Shield,
                titulo = "Estado de la alerta"
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )

            // Barra de progreso de estado
            EstadoTimeline(estadoActual = incidente.estado)
        }
    }
}

@Composable
private fun EstadoTimeline(estadoActual: String) {
    val estados = listOf("Reportado", "Activo", "En atención", "Resuelto")
    val indiceActual = when (estadoActual) {
        "Reportado" -> 0
        "Activo" -> 1
        "En atención" -> 2
        "Resuelto" -> 3
        else -> 1
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        estados.forEachIndexed { index, estado ->
            val esPasado = index <= indiceActual
            val esActual = index == indiceActual

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (esActual) 28.dp else 22.dp)
                        .clip(CircleShape)
                        .background(
                            if (esPasado) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .border(
                            1.5.dp,
                            if (esPasado) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (esPasado) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = estado,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (esActual) FontWeight.Bold else FontWeight.Normal,
                    color = if (esPasado) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            if (index < estados.size - 1) {
                Spacer(
                    modifier = Modifier
                        .width(12.dp)
                        .height(1.5.dp)
                        .background(
                            if (index < indiceActual) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@Composable
private fun AvisoFase1() {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ReportProblem,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Datos de demostración — Fase 1",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "En Fase 2 los datos reales serán provistos por la capa Room / Retrofit " +
                            "mediante el ViewModel y el repositorio de dominio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun AccionesSection(onNavigateBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Volver al inicio",
                style = MaterialTheme.typography.labelLarge
            )
        }

        OutlinedButton(
            onClick = { /* Fase 2: contactar autoridades */ },
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Contactar autoridades — próximamente",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Componente reutilizable de encabezado de sección
// ---------------------------------------------------------------------------

@Composable
private fun SectionHeader(icon: ImageVector, titulo: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, name = "Detalle - Modo claro")
@Composable
private fun DetalleScreenPreview() {
    MaterialTheme {
        DetalleScreen(
            incidenteId = 42,
            onNavigateBack = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Detalle - Modo oscuro",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DetalleScreenDarkPreview() {
    MaterialTheme {
        DetalleScreen(
            incidenteId = 42,
            onNavigateBack = {}
        )
    }
}