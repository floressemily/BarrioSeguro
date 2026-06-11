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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// Entrada pública — firma fija para el NavGraph
// ---------------------------------------------------------------------------

@Composable
fun HomeScreen(
    onNavigateToReporte: () -> Unit,
    onNavigateToDetalle: (Int) -> Unit
) {
    HomeScaffold(
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
    onNavigateToReporte: () -> Unit,
    onNavigateToDetalle: (Int) -> Unit
) {
    Scaffold(
        topBar = { HomeTopBar() },
        floatingActionButton = { HomeFab(onClick = onNavigateToReporte) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val mockIncidentes = listOf(
            ec.edu.puce.barrioseguro.domain.model.Incidente(
                id = 1,
                tipo = "Robo",
                descripcion = "Robo a mano armada en la esquina.",
                latitud = -0.22985,
                longitud = -78.52495,
                fotoUri = null,
                timestamp = System.currentTimeMillis(),
                estado = "Pendiente"
            ),
            ec.edu.puce.barrioseguro.domain.model.Incidente(
                id = 2,
                tipo = "Accidente de Tránsito",
                descripcion = "Choque entre dos autos cerca del parque.",
                latitud = -0.23112,
                longitud = -78.52012,
                fotoUri = null,
                timestamp = System.currentTimeMillis() - 86400000,
                estado = "Atendido"
            )
        )

        if (mockIncidentes.isEmpty()) {
            HomeEmptyState(
                paddingValues = paddingValues,
                onNavigateToReporte = onNavigateToReporte,
                onNavigateToDetalle = onNavigateToDetalle
            )
        } else {
            HomeListState(
                paddingValues = paddingValues,
                incidentes = mockIncidentes,
                onNavigateToDetalle = onNavigateToDetalle
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Componentes Stateless
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
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Composable
private fun HomeEmptyState(
    paddingValues: PaddingValues,
    onNavigateToReporte: () -> Unit,
    onNavigateToDetalle: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sin alertas activas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Las alertas se cargarán desde Room cuando el módulo de persistencia esté integrado.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Card arquitectónica — reemplazar cuando lleguen datos reales
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pantalla lista para datos reales",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Conectada al NavGraph con navegación tipada. Esperando integración de repositorio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigateToDetalle(1) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Probar detalle",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Button(
                        onClick = onNavigateToReporte,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "Reportar",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeListState(
    paddingValues: PaddingValues,
    incidentes: List<ec.edu.puce.barrioseguro.domain.model.Incidente>,
    onNavigateToDetalle: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(incidentes) { incidente ->
            IncidenteItem(
                incidente = incidente,
                onClick = { onNavigateToDetalle(incidente.id) }
            )
        }
    }
}

@Composable
private fun IncidenteItem(
    incidente: ec.edu.puce.barrioseguro.domain.model.Incidente,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = incidente.tipo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = if (incidente.estado == "Pendiente") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = incidente.estado,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (incidente.estado == "Pendiente") MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Text(
                text = incidente.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Lat: ${incidente.latitud}, Lng: ${incidente.longitud}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, name = "Home - Modo claro")
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            onNavigateToReporte = {},
            onNavigateToDetalle = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Home - Modo oscuro",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun HomeScreenDarkPreview() {
    MaterialTheme {
        HomeScreen(
            onNavigateToReporte = {},
            onNavigateToDetalle = {}
        )
    }
}