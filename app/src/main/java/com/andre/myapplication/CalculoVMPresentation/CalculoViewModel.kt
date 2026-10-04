package com.andre.myapplication.calculo

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.andre.myapplication.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

const val PASAJEROS_MIN = 1
const val PASAJEROS_MAX = 10

// Reglas de negocio de prueba para el nivel de gasto por persona (en pesos)
private const val COSTO_REFERENCIA = 500.0
private const val UMBRAL_MEDIO = 100.0
private const val UMBRAL_ALTO = 300.0

class CalculoViewModel : ViewModel() {

    // ---------- Modelos ----------

    /** Tipos de combustible con su color de bomba. Los precios son datos de prueba (mock). */
    enum class Combustible(
        @StringRes val nombre: Int,
        val precioBase: Double,
        @ColorRes val color: Int
    ) {
        MAGNA(R.string.combustible_magna, 24.0, R.color.magna_verde),
        PREMIUM(R.string.combustible_premium, 26.0, R.color.premium_rojo),
        DIESEL(R.string.combustible_diesel, 26.5, R.color.diesel_gris)
    }

    /** Nivel de gasto por persona. Cada nivel trae su texto y su color. */
    enum class NivelCosto(@StringRes val etiqueta: Int, @ColorRes val color: Int) {
        BAJO(R.string.nivel_bajo, R.color.costo_bajo),
        MEDIO(R.string.nivel_medio, R.color.costo_medio),
        ALTO(R.string.nivel_alto, R.color.costo_alto)
    }

    /** Indica qué campos tienen un valor inválido. Inmutable: solo "val". */
    data class ErroresEntrada(
        val distancia: Boolean = false,
        val rendimiento: Boolean = false,
        val casetas: Boolean = false
    )

    /** Los dos estados posibles de la zona de resultados. */
    sealed interface EstadoResultado {

        /** Todavía no hay datos válidos para calcular. */
        data object Inicial : EstadoResultado

        /** Ya hay un cálculo listo; todo viene formateado por el ViewModel. */
        data class Calculado(
            val litros: String,
            val casetas: String?,
            val costoTotal: String,
            val costoPorPersona: String,
            val progreso: Float,
            val nivel: NivelCosto
        ) : EstadoResultado
    }

    // ---------- Entradas, el estado vive aquí ----------
    private val _distancia = MutableStateFlow("")
    val distancia: StateFlow<String> = _distancia.asStateFlow()

    private val _rendimiento = MutableStateFlow("")
    val rendimiento: StateFlow<String> = _rendimiento.asStateFlow()

    private val _casetas = MutableStateFlow("")
    val casetas: StateFlow<String> = _casetas.asStateFlow()

    private val _pagaCaseta = MutableStateFlow(false)
    val pagaCaseta: StateFlow<Boolean> = _pagaCaseta.asStateFlow()

    private val _pasajeros = MutableStateFlow(2)
    val pasajeros: StateFlow<Int> = _pasajeros.asStateFlow()

    private val _viajeRedondo = MutableStateFlow(false)
    val viajeRedondo: StateFlow<Boolean> = _viajeRedondo.asStateFlow()

    private val _combustible = MutableStateFlow(Combustible.MAGNA)
    val combustible: StateFlow<Combustible> = _combustible.asStateFlow()

    // El precio no se escribe: lo determina el combustible elegido (ya formateado)
    private val _precioLitro = MutableStateFlow(formatearMoneda(Combustible.MAGNA.precioBase))
    val precioLitro: StateFlow<String> = _precioLitro.asStateFlow()

    // ---------- Salidas, data classes inmutables ----------
    private val _errores = MutableStateFlow(ErroresEntrada())
    val errores: StateFlow<ErroresEntrada> = _errores.asStateFlow()

    private val _resultado = MutableStateFlow<EstadoResultado>(EstadoResultado.Inicial)
    val resultado: StateFlow<EstadoResultado> = _resultado.asStateFlow()

    // ---------- Eventos que llegan desde la UI ----------
    fun onDistanciaChange(nuevoTexto: String) {
        _distancia.value = nuevoTexto.soloNumero()
        calcular()
    }

    fun onRendimientoChange(nuevoTexto: String) {
        _rendimiento.value = nuevoTexto.soloNumero()
        calcular()
    }

    fun onCasetasChange(nuevoTexto: String) {
        _casetas.value = nuevoTexto.soloNumero()
        calcular()
    }

    fun onPagaCasetaChange(activo: Boolean) {
        _pagaCaseta.value = activo
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

    fun onCombustibleChange(nuevo: Combustible) {
        _combustible.value = nuevo
        _precioLitro.value = formatearMoneda(nuevo.precioBase)
        calcular()
    }

    // ---------- Lógica de negocio ----------
    private fun calcular() {
        val dist = _distancia.value.aDouble()
        val rend = _rendimiento.value.aDouble()
        val precio = _combustible.value.precioBase
        // Las casetas solo cuentan si el usuario activó "¿Pagarás caseta?"
        val montoCasetas = if (_pagaCaseta.value) _casetas.value.aDouble() else null
        val peajes = (montoCasetas ?: 0.0).coerceAtLeast(0.0)

        _errores.value = ErroresEntrada(
            distancia = esInvalido(_distancia.value, dist),
            rendimiento = esInvalido(_rendimiento.value, rend),
            casetas = _pagaCaseta.value && _casetas.value.isNotEmpty() && montoCasetas == null
        )

        if (dist == null || dist <= 0.0 || rend == null || rend <= 0.0) {
            _resultado.value = EstadoResultado.Inicial
            return
        }

        val distanciaTotal = dist * if (_viajeRedondo.value) 2.0 else 1.0
        val totalLitros = distanciaTotal / rend
        val totalCosto = totalLitros * precio + peajes
        val costoPorIndividuo = totalCosto / _pasajeros.value

        _resultado.value = EstadoResultado.Calculado(
            litros = formatearNumero(totalLitros),
            casetas = if (peajes > 0.0) formatearMoneda(peajes) else null,
            costoTotal = formatearMoneda(totalCosto),
            costoPorPersona = formatearMoneda(costoPorIndividuo),
            progreso = (costoPorIndividuo / COSTO_REFERENCIA).coerceIn(0.0, 1.0).toFloat(),
            nivel = obtenerNivel(costoPorIndividuo)
        )
    }

    private fun obtenerNivel(costoPorPersona: Double): NivelCosto = when {
        costoPorPersona < UMBRAL_MEDIO -> NivelCosto.BAJO
        costoPorPersona < UMBRAL_ALTO -> NivelCosto.MEDIO
        else -> NivelCosto.ALTO
    }

    // Hay error solo si el usuario ya escribió algo y no es un número mayor que 0
    private fun esInvalido(texto: String, valor: Double?): Boolean =
        texto.isNotEmpty() && (valor == null || valor <= 0.0)

    // Deja solo dígitos, punto y coma
    private fun String.soloNumero(): String =
        filter { it.isDigit() || it == '.' || it == ',' }

    // Acepta coma o punto como separador decimal
    private fun String.aDouble(): Double? = replace(',', '.').toDoubleOrNull()

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