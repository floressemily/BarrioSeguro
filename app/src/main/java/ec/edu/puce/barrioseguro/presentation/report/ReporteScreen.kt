package ec.edu.puce.barrioseguro.presentation.report

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.io.File

val TIPOS_INCIDENTE = listOf(
    "Robo",
    "Actividad sospechosa",
    "Vandalismo",
    "Asunto de seguridad pública"
)

// ---------------------------------------------------------------------------
// Entrada pública — firma fija para el NavGraph
// ---------------------------------------------------------------------------

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

    // ── Estado del formulario ────────────────────────────────────────────────
    var tipo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var fotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var ubicacionError by remember { mutableStateOf<String?>(null) }

    // URI temporal preparada antes de abrir la cámara, almacenada en caché privada
    // (no ocupa memoria RAM ni requiere permisos de almacenamiento externo).
    val fotoFileUri = remember {
        val fotoFile = File(context.cacheDir, "foto_incidente_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            fotoFile
        )
    }

    // ── Launcher de cámara con TakePicture (URI real, alta calidad) ──────────
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        // success == true significa que el usuario tomó la foto y se guardó en fotoFileUri.
        if (success) fotoUri = fotoFileUri
    }

    // ── Launcher de permiso de cámara ────────────────────────────────────────
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(fotoFileUri)
    }

    // ── Cliente de ubicación ─────────────────────────────────────────────────
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    fun obtenerUbicacion() {
        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cts.token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                latitud = location.latitude
                longitud = location.longitude
                ubicacionError = null
            } else {
                ubicacionError = "No se pudo obtener la ubicación. Intenta de nuevo."
            }
        }.addOnFailureListener {
            ubicacionError = "Error al obtener ubicación: ${it.message}"
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) obtenerUbicacion()
        else ubicacionError = "Permiso de ubicación denegado."
    }

    // ── Solicitar ubicación al abrir la pantalla ─────────────────────────────
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
        latitud = latitud,
        longitud = longitud,
        ubicacionError = ubicacionError,
        onTipoChange = { tipo = it },
        onDescripcionChange = { descripcion = it },
        onTomarFotoClick = {
            val cameraGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            if (cameraGranted) cameraLauncher.launch(fotoFileUri)
            else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        },
        onEnviarClick = {
            val incidente = Incidente(
                id = 0,
                tipo = tipo,
                descripcion = descripcion,
                latitud = latitud ?: 0.0,
                longitud = longitud ?: 0.0,
                fotoUri = fotoUri?.toString(), // URI real persistida como String
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

// ---------------------------------------------------------------------------
// Scaffold principal
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReporteScaffold(
    tipo: String,
    descripcion: String,
    fotoUri: Uri?,
    latitud: Double?,
    longitud: Double?,
    ubicacionError: String?,
    onTipoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onTomarFotoClick: () -> Unit,
    onEnviarClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reportar Incidente",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        ReporteForm(
            modifier = Modifier.padding(paddingValues),
            tipo = tipo,
            descripcion = descripcion,
            fotoUri = fotoUri,
            latitud = latitud,
            longitud = longitud,
            ubicacionError = ubicacionError,
            onTipoChange = onTipoChange,
            onDescripcionChange = onDescripcionChange,
            onTomarFotoClick = onTomarFotoClick,
            onEnviarClick = onEnviarClick
        )
    }
}

// ---------------------------------------------------------------------------
// Formulario principal
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReporteForm(
    modifier: Modifier = Modifier,
    tipo: String,
    descripcion: String,
    fotoUri: Uri?,
    latitud: Double?,
    longitud: Double?,
    ubicacionError: String?,
    onTipoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onTomarFotoClick: () -> Unit,
    onEnviarClick: () -> Unit
) {
    val formularioValido = tipo.isNotBlank() && descripcion.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── 1. Área de foto ──────────────────────────────────────────────────
        FotoSection(
            fotoUri = fotoUri,
            onTomarFotoClick = onTomarFotoClick
        )

        // ── 2. Dropdown de tipo de incidente ───────────────────────────────
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = tipo,
                onValueChange = {},
                readOnly = true,
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
                expanded = expanded,
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

        // ── 3. Descripción ──────────────────────────────────────────────────
        OutlinedTextField(
            value = descripcion,
            onValueChange = onDescripcionChange,
            placeholder = { Text("Describe el incidente...", color = Color.Gray) },
            minLines = 3,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        // ── 4. Ubicación GPS ────────────────────────────────────────────────
        UbicacionSection(
            latitud = latitud,
            longitud = longitud,
            ubicacionError = ubicacionError
        )

        // ── 5. Botón Enviar ──────────────────────────────────────────────────
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
                text = "Enviar Reporte",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Sección de foto — usa Coil AsyncImage para cargar la URI sin bloquear el hilo
// ---------------------------------------------------------------------------

@Composable
private fun FotoSection(
    fotoUri: Uri?,
    onTomarFotoClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE0E0E0))
            .clickable { onTomarFotoClick() },
        contentAlignment = Alignment.Center
    ) {
        if (fotoUri != null) {
            // Coil carga la imagen de forma asíncrona, comprimida y cacheada en disco —
            // no se carga el Bitmap completo en RAM.
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
                    tint = Color.DarkGray,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Toca para añadir una foto",
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sección de ubicación GPS
// ---------------------------------------------------------------------------

@Composable
private fun UbicacionSection(
    latitud: Double?,
    longitud: Double?,
    ubicacionError: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = Color(0xFFE53935),
            modifier = Modifier.size(20.dp)
        )
        val textoUbicacion = when {
            latitud != null && longitud != null ->
                "%.5f, %.5f".format(latitud, longitud)
            ubicacionError != null -> ubicacionError
            else -> "Obteniendo ubicación..."
        }
        Text(
            text = textoUbicacion,
            color = Color.DarkGray,
            fontSize = 14.sp
        )
    }
}