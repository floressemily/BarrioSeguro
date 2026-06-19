package ec.edu.puce.barrioseguro.presentation.report

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import ec.edu.puce.barrioseguro.domain.model.Incidente
import ec.edu.puce.barrioseguro.presentation.viewmodel.IncidenteViewModel
import ec.edu.puce.barrioseguro.presentation.viewmodel.IncidenteViewModelFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.events.MapEventsReceiver
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import java.io.File

private const val QUITO_LAT = -0.2295
private const val QUITO_LON = -78.5243

val TIPOS_INCIDENTE = listOf(
    "Robo",
    "Actividad sospechosa",
    "Vandalismo",
    "Asunto de seguridad pública"
)

@Composable
fun ReporteScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: IncidenteViewModel = viewModel(
        factory = IncidenteViewModelFactory()
    )

    // Formulario state (String-based coordinates for stable text fields editing)
    var tipo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var fotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var latitudStr by rememberSaveable { mutableStateOf("") }
    var longitudStr by rememberSaveable { mutableStateOf("") }
    var ubicacionError by remember { mutableStateOf<String?>(null) }
    var isFormEditable by rememberSaveable { mutableStateOf(true) }

    val fotoFileUri = remember {
        val fotoFile = File(context.cacheDir, "foto_incidente_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            fotoFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) fotoUri = fotoFileUri
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(fotoFileUri)
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun aplicarFallbackQuito(razon: String) {
        latitudStr = QUITO_LAT.toString()
        longitudStr = QUITO_LON.toString()
        ubicacionError = "📍 $razon Se usará Quito como ubicación por defecto."
    }

    @SuppressLint("MissingPermission")
    fun obtenerUbicacion() {
        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cts.token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                latitudStr = location.latitude.toString()
                longitudStr = location.longitude.toString()
                ubicacionError = null
            } else {
                aplicarFallbackQuito("GPS sin señal.")
            }
        }.addOnFailureListener {
            aplicarFallbackQuito("Error de GPS: ${it.message}.")
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            obtenerUbicacion()
        } else {
            aplicarFallbackQuito("Permiso de ubicación denegado.")
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            obtenerUbicacion()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    ReporteScaffold(
        tipo = tipo,
        descripcion = descripcion,
        fotoUri = fotoUri,
        latitudStr = latitudStr,
        longitudStr = longitudStr,
        ubicacionError = ubicacionError,
        isFormEditable = isFormEditable,
        onToggleEditable = { isFormEditable = it },
        onTipoChange = { tipo = it },
        onDescripcionChange = { descripcion = it },
        onLatitudChange = { latitudStr = it },
        onLongitudChange = { longitudStr = it },
        onTomarFotoClick = {
            val cameraGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            if (cameraGranted) cameraLauncher.launch(fotoFileUri)
            else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        },
        onEnviarClick = {
            val lat = latitudStr.toDoubleOrNull() ?: 0.0
            val lon = longitudStr.toDoubleOrNull() ?: 0.0
            val incidente = Incidente(
                id = 0,
                tipo = tipo,
                descripcion = descripcion,
                latitud = lat,
                longitud = lon,
                fotoUri = fotoUri?.toString(),
                timestamp = System.currentTimeMillis(),
                estado = "activo"
            )
            viewModel.guardarIncidente(incidente)
            onNavigateBack()
        },
        onNavigateBack = onNavigateBack,
        onNavigateToMap = onNavigateToMap,
        onNavigateToProfile = onNavigateToProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReporteScaffold(
    tipo: String,
    descripcion: String,
    fotoUri: Uri?,
    latitudStr: String,
    longitudStr: String,
    ubicacionError: String?,
    isFormEditable: Boolean,
    onToggleEditable: (Boolean) -> Unit,
    onTipoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onLatitudChange: (String) -> Unit,
    onLongitudChange: (String) -> Unit,
    onTomarFotoClick: () -> Unit,
    onEnviarClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reportar Incidente",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isFormEditable) Icons.Filled.Edit else Icons.Filled.Lock,
                            contentDescription = if (isFormEditable) "Editable" else "Bloqueado",
                            tint = if (isFormEditable) Color(0xFFE53935) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Switch(
                            checked = isFormEditable,
                            onCheckedChange = onToggleEditable,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFE53935),
                                checkedTrackColor = Color(0xFFE53935).copy(alpha = 0.5f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0xFF333333)
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
        }
    ) { paddingValues ->
        ReporteForm(
            modifier = Modifier.padding(paddingValues),
            tipo = tipo,
            descripcion = descripcion,
            fotoUri = fotoUri,
            latitudStr = latitudStr,
            longitudStr = longitudStr,
            ubicacionError = ubicacionError,
            isFormEditable = isFormEditable,
            onTipoChange = onTipoChange,
            onDescripcionChange = onDescripcionChange,
            onLatitudChange = onLatitudChange,
            onLongitudChange = onLongitudChange,
            onTomarFotoClick = onTomarFotoClick,
            onEnviarClick = onEnviarClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReporteForm(
    modifier: Modifier = Modifier,
    tipo: String,
    descripcion: String,
    fotoUri: Uri?,
    latitudStr: String,
    longitudStr: String,
    ubicacionError: String?,
    isFormEditable: Boolean,
    onTipoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onLatitudChange: (String) -> Unit,
    onLongitudChange: (String) -> Unit,
    onTomarFotoClick: () -> Unit,
    onEnviarClick: () -> Unit
) {
    val latValid = latitudStr.toDoubleOrNull() != null
    val lonValid = longitudStr.toDoubleOrNull() != null
    val formularioValido = isFormEditable && tipo.isNotBlank() && descripcion.isNotBlank() && latValid && lonValid

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── 1. Bloque de bloqueo (Banner Informativo) ──
        if (!isFormEditable) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2C1A1A), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE53935), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "🔒 El formulario está bloqueado para edición. Activa el interruptor superior para modificar.",
                    color = Color(0xFFEF9A9A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ── 2. Área de foto ──────────────────────────────────────────────────
        FotoSection(
            fotoUri = fotoUri,
            isFormEditable = isFormEditable,
            onTomarFotoClick = onTomarFotoClick
        )

        // ── 3. Dropdown de tipo de incidente ───────────────────────────────
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded && isFormEditable,
            onExpandedChange = { if (isFormEditable) expanded = it }
        ) {
            OutlinedTextField(
                value = tipo,
                onValueChange = {},
                readOnly = true,
                enabled = isFormEditable,
                placeholder = {
                    Text("Seleccionar tipo de incidente", color = Color.Gray)
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded && isFormEditable,
                onDismissRequest = { expanded = false }
            ) {
                TIPOS_INCIDENTE.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            onTipoChange(opcion)
                            expanded = false
                        }
                    )
                }
            }
        }

        // ── 4. Descripción ──────────────────────────────────────────────────
        OutlinedTextField(
            value = descripcion,
            onValueChange = onDescripcionChange,
            enabled = isFormEditable,
            placeholder = { Text("Describe el incidente...", color = Color.Gray) },
            minLines = 3,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        // ── 5. Ubicación GPS editable ──────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = latitudStr,
                onValueChange = onLatitudChange,
                enabled = isFormEditable,
                label = { Text("Latitud") },
                isError = latitudStr.isNotEmpty() && !latValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
                value = longitudStr,
                onValueChange = onLongitudChange,
                enabled = isFormEditable,
                label = { Text("Longitud") },
                isError = longitudStr.isNotEmpty() && !lonValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
        }

        // ── 6. Mapa de Selección de Ubicación Exacta ──
        val latDouble = latitudStr.toDoubleOrNull() ?: QUITO_LAT
        val lonDouble = longitudStr.toDoubleOrNull() ?: QUITO_LON
        val geoPointActual = remember(latDouble, lonDouble) { GeoPoint(latDouble, lonDouble) }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Seleccionar ubicación exacta (toca el mapa)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.White
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
            ) {
                var formMapViewRef by remember { mutableStateOf<MapView?>(null) }
                
                DisposableEffect(Unit) {
                    onDispose {
                        formMapViewRef?.onPause()
                        formMapViewRef?.onDetach()
                        formMapViewRef = null
                    }
                }

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setMultiTouchControls(true)
                            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
                            controller.setZoom(15.0)
                            controller.setCenter(geoPointActual)
                            
                            val receiver = object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                    if (isFormEditable) {
                                        onLatitudChange(p.latitude.toString())
                                        onLongitudChange(p.longitude.toString())
                                    }
                                    return true
                                }
                                override fun longPressHelper(p: GeoPoint): Boolean {
                                    return false
                                }
                            }
                            overlays.add(MapEventsOverlay(receiver))
                            
                            onResume()
                        }.also { formMapViewRef = it }
                    },
                    update = { mv ->
                        mv.overlays.clear()
                        
                        val receiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                if (isFormEditable) {
                                    onLatitudChange(p.latitude.toString())
                                    onLongitudChange(p.longitude.toString())
                                }
                                return true
                            }
                            override fun longPressHelper(p: GeoPoint): Boolean {
                                return false
                            }
                        }
                        mv.overlays.add(MapEventsOverlay(receiver))

                        val marker = Marker(mv).apply {
                            position = geoPointActual
                            title = "Ubicación del incidente"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mv.overlays.add(marker)
                        
                        mv.controller.setCenter(geoPointActual)
                        mv.invalidate()
                    }
                )
            }
        }

        // Mensaje de error/info de ubicación
        if (ubicacionError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF3E3618),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = ubicacionError,
                    color = Color(0xFFFFF59D),
                    fontSize = 12.sp
                )
            }
        }

        // ── 7. Botón Enviar ──────────────────────────────────────────────────
        Button(
            onClick = onEnviarClick,
            enabled = formularioValido,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935),
                disabledContainerColor = Color(0xFFE53935).copy(alpha = 0.5f),
                contentColor = Color.White,
                disabledContentColor = Color.White
            )
        ) {
            Text(
                text = if (isFormEditable) "Enviar Reporte" else "🔒 Edición bloqueada",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FotoSection(
    fotoUri: Uri?,
    isFormEditable: Boolean,
    onTomarFotoClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A2A2A))
            .clickable(enabled = isFormEditable) { onTomarFotoClick() },
        contentAlignment = Alignment.Center
    ) {
        if (fotoUri != null) {
            AsyncImage(
                model = fotoUri,
                contentDescription = "Foto del incidente",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Toca para añadir una foto",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}