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
 *
 * Para qué sirve: es el "puente" entre el ViewModel y la pantalla. Lee cada dato del
 * ViewModel y se lo entrega a CalculoContent, que es quien dibuja. Cuando el usuario
 * toca algo, CalculoContent avisa por medio de una lambda y aquí se manda al ViewModel.
 */
@Composable
fun CalculoVMPage(viewModel: CalculoViewModel = viewModel()) {
    // viewModel() obtiene (o crea) el ViewModel, y sobrevive a giros de pantalla.
    // collectAsStateWithLifecycle() convierte cada StateFlow en un estado de Compose:
    // cuando el valor cambia en el ViewModel, la pantalla se redibuja sola.
    // También deja de escuchar cuando la app no está visible, para ahorrar recursos.
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
        // Estado: lo que se muestra
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
        // Eventos: lo que pasa cuando el usuario toca algo.
        // "viewModel::onXxx" es una referencia a la función del ViewModel.
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
// Por eso se puede ver en el @Preview con datos inventados, sin necesidad del ViewModel real.

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
    // Scaffold da la estructura base de la pantalla; innerPadding evita que el contenido
    // quede tapado por las barras del sistema
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

                // Fila con la etiqueta "Combustible" a la izquierda y el precio por litro a la derecha
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
                // Chips para elegir Magna, Premium o Diésel
                SelectorCombustible(
                    seleccionado = combustible,
                    onSeleccion = onCombustibleChange
                )

                // Distancia y rendimiento lado a lado; weight(1f) reparte el ancho en partes iguales
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
                            onCambio = onPagaCasetaChange,
                        )
                        CampoDecimal(
                            valor = casetas,
                            etiqueta = stringResource(R.string.casetas_label),
                            hayError = errores.casetas,
                            habilitado = pagaCaseta, // se desactiva si el switch está apagado
                            onCambio = onCasetasChange,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Slider de pasajeros: muestra el número actual y deja elegir entre el mínimo y el máximo
                Text(text = stringResource(R.string.pasajeros_valor, pasajeros))
                Slider(
                    value = pasajeros.toFloat(), // el Slider trabaja con Float
                    onValueChange = onPasajerosChange,
                    valueRange = PASAJEROS_MIN.toFloat()..PASAJEROS_MAX.toFloat(),
                    // "steps" son las paradas intermedias; así el slider salta de número entero en entero
                    steps = PASAJEROS_MAX - PASAJEROS_MIN - 1
                )
                viajeRedondo
                FilaSwitch(
                    etiqueta = stringResource(R.string.viaje_redondo_label),
                    activo = viajeRedondo,
                    onCambio = onViajeRedondoChange
                )
                Profile(
                    etiqueta = stringResource(R.string.my_name),
                    etiqueta2 = stringResource(R.string.Matricula),
                    viajeRedondo= viajeRedondo
                )

            }

            // Zona de resultado: fija abajo, siempre a la vista
            Column(modifier = Modifier.padding(16.dp)) {
                // "when" decide qué mostrar según el estado que mande el ViewModel
                when (resultado) {
                    // Aún no hay datos válidos: texto de ayuda
                    EstadoResultado.Inicial -> Text(
                        text = stringResource(R.string.resultado_vacio),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Ya hay cálculo: tarjeta con el desglose
                    is EstadoResultado.Calculado -> TarjetaResultado(resultado)
                }
            }
        }
    }
}

/** Tarjeta de color con el título de la pantalla en la parte superior. */
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

/**
 * Campo de texto reutilizable para números decimales.
 * Muestra el mensaje de error debajo cuando "hayError" es true, y puede desactivarse
 * con "habilitado" (se usa en el campo de casetas).
 */
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
        // Solo se muestra el texto de error si hayError es true; si no, no ocupa espacio (null)
        supportingText = if (hayError) {
            { Text(text = stringResource(R.string.error_valor)) }
        } else {
            null
        },
        // Abre el teclado numérico con punto decimal
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun Profile(
    etiqueta: String,
    etiqueta2: String,
    viajeRedondo: Boolean
){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(viajeRedondo) {
            Text(text= etiqueta)
            Text(text= etiqueta2)
        }else{
            null
        }
    }
}

/** Fila reutilizable: una etiqueta a la izquierda y un interruptor (Switch) a la derecha. */
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

/**
 * Fila de chips para elegir el tipo de combustible.
 * Combustible.entries recorre Magna, Premium y Diésel; solo uno queda seleccionado a la vez.
 * Cada chip toma su color propio (el de la bomba) definido en el enum.
 */
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
                    labelColor = colorResource(tipo.color),               // texto cuando NO está elegido
                    selectedContainerColor = colorResource(tipo.color),   // fondo cuando SÍ está elegido
                    selectedLabelColor = colorResource(R.color.white)     // texto cuando SÍ está elegido
                )
            )
        }
    }
}

/** Una línea del resultado: la etiqueta a la izquierda y el valor a la derecha. */
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

/**
 * Tarjeta con el resultado del cálculo.
 * Su color de fondo depende del nivel de costo (bajo, medio o alto), y la barra de progreso
 * muestra qué tan caro resulta el viaje por persona.
 */
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
            // "?.let" ejecuta el bloque solo si casetas no es null; si es null, la fila no se dibuja
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
                progress = { resultado.progreso }, // valor entre 0.0 y 1.0 calculado en el ViewModel
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(R.color.texto_tarjeta),
                trackColor = colorResource(R.color.white)
            )
        }
    }
}

// Vista previa en Android Studio: usa datos inventados (no necesita el ViewModel real).
// Los valores coinciden con el cálculo: 120 km ida y vuelta = 240 km / 12 km/l = 20 litros;
// 20 x $24 + $80 de casetas = $560; entre 3 personas = $186.67.
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
            // Lambdas vacías: en la vista previa los botones no hacen nada
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