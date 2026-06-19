package ec.edu.puce.barrioseguro.presentation.detail

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ec.edu.puce.barrioseguro.data.local.database.BarrioSeguroDatabase
import ec.edu.puce.barrioseguro.data.repository.IncidenteRepositoryLocal
import ec.edu.puce.barrioseguro.domain.model.Incidente
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// Color solicitado en los requisitos
private val ColorRojo = Color(0xFFE53935)
private val ColorGrisOscuro = Color(0xFF616161)
private val ColorGrisClaro = Color(0xFFBDBDBD)

// ---------------------------------------------------------------------------
// Entrada pública — firma fija para el NavGraph
// ---------------------------------------------------------------------------

@Composable
fun DetalleScreen(
    incidenteId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember {
        IncidenteRepositoryLocal(
            BarrioSeguroDatabase.getInstance(context).incidenteDao()
        )
    }

    var incidente by remember { mutableStateOf<Incidente?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Buscar incidente por ID en Room
    LaunchedEffect(incidenteId) {
        cargando = true
        try {
            incidente = repository.obtenerIncidentePorId(incidenteId)
            if (incidente == null) errorMsg = "Incidente #$incidenteId no encontrado."
        } catch (e: Exception) {
            errorMsg = "Error al cargar: ${e.message}"
        } finally {
            cargando = false
        }
    }

    when {
        cargando -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorRojo)
            }
        }

        errorMsg != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        else -> {
            DetalleScaffold(
                incidente = incidente!!,
                onNavigateBack = onNavigateBack
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Scaffold principal
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetalleScaffold(
    incidente: Incidente,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Detalle del incidente",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        DetalleBody(
            incidente = incidente,
            paddingValues = paddingValues,
            onCompartir = {
                val texto = "Incidente: ${incidente.tipo}\n${incidente.descripcion}"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, texto)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir alerta"))
            },
            onSeguirAlerta = {
                scope.launch {
                    snackbarHostState.showSnackbar("Alerta guardada")
                }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Cuerpo principal con scroll
// ---------------------------------------------------------------------------

@Composable
private fun DetalleBody(
    incidente: Incidente,
    paddingValues: PaddingValues,
    onCompartir: () -> Unit,
    onSeguirAlerta: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Foto o placeholder
        FotoSection(fotoUrl = incidente.fotoUri)

        // 2. Chip de tipo con fondo rojo
        TipoChip(tipo = incidente.tipo)

        // 3. Descripción completa
        DescripcionSection(descripcion = incidente.descripcion)

        // 4. "Ocurrió hace X min"
        TiempoTranscurrido(timestamp = incidente.timestamp)

        // 5 & 6 & 7. Sección de estado con barra de progreso
        EstadoSection(estado = incidente.estado)

        // 8. Coordenadas GPS
        UbicacionSection(latitud = incidente.latitud, longitud = incidente.longitud)

        Spacer(modifier = Modifier.height(4.dp))

        // 9. Botones de acción
        AccionesSection(
            tipo = incidente.tipo,
            onCompartir = onCompartir,
            onSeguirAlerta = onSeguirAlerta
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ---------------------------------------------------------------------------
// Foto o placeholder
// ---------------------------------------------------------------------------

@Composable
private fun FotoSection(fotoUrl: String?) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically()
    ) {
        if (fotoUrl != null) {
            AsyncImage(
                model = fotoUrl,
                contentDescription = "Foto del incidente",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(52.dp)
                    )
                    Text(
                        text = "Sin foto disponible",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Chip de tipo con fondo rojo #E53935
// ---------------------------------------------------------------------------

@Composable
private fun TipoChip(tipo: String) {
    Box(
        modifier = Modifier
            .background(
                color = ColorRojo,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = tipo,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// ---------------------------------------------------------------------------
// Descripción completa
// ---------------------------------------------------------------------------

@Composable
private fun DescripcionSection(descripcion: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Descripción",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = descripcion,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------------------------
// Tiempo transcurrido
// ---------------------------------------------------------------------------

@Composable
private fun TiempoTranscurrido(timestamp: Long) {
    val ahora = System.currentTimeMillis()
    val diffMs = ahora - timestamp
    val textoTiempo = when {
        diffMs < 0 -> "justo ahora"
        diffMs < TimeUnit.MINUTES.toMillis(1) -> "hace menos de 1 min"
        diffMs < TimeUnit.HOURS.toMillis(1) -> {
            val mins = TimeUnit.MILLISECONDS.toMinutes(diffMs)
            "hace $mins min"
        }
        diffMs < TimeUnit.DAYS.toMillis(1) -> {
            val hrs = TimeUnit.MILLISECONDS.toHours(diffMs)
            "hace $hrs h"
        }
        else -> {
            val days = TimeUnit.MILLISECONDS.toDays(diffMs)
            "hace $days día(s)"
        }
    }

    Text(
        text = "Ocurrió $textoTiempo",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// ---------------------------------------------------------------------------
// Sección de estado con barra de progreso de 3 pasos
// ---------------------------------------------------------------------------

private val PASOS_ESTADO = listOf("Reportado", "En revisión", "Resuelto")

@Composable
private fun EstadoSection(estado: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Estado",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        BarraProgreso(estadoActual = estado)
    }
}

@Composable
private fun BarraProgreso(estadoActual: String) {
    // Mapeo flexible: "activo" → índice 0 (Reportado), "revisión" → 1, "resuelto" → 2
    val indiceActual = when {
        estadoActual.contains("resuelto", ignoreCase = true) -> 2
        estadoActual.contains("revision", ignoreCase = true) ||
                estadoActual.contains("revisión", ignoreCase = true) ||
                estadoActual.contains("revision", ignoreCase = true) -> 1
        else -> 0 // "activo", "reportado", o cualquier otro
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PASOS_ESTADO.forEachIndexed { index, paso ->
            val esActual = index == indiceActual
            val esPasado = index < indiceActual
            val esFuturo = index > indiceActual

            // Círculo del paso
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (esActual) 30.dp else 24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                esActual -> ColorRojo
                                esPasado -> ColorGrisOscuro
                                else -> ColorGrisClaro
                            }
                        )
                        .border(
                            width = if (esActual) 2.dp else 1.dp,
                            color = if (esFuturo) ColorGrisClaro else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (esPasado) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = paso,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (esActual) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        esActual -> ColorRojo
                        esPasado -> ColorGrisOscuro
                        else -> ColorGrisClaro
                    }
                )
            }

            // Línea conectora entre pasos
            if (index < PASOS_ESTADO.size - 1) {
                Spacer(
                    modifier = Modifier
                        .weight(0.3f)
                        .height(2.dp)
                        .background(
                            if (index < indiceActual) ColorGrisOscuro else ColorGrisClaro
                        )
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Ubicación GPS
// ---------------------------------------------------------------------------

@Composable
private fun UbicacionSection(latitud: Double, longitud: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Ubicación GPS",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CoordenadaCard(
                etiqueta = "Latitud",
                valor = "%.6f".format(latitud),
                modifier = Modifier.weight(1f)
            )
            CoordenadaCard(
                etiqueta = "Longitud",
                valor = "%.6f".format(longitud),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CoordenadaCard(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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

// ---------------------------------------------------------------------------
// Botones de acción
// ---------------------------------------------------------------------------

@Composable
private fun AccionesSection(
    tipo: String,
    onCompartir: () -> Unit,
    onSeguirAlerta: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onCompartir,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Compartir",
                style = MaterialTheme.typography.labelLarge
            )
        }

        OutlinedButton(
            onClick = onSeguirAlerta,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Seguir alerta",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}