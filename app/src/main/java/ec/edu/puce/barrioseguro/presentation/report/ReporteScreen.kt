package ec.edu.puce.barrioseguro.presentation.report

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import ec.edu.puce.barrioseguro.data.local.database.BarrioSeguroDatabase
import ec.edu.puce.barrioseguro.data.repository.IncidenteRepositoryLocal
import ec.edu.puce.barrioseguro.domain.model.Incidente
import ec.edu.puce.barrioseguro.presentation.viewmodel.IncidenteViewModel
import ec.edu.puce.barrioseguro.presentation.viewmodel.IncidenteViewModelFactory

// Rojo indicado en los requisitos
private val ColorRojoEnviar = Color(0xFFE53935)

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
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember {
        IncidenteRepositoryLocal(
            BarrioSeguroDatabase.getInstance(context).incidenteDao()
        )
    }
    val viewModel: IncidenteViewModel = viewModel(
        factory = IncidenteViewModelFactory(repository)
    )

    // ── Estado del formulario ────────────────────────────────────────────────
    var tipo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var ubicacionError by remember { mutableStateOf<String?>(null) }

    // ── Launcher de cámara ───────────────────────────────────────────────────
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        fotoBitmap = bitmap
    }

    // ── Launcher de permiso de cámara ────────────────────────────────────────
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
    }

    // ── Launcher de permiso de ubicación ────────────────────────────────────
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
        fotoBitmap = fotoBitmap,
        latitud = latitud,
        longitud = longitud,
        ubicacionError = ubicacionError,
        onTipoChange = { tipo = it },
        onDescripcionChange = { descripcion = it },
        onTomarFotoClick = {
            val cameraGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            if (cameraGranted) cameraLauncher.launch(null)
            else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        },
        onEnviarClick = {
            val incidente = Incidente(
                id = 0,
                tipo = tipo,
                descripcion = descripcion,
                latitud = latitud ?: 0.0,
                longitud = longitud ?: 0.0,
                fotoUri = null, // TakePicturePreview devuelve Bitmap, no URI
                timestamp = System.currentTimeMillis(),
                estado = "activo"
            )
            viewModel.guardarIncidente(incidente)
            onNavigateBack()
        },
        onNavigateBack = onNavigateBack
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
    fotoBitmap: Bitmap?,
    latitud: Double?,
    longitud: Double?,
    ubicacionError: String?,
    onTipoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onTomarFotoClick: () -> Unit,
    onEnviarClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Nuevo Reporte",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Incidente comunitario",
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        ReporteForm(
            modifier = Modifier.padding(paddingValues),
            tipo = tipo,
            descripcion = descripcion,
            fotoBitmap = fotoBitmap,
            latitud = latitud,
            longitud = longitud,
            ubicacionError = ubicacionError,
            onTipoChange = onTipoChange,
            onDescripcionChange = onDescripcionChange,
            onTomarFotoClick = onTomarFotoClick,
            onEnviarClick = onEnviarClick,
            onCancelarClick = onNavigateBack
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
    fotoBitmap: Bitmap?,
    latitud: Double?,
    longitud: Double?,
    ubicacionError: String?,
    onTipoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onTomarFotoClick: () -> Unit,
    onEnviarClick: () -> Unit,
    onCancelarClick: () -> Unit
) {
    val formularioValido = tipo.isNotBlank() && descripcion.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Sección: Tipo de incidente (Dropdown) ───────────────────────────
        SeccionTitulo(texto = "Tipo de incidente")

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = tipo,
                onValueChange = {},
                readOnly = true,
                label = { Text("Selecciona el tipo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = MaterialTheme.shapes.medium
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

        // ── Sección: Descripción ────────────────────────────────────────────
        SeccionTitulo(texto = "Descripción del incidente")

        OutlinedTextField(
            value = descripcion,
            onValueChange = onDescripcionChange,
            label = { Text("¿Qué ocurrió?") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        // ── Sección: Foto ───────────────────────────────────────────────────
        SeccionTitulo(texto = "Foto del incidente")

        FotoSection(
            fotoBitmap = fotoBitmap,
            onTomarFotoClick = onTomarFotoClick
        )

        // ── Sección: Ubicación GPS ──────────────────────────────────────────
        SeccionTitulo(texto = "Ubicación GPS")

        UbicacionSection(
            latitud = latitud,
            longitud = longitud,
            ubicacionError = ubicacionError
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Botón Enviar ────────────────────────────────────────────────────
        Button(
            onClick = onEnviarClick,
            enabled = formularioValido,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorRojoEnviar,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "Enviar Reporte",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedButton(
            onClick = onCancelarClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "Cancelar",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ---------------------------------------------------------------------------
// Sección de foto
// ---------------------------------------------------------------------------

@Composable
private fun FotoSection(
    fotoBitmap: Bitmap?,
    onTomarFotoClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (fotoBitmap != null) {
            Card(
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Image(
                    bitmap = fotoBitmap.asImageBitmap(),
                    contentDescription = "Foto del incidente",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Sin foto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onTomarFotoClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (fotoBitmap != null) "Cambiar foto" else "Tomar foto",
                style = MaterialTheme.typography.labelLarge
            )
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
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(22.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                when {
                    ubicacionError != null -> {
                        Text(
                            text = ubicacionError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    latitud != null && longitud != null -> {
                        Text(
                            text = "Ubicación obtenida",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Lat: ${"%.6f".format(latitud)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Lon: ${"%.6f".format(longitud)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    else -> {
                        Text(
                            text = "Obteniendo ubicación GPS...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Componente auxiliar: título de sección
// ---------------------------------------------------------------------------

@Composable
private fun SeccionTitulo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}