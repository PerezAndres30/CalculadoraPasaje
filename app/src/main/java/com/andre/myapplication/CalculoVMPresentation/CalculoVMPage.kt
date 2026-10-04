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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andre.myapplication.R
import com.andre.myapplication.ui.theme.MyApplicationTheme
import com.andre.myapplication.calculo.CalculoViewModel.Combustible
import com.andre.myapplication.calculo.CalculoViewModel.ErroresEntrada
import com.andre.myapplication.calculo.CalculoViewModel.EstadoResultado
import com.andre.myapplication.calculo.CalculoViewModel.NivelCosto

/**
 * Único Composable que conoce al ViewModel.
 * Baja el estado (valores) y sube los eventos (lambdas).
 */
@Composable
fun CalculoVMPage(viewModel: CalculoViewModel = viewModel()) {
    val distancia by viewModel.distancia.collectAsStateWithLifecycle()
    val rendimiento by viewModel.rendimiento.collectAsStateWithLifecycle()
    val precioLitro by viewModel.precioLitro.collectAsStateWithLifecycle()
    val casetas by viewModel.casetas.collectAsStateWithLifecycle()
    val pagaCaseta by viewModel.pagaCaseta.collectAsStateWithLifecycle()
    val pasajeros by viewModel.pasajeros.collectAsStateWithLifecycle()
    val viajeRedondo by viewModel.viajeRedondo.collectAsStateWithLifecycle()
    val combustible by viewModel.combustible.collectAsStateWithLifecycle()
    val errores by viewModel.errores.collectAsStateWithLifecycle()
    val resultado by viewModel.resultado.collectAsStateWithLifecycle()

    CalculoContent(
        distancia = distancia,
        rendimiento = rendimiento,
        precioLitro = precioLitro,
        casetas = casetas,
        pagaCaseta = pagaCaseta,
        pasajeros = pasajeros,
        viajeRedondo = viajeRedondo,
        combustible = combustible,
        errores = errores,
        resultado = resultado,
        onDistanciaChange = viewModel::onDistanciaChange,
        onRendimientoChange = viewModel::onRendimientoChange,
        onCasetasChange = viewModel::onCasetasChange,
        onPagaCasetaChange = viewModel::onPagaCasetaChange,
        onPasajerosChange = viewModel::onPasajerosChange,
        onViajeRedondoChange = viewModel::onViajeRedondoChange,
        onCombustibleChange = viewModel::onCombustibleChange
    )
}


// Stateless: solo dibuja lo que recibe y avisa con lambdas. No conoce al ViewModel.

@Composable
fun CalculoContent(
    distancia: String,
    rendimiento: String,
    precioLitro: String,
    casetas: String,
    pagaCaseta: Boolean,
    pasajeros: Int,
    viajeRedondo: Boolean,
    combustible: Combustible,
    errores: ErroresEntrada,
    resultado: EstadoResultado,
    onDistanciaChange: (String) -> Unit,
    onRendimientoChange: (String) -> Unit,
    onCasetasChange: (String) -> Unit,
    onPagaCasetaChange: (Boolean) -> Unit,
    onPasajerosChange: (Float) -> Unit,
    onViajeRedondoChange: (Boolean) -> Unit,
    onCombustibleChange: (Combustible) -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Zona de captura: ocupa el espacio sobrante y hace scroll si no cabe
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Encabezado(titulo = stringResource(R.string.titulo))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.combustible_label))
                    Text(
                        text = stringResource(R.string.precio_valor, precioLitro),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                SelectorCombustible(
                    seleccionado = combustible,
                    onSeleccion = onCombustibleChange
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoDecimal(
                        valor = distancia,
                        etiqueta = stringResource(R.string.distancia_label),
                        hayError = errores.distancia,
                        onCambio = onDistanciaChange,
                        modifier = Modifier.weight(1f)
                    )
                    CampoDecimal(
                        valor = rendimiento,
                        etiqueta = stringResource(R.string.rendimiento_label),
                        hayError = errores.rendimiento,
                        onCambio = onRendimientoChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Bloque de casetas: el campo solo se habilita si el usuario las va a pagar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilaSwitch(
                            etiqueta = stringResource(R.string.paga_caseta_label),
                            activo = pagaCaseta,
                            onCambio = onPagaCasetaChange
                        )
                        CampoDecimal(
                            valor = casetas,
                            etiqueta = stringResource(R.string.casetas_label),
                            hayError = errores.casetas,
                            habilitado = pagaCaseta,
                            onCambio = onCasetasChange,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Text(text = stringResource(R.string.pasajeros_valor, pasajeros))
                Slider(
                    value = pasajeros.toFloat(),
                    onValueChange = onPasajerosChange,
                    valueRange = PASAJEROS_MIN.toFloat()..PASAJEROS_MAX.toFloat(),
                    steps = PASAJEROS_MAX - PASAJEROS_MIN - 1
                )

                FilaSwitch(
                    etiqueta = stringResource(R.string.viaje_redondo_label),
                    activo = viajeRedondo,
                    onCambio = onViajeRedondoChange
                )
            }

            // Zona de resultado: fija abajo, siempre a la vista
            Column(modifier = Modifier.padding(16.dp)) {
                when (resultado) {
                    EstadoResultado.Inicial -> Text(
                        text = stringResource(R.string.resultado_vacio),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    is EstadoResultado.Calculado -> TarjetaResultado(resultado)
                }
            }
        }
    }
}

@Composable
fun Encabezado(titulo: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun CampoDecimal(
    valor: String,
    etiqueta: String,
    hayError: Boolean,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(text = etiqueta) },
        enabled = habilitado,
        isError = hayError,
        supportingText = if (hayError) {
            { Text(text = stringResource(R.string.error_valor)) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun FilaSwitch(
    etiqueta: String,
    activo: Boolean,
    onCambio: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = etiqueta)
        Switch(checked = activo, onCheckedChange = onCambio)
    }
}

@Composable
fun SelectorCombustible(
    seleccionado: Combustible,
    onSeleccion: (Combustible) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Combustible.entries.forEach { tipo ->
            FilterChip(
                selected = tipo == seleccionado,
                onClick = { onSeleccion(tipo) },
                label = { Text(text = stringResource(tipo.nombre)) },
                colors = FilterChipDefaults.filterChipColors(
                    labelColor = colorResource(tipo.color),
                    selectedContainerColor = colorResource(tipo.color),
                    selectedLabelColor = colorResource(R.color.white)
                )
            )
        }
    }
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
fun TarjetaResultado(resultado: EstadoResultado.Calculado) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(resultado.nivel.color),
            contentColor = colorResource(R.color.texto_tarjeta)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilaResultado(stringResource(R.string.litros_label), resultado.litros)
            resultado.casetas?.let {
                FilaResultado(stringResource(R.string.casetas_resultado), it)
            }
            FilaResultado(stringResource(R.string.costo_total_label), resultado.costoTotal)
            FilaResultado(stringResource(R.string.costo_persona_label), resultado.costoPorPersona)
            FilaResultado(
                stringResource(R.string.nivel_label),
                stringResource(resultado.nivel.etiqueta)
            )
            LinearProgressIndicator(
                progress = { resultado.progreso },
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(R.color.texto_tarjeta),
                trackColor = colorResource(R.color.white)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalculoContentPreview() {
    MyApplicationTheme {
        CalculoContent(
            distancia = "120",
            rendimiento = "12",
            precioLitro = "\$24.00",
            casetas = "80",
            pagaCaseta = true,
            pasajeros = 3,
            viajeRedondo = true,
            combustible = Combustible.MAGNA,
            errores = ErroresEntrada(),
            resultado = EstadoResultado.Calculado(
                litros = "20.00",
                casetas = "\$80.00",
                costoTotal = "\$560.00",
                costoPorPersona = "\$186.67",
                progreso = 0.37f,
                nivel = NivelCosto.MEDIO
            ),
            onDistanciaChange = {},
            onRendimientoChange = {},
            onCasetasChange = {},
            onPagaCasetaChange = {},
            onPasajerosChange = {},
            onViajeRedondoChange = {},
            onCombustibleChange = {}
        )
    }
}