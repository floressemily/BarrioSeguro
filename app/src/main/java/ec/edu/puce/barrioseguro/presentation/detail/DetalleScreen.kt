package ec.edu.puce.barrioseguro.presentation.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import ec.edu.puce.barrioseguro.presentation.viewmodel.DetalleViewModel
import ec.edu.puce.barrioseguro.presentation.viewmodel.IncidenteViewModelFactory
import ec.edu.puce.barrioseguro.presentation.common.IncidenteUiState
import ec.edu.puce.barrioseguro.domain.model.Incidente
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun DetalleScreen(
    incidenteId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val viewModel: DetalleViewModel = viewModel(
        factory = IncidenteViewModelFactory()
    )

    val uiState by viewModel.uiState.collectAsState()

    // ── Simulador de latencia REST (1.5s) ──
    var isLoadingSimulated by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1_500L)
        isLoadingSimulated = false
    }

    val showLoading = isLoadingSimulated || uiState is IncidenteUiState.Loading

    when {
        showLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = Color(0xFFE53935))
                    Text(
                        text = "Cargando detalle del incidente…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }

        uiState is IncidenteUiState.Error -> {
            val errorState = uiState as IncidenteUiState.Error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        uiState is IncidenteUiState.Success -> {
            DetalleScaffold(
                incidente = (uiState as IncidenteUiState.Success).data,
                onNavigateBack = onNavigateBack,
                onNavigateToMap = onNavigateToMap,
                onNavigateToProfile = onNavigateToProfile,
                onEstadoChange = { nuevoEstado ->
                    viewModel.actualizarEstado(nuevoEstado)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetalleScaffold(
    incidente: Incidente,
    onNavigateBack: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onEstadoChange: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Detalle del incidente",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notificaciones",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF121212)
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF121212)) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateBack,
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToMap,
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
            },
            onEstadoChange = { nuevoEstado ->
                onEstadoChange(nuevoEstado)
                scope.launch {
                    snackbarHostState.showSnackbar("Estado actualizado a: $nuevoEstado")
                }
            }
        )
    }
}

@Composable
private fun DetalleBody(
    incidente: Incidente,
    paddingValues: PaddingValues,
    onCompartir: () -> Unit,
    onSeguirAlerta: () -> Unit,
    onEstadoChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        // IMAGEN SUPERIOR CON BADGE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            if (incidente.fotoUri != null) {
                AsyncImage(
                    model = incidente.fotoUri,
                    contentDescription = "Foto del incidente",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2A2A2A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Badge superpuesto abajo izquierda
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .background(Color(0xFFE53935), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = incidente.tipo,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // HEADER TÍTULO
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = incidente.descripcion,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                val tiempoRelativo = remember(incidente.timestamp) {
                    val diffMs = System.currentTimeMillis() - incidente.timestamp
                    when {
                        diffMs < TimeUnit.MINUTES.toMillis(1) -> "justo ahora"
                        diffMs < TimeUnit.HOURS.toMillis(1) -> "hace ${TimeUnit.MILLISECONDS.toMinutes(diffMs)} min"
                        diffMs < TimeUnit.DAYS.toMillis(1) -> "hace ${TimeUnit.MILLISECONDS.toHours(diffMs)} horas"
                        else -> "hace ${TimeUnit.MILLISECONDS.toDays(diffMs)} días"
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Lat: %.4f, Lon: %.4f".format(incidente.latitud, incidente.longitud),
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tiempoRelativo,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            // SECCIÓN DESCRIPCIÓN
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Descripción",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    text = incidente.descripcion,
                    fontSize = 14.sp,
                    color = Color.LightGray
                )
            }

            // SECCIÓN MAPA DE UBICACIÓN
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ubicación en mapa",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
                ) {
                    var miniMapViewRef by remember { mutableStateOf<MapView?>(null) }
                    
                    DisposableEffect(Unit) {
                        onDispose {
                            miniMapViewRef?.onPause()
                            miniMapViewRef?.onDetach()
                            miniMapViewRef = null
                        }
                    }

                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setMultiTouchControls(true)
                                zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
                                controller.setZoom(15.5)
                                controller.setCenter(GeoPoint(incidente.latitud, incidente.longitud))
                                onResume()
                            }.also { miniMapViewRef = it }
                        },
                        update = { mv ->
                            mv.overlays.clear()
                            val marker = Marker(mv).apply {
                                position = GeoPoint(incidente.latitud, incidente.longitud)
                                title = incidente.tipo
                                snippet = incidente.descripcion
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            mv.overlays.add(marker)
                            mv.controller.setCenter(GeoPoint(incidente.latitud, incidente.longitud))
                            mv.invalidate()
                        }
                    )
                }
            }

            // SECCIÓN ESTADO (INTERACTIVA)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Estado",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val estados = listOf("Reportado", "En revisión", "Resuelto", "En alerta")
                    estados.forEach { texto ->
                        val esActual = when (texto) {
                            "Reportado" -> incidente.estado.contains("reportado", ignoreCase = true) || incidente.estado.contains("activo", ignoreCase = true) || incidente.estado.isEmpty()
                            "En revisión" -> incidente.estado.contains("revision", ignoreCase = true) || incidente.estado.contains("revisión", ignoreCase = true)
                            "Resuelto" -> incidente.estado.contains("resuelto", ignoreCase = true)
                            "En alerta" -> incidente.estado.contains("alerta", ignoreCase = true)
                            else -> false
                        }

                        val bgColor = if (esActual) Color(0xFFE53935) else Color(0xFF2A2A2A)
                        val textColor = if (esActual) Color.White else Color.Gray
                        
                        val modifierBox = Modifier
                            .background(bgColor, RoundedCornerShape(20.dp))
                            .then(
                                if (!esActual) Modifier.border(1.dp, Color.Gray, RoundedCornerShape(20.dp))
                                else Modifier
                            )
                            .clickable {
                                if (!esActual) {
                                    onEstadoChange(texto)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)

                        Box(modifier = modifierBox) {
                            Text(
                                text = texto,
                                color = textColor,
                                fontSize = 13.sp,
                                fontWeight = if (esActual) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTONES INFERIORES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCompartir,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A3A)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Compartir",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = onSeguirAlerta,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Seguir alerta",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}