package ec.edu.puce.barrioseguro.presentation.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// Entrada pública — firma fija para el NavGraph
// ---------------------------------------------------------------------------

@Composable
fun ReporteScreen(
    onNavigateBack: () -> Unit
) {
    var tipo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var latitud by rememberSaveable { mutableStateOf("") }
    var longitud by rememberSaveable { mutableStateOf("") }

    val latitudValida = latitud.toDoubleOrNull() != null || latitud.isBlank()
    val longitudValida = longitud.toDoubleOrNull() != null || longitud.isBlank()

    ReporteContent(
        tipo = tipo,
        descripcion = descripcion,
        latitud = latitud,
        longitud = longitud,
        latitudValida = latitudValida,
        longitudValida = longitudValida,
        onTipoChange = { tipo = it },
        onDescripcionChange = { descripcion = it },
        onLatitudChange = { latitud = it },
        onLongitudChange = { longitud = it },
        onGuardarClick = onNavigateBack,
        onCancelarClick = onNavigateBack
    )
}

// ---------------------------------------------------------------------------
// Scaffold principal
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReporteContent(
    tipo: String,
    descripcion: String,
    latitud: String,
    longitud: String,
    latitudValida: Boolean,
    longitudValida: Boolean,
    onTipoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onLatitudChange: (String) -> Unit,
    onLongitudChange: (String) -> Unit,
    onGuardarClick: () -> Unit,
    onCancelarClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Nuevo reporte",
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
                    IconButton(onClick = onCancelarClick) {
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
            paddingValues = paddingValues,
            tipo = tipo,
            descripcion = descripcion,
            latitud = latitud,
            longitud = longitud,
            latitudValida = latitudValida,
            longitudValida = longitudValida,
            onTipoChange = onTipoChange,
            onDescripcionChange = onDescripcionChange,
            onLatitudChange = onLatitudChange,
            onLongitudChange = onLongitudChange,
            onGuardarClick = onGuardarClick,
            onCancelarClick = onCancelarClick
        )
    }
}

// ---------------------------------------------------------------------------
// Formulario Stateless
// ---------------------------------------------------------------------------

@Composable
private fun ReporteForm(
    paddingValues: PaddingValues,
    tipo: String,
    descripcion: String,
    latitud: String,
    longitud: String,
    latitudValida: Boolean,
    longitudValida: Boolean,
    onTipoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onLatitudChange: (String) -> Unit,
    onLongitudChange: (String) -> Unit,
    onGuardarClick: () -> Unit,
    onCancelarClick: () -> Unit
) {
    val formularioValido = tipo.isNotBlank() &&
            descripcion.isNotBlank() &&
            latitudValida &&
            longitudValida

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado de sección
        Text(
            text = "Datos del incidente",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Formulario preparado para integrarse con Room y validaciones del dominio.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Campos principales
        OutlinedTextField(
            value = tipo,
            onValueChange = onTipoChange,
            label = { Text("Tipo de incidente") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = onDescripcionChange,
            label = { Text("Descripción") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        // Sección de ubicación
        UbicacionHeader()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = latitud,
                onValueChange = onLatitudChange,
                label = { Text("Latitud") },
                isError = !latitudValida,
                supportingText = {
                    if (!latitudValida) Text("Número inválido")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = longitud,
                onValueChange = onLongitudChange,
                label = { Text("Longitud") },
                isError = !longitudValida,
                supportingText = {
                    if (!longitudValida) Text("Número inválido")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botones de acción
        Button(
            onClick = onGuardarClick,
            enabled = formularioValido,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "Guardar borrador",
                style = MaterialTheme.typography.labelLarge
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
    }
}

// ---------------------------------------------------------------------------
// Componente de sección de ubicación
// ---------------------------------------------------------------------------

@Composable
private fun UbicacionHeader() {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = "Ubicación del incidente",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Coordenadas GPS o estimadas manualmente",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview(showBackground = true, name = "Reporte - Modo claro")
@Composable
private fun ReporteScreenPreview() {
    MaterialTheme {
        ReporteScreen(onNavigateBack = {})
    }
}

@Preview(
    showBackground = true,
    name = "Reporte - Modo oscuro",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ReporteScreenDarkPreview() {
    MaterialTheme {
        ReporteScreen(onNavigateBack = {})
    }
}