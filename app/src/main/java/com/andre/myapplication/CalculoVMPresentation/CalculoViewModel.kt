package com.andre.myapplication.calculo

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

const val PASAJEROS_MIN = 1
const val PASAJEROS_MAX = 10

class CalculoViewModel : ViewModel() {

    // Entradas
    private val _distancia = MutableStateFlow("")
    val distancia: StateFlow<String> = _distancia.asStateFlow()

    private val _rendimiento = MutableStateFlow("")
    val rendimiento: StateFlow<String> = _rendimiento.asStateFlow()

    private val _precioLitro = MutableStateFlow("")
    val precioLitro: StateFlow<String> = _precioLitro.asStateFlow()

    private val _pasajeros = MutableStateFlow(2)
    val pasajeros: StateFlow<Int> = _pasajeros.asStateFlow()

    private val _viajeRedondo = MutableStateFlow(false)
    val viajeRedondo: StateFlow<Boolean> = _viajeRedondo.asStateFlow()

    // Resultados calculados
    private val _litros = MutableStateFlow("")
    val litros: StateFlow<String> = _litros.asStateFlow()

    private val _costoTotal = MutableStateFlow("")
    val costoTotal: StateFlow<String> = _costoTotal.asStateFlow()

    private val _costoPorPersona = MutableStateFlow("")
    val costoPorPersona: StateFlow<String> = _costoPorPersona.asStateFlow()

    private val _hayResultado = MutableStateFlow(false)
    val hayResultado: StateFlow<Boolean> = _hayResultado.asStateFlow()

    // Manejadores de eventos
    fun onDistanciaChange(nuevoTexto: String) {
        _distancia.value = nuevoTexto
        calcular()
    }

    fun onRendimientoChange(nuevoTexto: String) {
        _rendimiento.value = nuevoTexto
        calcular()
    }

    fun onPrecioChange(nuevoTexto: String) {
        _precioLitro.value = nuevoTexto
        calcular()
    }

    fun onPasajerosChange(nuevoValor: Float) {
        _pasajeros.value = nuevoValor.roundToInt().coerceIn(PASAJEROS_MIN, PASAJEROS_MAX)
        calcular()
    }

    fun onViajeRedondoChange(activo: Boolean) {
        _viajeRedondo.value = activo
        calcular()
    }

    private fun calcular() {
        val dist = _distancia.value.toDoubleOrNull()
        val rend = _rendimiento.value.toDoubleOrNull()
        val prec = _precioLitro.value.toDoubleOrNull()
        val numPasajeros = _pasajeros.value
        val esRedondo = _viajeRedondo.value

        if (dist == null || dist <= 0.0 ||
            rend == null || rend <= 0.0 ||
            prec == null || prec <= 0.0
        ) {
            _litros.value = ""
            _costoTotal.value = ""
            _costoPorPersona.value = ""
            _hayResultado.value = false
            return
        }

        val distanciaTotal = dist * if (esRedondo) 2.0 else 1.0
        val totalLitros = distanciaTotal / rend
        val totalCosto = totalLitros * prec
        val costoPorIndividuo = totalCosto / numPasajeros

        _litros.value = formatearNumero(totalLitros)
        _costoTotal.value = formatearMoneda(totalCosto)
        _costoPorPersona.value = formatearMoneda(costoPorIndividuo)
        _hayResultado.value = true
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