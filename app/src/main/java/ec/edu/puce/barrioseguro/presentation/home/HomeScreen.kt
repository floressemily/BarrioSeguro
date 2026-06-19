package ec.edu.puce.barrioseguro.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
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
    val viewModel: IncidenteViewModel = viewModel(
        factory = IncidenteViewModelFactory()
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarIncidentes()
    }

    // DATOS DE PRUEBA condicionados
    LaunchedEffect(uiState) {
        if (uiState is IncidenteUiState.Success) {
            val list = (uiState as IncidenteUiState.Success<List<Incidente>>).data
            if (list.isEmpty()) {
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
            }
        }
    }

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
        bottomBar = { HomeBottomBar() },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        HomeContent(
            uiState = uiState,
            paddingValues = paddingValues,
            onNavigateToDetalle = onNavigateToDetalle
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "BarrioSeguro",
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notificaciones"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun HomeFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color(0xFFE53935),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Nuevo reporte"
        )
    }
}

@Composable
private fun HomeBottomBar() {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Filled.Map, contentDescription = "Mapa") },
            label = { Text("Mapa") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // MAPA OPENSTREETMAP
        IncidenteMapaOSM(
            uiState = uiState,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
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
}

// ---------------------------------------------------------------------------
// Mapa de incidentes — OpenStreetMap (OSMDroid)
// ---------------------------------------------------------------------------

/**
 * Muestra un MapView de OpenStreetMap (OSMDroid) real con marcadores por cada incidente cuando el estado es [Success].
 * En [Loading] o [Error] muestra un placeholder estilizado para no romper el layout.
 */
@Composable
private fun IncidenteMapaOSM(
    uiState: IncidenteUiState<List<Incidente>>,
    modifier: Modifier = Modifier
) {
    val posicionInicial = remember { GeoPoint(-0.2295, -78.5243) }
    val context = LocalContext.current

    val mapView = remember {
        MapView(context).apply {
            setMultiTouchControls(true)
            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
            controller.setZoom(14.0)
            controller.setCenter(posicionInicial)
        }
    }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onDetach()
        }
    }

    when (uiState) {
        is IncidenteUiState.Success -> {
            val incidentes = uiState.data

            LaunchedEffect(incidentes) {
                Configuration.getInstance().load(
                    context,
                    context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
                )
                Configuration.getInstance().userAgentValue = context.packageName

                if (incidentes.isNotEmpty()) {
                    val primero = incidentes.first()
                    mapView.controller.animateTo(GeoPoint(primero.latitud, primero.longitud))
                } else {
                    mapView.controller.animateTo(posicionInicial)
                }
            }

            AndroidView(
                modifier = modifier,
                factory = { mapView },
                update = { mv ->
                    mv.overlays.clear()
                    incidentes.forEach { incidente ->
                        val marker = Marker(mv).apply {
                            position = GeoPoint(incidente.latitud, incidente.longitud)
                            title = incidente.tipo
                            snippet = incidente.descripcion
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mv.overlays.add(marker)
                    }
                    mv.invalidate()
                }
            )
        }

        // Placeholder estilizado para Loading y Error
        else -> {
            Box(
                modifier = modifier.background(Color(0xFFCFD8DC)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = null,
                        tint = Color(0xFF607D8B),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cargando mapa…",
                        color = Color(0xFF455A64),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Alertas Recientes",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Recientes ▼",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }

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
                
                if (incidentes.size > 3) {
                    item {
                        Text(
                            text = "Ver más alertas",
                            color = Color(0xFFE53935),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .clickable { },
                            textAlign = TextAlign.Center
                        )
                    }
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE53935), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = incidente.tipo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = coordenadasBarrio,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = tiempoRelativo,
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.End
            )
        }
    }
}