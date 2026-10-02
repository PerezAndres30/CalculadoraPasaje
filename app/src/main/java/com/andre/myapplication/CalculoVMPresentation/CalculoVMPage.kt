package com.andre.myapplication.calculo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andre.myapplication.R

@Composable
fun CalculoVMPage(viewModel: CalculoViewModel = viewModel()) {
    // Nos suscribimos al estado del ViewModel: cuando cambia, la pantalla se redibuja.
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.titulo),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = stringResource(R.string.instruccion))

            // Cada campo muestra su valor del estado y avisa al ViewModel cuando el usuario escribe.
            CampoDecimal(
                valor = estado.distancia,
                etiqueta = stringResource(R.string.distancia_label),
                onCambio = viewModel::onDistanciaChange
            )
            CampoDecimal(
                valor = estado.rendimiento,
                etiqueta = stringResource(R.string.rendimiento_label),
                onCambio = viewModel::onRendimientoChange
            )
            CampoDecimal(
                valor = estado.precioLitro,
                etiqueta = stringResource(R.string.precio_label),
                onCambio = viewModel::onPrecioChange
            )

            Text(text = stringResource(R.string.pasajeros_valor, estado.pasajeros))
            Slider(
                value = estado.pasajeros.toFloat(),
                onValueChange = viewModel::onPasajerosChange,
                valueRange = PASAJEROS_MIN.toFloat()..PASAJEROS_MAX.toFloat(),
                steps = PASAJEROS_MAX - PASAJEROS_MIN - 1
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.viaje_redondo_label))
                Switch(
                    checked = estado.viajeRedondo,
                    onCheckedChange = viewModel::onViajeRedondoChange
                )
            }

            // La tarjeta solo aparece cuando ya hay un resultado.
            if (estado.nivel != NivelCosto.INICIAL) {
                TarjetaResultado(estado)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------
// Componentes reutilizables: no guardan estado, solo reciben datos y avisan eventos.
// ---------------------------------------------------------------------------------------

@Composable
fun CampoDecimal(
    valor: String,
    etiqueta: String,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(text = etiqueta) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun FilaResultado(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta)
        Text(text = valor, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun TarjetaResultado(estado: CalculoUiState) {
    // El mensaje depende del nivel que calculó el ViewModel.
    val mensaje = when (estado.nivel) {
        NivelCosto.BAJO -> R.string.nivel_bajo
        NivelCosto.MEDIO -> R.string.nivel_medio
        else -> R.string.nivel_alto
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = stringResource(mensaje), style = MaterialTheme.typography.titleLarge)
            FilaResultado(stringResource(R.string.litros_label), estado.litros)
            FilaResultado(stringResource(R.string.costo_total_label), estado.costoTotal)
            FilaResultado(stringResource(R.string.costo_persona_label), estado.costoPorPersona)
        }
    }
}