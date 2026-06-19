package ec.edu.puce.barrioseguro.presentation.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ec.edu.puce.barrioseguro.data.local.database.BarrioSeguroDatabase
import ec.edu.puce.barrioseguro.data.repository.IncidenteRepositoryLocal
import ec.edu.puce.barrioseguro.domain.model.Incidente
import ec.edu.puce.barrioseguro.presentation.viewmodel.IncidenteViewModel
import ec.edu.puce.barrioseguro.presentation.viewmodel.IncidenteViewModelFactory
import ec.edu.puce.barrioseseguro.presentation.common.IncidenteUiState
import java.util.concurrent.TimeUnit

// ---------------------------------------------------------------------------
// Entrada pública — firma fija para el NavGraph
// ---------------------------------------------------------------------------

@Composable
fun HomeScreen(
    onNavigateToReporte: () -> Unit,
    onNavigateToDetalle: (Int) -> Unit
) {
    val context = LocalContext.current
    val repository = IncidenteRepositoryLocal(
        BarrioSeguroDatabase.getInstance(context).incidenteDao()
    )
    val viewModel: IncidenteViewModel = viewModel(
        factory = IncidenteViewModelFactory(repository)
    )

    LaunchedEffect(Unit) {
        // DATOS DE PRUEBA - remover antes de entrega final
        val incidentePrueba1 = Incidente(
            id = 0,
            tipo = "Robo",
            descripcion = "Robo de celular en la esquina del parque",
            latitud = -0.2295,
            longitud = -78.5243,
            fotoUri = null,
            timestamp = System.currentTimeMillis() - 600000,
            estado = "activo"
        )
        val incidentePrueba2 = Incidente(
            id = 0,
            tipo = "Actividad sospechosa",
            descripcion = "Persona merodeando el edificio hace 30 minutos",
            latitud = -0.2301,
            longitud = -78.5251,
            fotoUri = null,
            timestamp = System.currentTimeMillis() - 1800000,
            estado = "en revision"
        )
        viewModel.guardarIncidente(incidentePrueba1)
        viewModel.guardarIncidente(incidentePrueba2)

        viewModel.cargarIncidentes()
    }

    val uiState by viewModel.uiState.collectAsState()

    HomeScaffold(
        uiState = uiState,
        onNavigateToReporte = onNavigateToReporte,
        onNavigateToDetalle = onNavigateToDetalle
    )
}

// ---------------------------------------------------------------------------
// Scaffold principal
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScaffold(
    uiState: IncidenteUiState<List<Incidente>>,
    onNavigateToReporte: () -> Unit,
    onNavigateToDetalle: (Int) -> Unit
) {
    Scaffold(
        topBar = { HomeTopBar() },
        floatingActionButton = { HomeFab(onClick = onNavigateToReporte) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        HomeContent(
            uiState = uiState,
            paddingValues = paddingValues,
            onNavigateToDetalle = onNavigateToDetalle
        )
    }
}

// ---------------------------------------------------------------------------
// Contenido principal según estado
// ---------------------------------------------------------------------------

@Composable
private fun HomeContent(
    uiState: IncidenteUiState<List<Incidente>>,
    paddingValues: PaddingValues,
    onNavigateToDetalle: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when (uiState) {
            is IncidenteUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            is IncidenteUiState.Error -> {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp)
                )
            }

            is IncidenteUiState.Success -> {
                IncidenteList(
                    incidentes = uiState.data,
                    onNavigateToDetalle = onNavigateToDetalle
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Lista de incidentes
// ---------------------------------------------------------------------------

@Composable
private fun IncidenteList(
    incidentes: List<Incidente>,
    onNavigateToDetalle: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Alertas Recientes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (incidentes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay alertas registradas aún.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 88.dp) // espacio para el FAB
            ) {
                items(incidentes, key = { it.id }) { incidente ->
                    IncidenteCard(
                        incidente = incidente,
                        onClick = { onNavigateToDetalle(incidente.id) }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Tarjeta de incidente
// ---------------------------------------------------------------------------

@Composable
private fun IncidenteCard(
    incidente: Incidente,
    onClick: () -> Unit
) {
    val tiempoRelativo = remember(incidente.timestamp) {
        val ahora = System.currentTimeMillis()
        val diffMs = ahora - incidente.timestamp
        when {
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
    }

    val coordenadasBarrio = remember(incidente.latitud, incidente.longitud) {
        "%.4f, %.4f".format(incidente.latitud, incidente.longitud)
    }

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono de tipo
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = incidente.tipo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = coordenadasBarrio,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = incidente.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tiempoRelativo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Badge de estado
            EstadoBadge(estado = incidente.estado)
        }
    }
}

@Composable
private fun EstadoBadge(estado: String) {
    val (bgColor, textColor) = when (estado.lowercase()) {
        "activo"    -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        "resuelto"  -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        else        -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Text(
            text = estado,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Componentes reutilizables
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar() {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "BarrioSeguro",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Alertas comunitarias",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            IconButton(onClick = { /* futuro: notificaciones */ }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notificaciones",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun HomeFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null
            )
        },
        text = { Text("Nuevo reporte") },
        containerColor = Color(0xFFE53935),
        contentColor = Color.White
    )
}