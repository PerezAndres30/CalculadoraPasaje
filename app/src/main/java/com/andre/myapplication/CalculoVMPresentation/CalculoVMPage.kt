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
    // Observamos cada StateFlow individualmente
    val distancia by viewModel.distancia.collectAsStateWithLifecycle()
    val rendimiento by viewModel.rendimiento.collectAsStateWithLifecycle()
    val precioLitro by viewModel.precioLitro.collectAsStateWithLifecycle()
    val pasajeros by viewModel.pasajeros.collectAsStateWithLifecycle()
    val viajeRedondo by viewModel.viajeRedondo.collectAsStateWithLifecycle()

    val litros by viewModel.litros.collectAsStateWithLifecycle()
    val costoTotal by viewModel.costoTotal.collectAsStateWithLifecycle()
    val costoPorPersona by viewModel.costoPorPersona.collectAsStateWithLifecycle()
    val hayResultado by viewModel.hayResultado.collectAsStateWithLifecycle()

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

            CampoDecimal(
                valor = distancia,
                etiqueta = stringResource(R.string.distancia_label),
                onCambio = viewModel::onDistanciaChange
            )
            CampoDecimal(
                valor = rendimiento,
                etiqueta = stringResource(R.string.rendimiento_label),
                onCambio = viewModel::onRendimientoChange
            )
            CampoDecimal(
                valor = precioLitro,
                etiqueta = stringResource(R.string.precio_label),
                onCambio = viewModel::onPrecioChange
            )

            Text(text = stringResource(R.string.pasajeros_valor, pasajeros))
            Slider(
                value = pasajeros.toFloat(),
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
                    checked = viajeRedondo,
                    onCheckedChange = viewModel::onViajeRedondoChange
                )
            }

            // La tarjeta solo aparece cuando ya hay un resultado válido calculado
            if (hayResultado) {
                TarjetaResultado(
                    litros = litros,
                    costoTotal = costoTotal,
                    costoPorPersona = costoPorPersona
                )
            }
        }
    }
}

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
fun TarjetaResultado(
    litros: String,
    costoTotal: String,
    costoPorPersona: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilaResultado(stringResource(R.string.litros_label), litros)
            FilaResultado(stringResource(R.string.costo_total_label), costoTotal)
            FilaResultado(stringResource(R.string.costo_persona_label), costoPorPersona)
        }
    }
}