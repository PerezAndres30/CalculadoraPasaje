package com.andre.myapplication.calculo

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

const val PASAJEROS_MIN = 1
const val PASAJEROS_MAX = 10

data class CalculoUiState(

    val distancia: String = "",
    val rendimiento: String = "",
    val precioLitro: String = "",
    val pasajeros: Int = 2,
    val viajeRedondo: Boolean = false,

    val litros: String = "",
    val costoTotal: String = "",
    val costoPorPersona: String = "",
    val hayResultado: Boolean = false
)

class CalculoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalculoUiState())
    val uiState: StateFlow<CalculoUiState> = _uiState.asStateFlow()

    fun onDistanciaChange(texto: String) = actualizar { it.copy(distancia = filtrarDecimal(texto)) }

    fun onRendimientoChange(texto: String) = actualizar { it.copy(rendimiento = filtrarDecimal(texto)) }

    fun onPrecioChange(texto: String) = actualizar { it.copy(precioLitro = filtrarDecimal(texto)) }

    /** El deslizador entrega un Float; aquí se convierte a entero dentro de 1..10. */
    fun onPasajerosChange(valor: Float) = actualizar {
        it.copy(pasajeros = valor.roundToInt().coerceIn(PASAJEROS_MIN, PASAJEROS_MAX))
    }

    fun onViajeRedondoChange(activo: Boolean) = actualizar { it.copy(viajeRedondo = activo) }
    private fun actualizar(cambio: (CalculoUiState) -> CalculoUiState) {
        _uiState.update { actual -> calcular(cambio(actual)) }
    }
    private fun filtrarDecimal(texto: String): String {
        val limpio = StringBuilder()
        var hayPunto = false
        for (caracter in texto) {
            when {
                caracter in '0'..'9' -> limpio.append(caracter)
                caracter == '.' && !hayPunto -> {
                    limpio.append(caracter)
                    hayPunto = true
                }
            }
        }
        return limpio.toString()
    }

    private fun calcular(estado: CalculoUiState): CalculoUiState {
        val distancia = estado.distancia.toDoubleOrNull()
        val rendimiento = estado.rendimiento.toDoubleOrNull()
        val precio = estado.precioLitro.toDoubleOrNull()

        if (distancia == null || distancia <= 0.0 ||
            rendimiento == null || rendimiento <= 0.0 ||
            precio == null || precio <= 0.0
        ) {
            return estado.copy(litros = "", costoTotal = "", costoPorPersona = "", hayResultado = false)
        }

        val distanciaTotal = distancia * if (estado.viajeRedondo) 2.0 else 1.0
        val litros = distanciaTotal / rendimiento
        val costoTotal = litros * precio
        val costoPorPersona = costoTotal / estado.pasajeros

        return estado.copy(
            litros = formatearNumero(litros),
            costoTotal = formatearMoneda(costoTotal),
            costoPorPersona = formatearMoneda(costoPorPersona),
            hayResultado = true
        )
    }

    private fun formatearMoneda(monto: Double): String =
        NumberFormat.getCurrencyInstance(LOCALE_MX).format(monto)

    private fun formatearNumero(valor: Double): String =
        NumberFormat.getNumberInstance(LOCALE_MX).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(valor)

    private companion object {
        val LOCALE_MX: Locale = Locale.forLanguageTag("es-MX")
    }
}