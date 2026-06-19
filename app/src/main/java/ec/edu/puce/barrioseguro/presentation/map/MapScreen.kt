package ec.edu.puce.barrioseguro.presentation.map

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import ec.edu.puce.barrioseguro.domain.model.Incidente
import ec.edu.puce.barrioseguro.presentation.common.IncidenteUiState
import ec.edu.puce.barrioseguro.presentation.viewmodel.IncidenteViewModel
import ec.edu.puce.barrioseguro.presentation.viewmodel.IncidenteViewModelFactory
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val viewModel: IncidenteViewModel = viewModel(
        factory = IncidenteViewModelFactory()
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarIncidentes()
    }

    MapScaffold(
        uiState = uiState,
        onNavigateToHome = onNavigateToHome,
        onNavigateToProfile = onNavigateToProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapScaffold(
    uiState: IncidenteUiState<List<Incidente>>,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mapa de Incidentes",
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
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Filled.Map, contentDescription = "Mapa") },
                    label = { Text("Mapa") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            IncidenteMapaCompletoOSM(uiState = uiState)
        }
    }
}

@Composable
private fun IncidenteMapaCompletoOSM(
    uiState: IncidenteUiState<List<Incidente>>
) {
    val posicionInicial = remember { GeoPoint(-0.2295, -78.5243) }
    val context = LocalContext.current
    var mapInitialized by remember { mutableStateOf(false) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(mapViewRef) {
        val mapView = mapViewRef
        onDispose {
            mapView?.onPause()
            mapView?.onDetach()
        }
    }

    when (uiState) {
        is IncidenteUiState.Success -> {
            val incidentes = uiState.data

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    // Inicializar OSMDroid ANTES de crear MapView
                    Configuration.getInstance().load(
                        ctx,
                        ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
                    )
                    Configuration.getInstance().userAgentValue = ctx.packageName

                    MapView(ctx).apply {
                        setMultiTouchControls(true)
                        zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
                        controller.setZoom(14.0)
                        controller.setCenter(posicionInicial)
                        onResume()
                    }.also { mapViewRef = it }
                },
                update = { mv ->
                    if (!mapInitialized && incidentes.isNotEmpty()) {
                        val primero = incidentes.first()
                        mv.controller.setZoom(14.0)
                        mv.controller.animateTo(GeoPoint(primero.latitud, primero.longitud))
                        mapInitialized = true
                    } else if (!mapInitialized) {
                        mv.controller.setZoom(14.0)
                        mv.controller.animateTo(posicionInicial)
                    }

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
        else -> {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFFCFD8DC)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = null,
                        tint = Color(0xFF607D8B),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cargando mapa interactivo…",
                        color = Color(0xFF455A64),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
