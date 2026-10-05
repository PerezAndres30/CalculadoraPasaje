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

// Límites del slider de pasajeros. Son públicas porque la pantalla (UI) también las usa
// para dibujar el slider con el mismo rango.
const val PASAJEROS_MIN = 1
const val PASAJEROS_MAX = 10

// Reglas de negocio de prueba para el nivel de gasto por persona (en pesos)
// COSTO_REFERENCIA: con este costo por persona la barra de progreso queda llena al 100%.
// UMBRAL_MEDIO / UMBRAL_ALTO: cortes para clasificar el gasto en BAJO, MEDIO o ALTO.
private const val COSTO_REFERENCIA = 500.0
private const val UMBRAL_MEDIO = 100.0
private const val UMBRAL_ALTO = 300.0

/**
 * ViewModel de la calculadora de combustible por pasajero.
 *
 * Para qué sirve: guarda los datos que escribe el usuario, hace los cálculos y entrega
 * el resultado ya listo para mostrarse. La pantalla (Compose) NO calcula nada:
 * solo muestra lo que este ViewModel le da y le avisa cuando el usuario cambia algo.
 *
 * Ventaja: si se gira el teléfono, el ViewModel sobrevive y los datos no se pierden.
 */
class CalculoViewModel : ViewModel() {

    // ---------- Modelos ----------

    /** Tipos de combustible con su color de bomba. Los precios son datos de prueba (mock). */
    enum class Combustible(
        @StringRes val nombre: Int,      // texto que se muestra en el chip (viene de strings.xml)
        val precioBase: Double,          // precio por litro en pesos
        @ColorRes val color: Int         // color del chip (viene de colors.xml)
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

    /**
     * Indica qué campos tienen un valor inválido. Inmutable: solo "val".
     * La pantalla lo usa para pintar de rojo el campo y mostrar el mensaje de error.
     */
    data class ErroresEntrada(
        val distancia: Boolean = false,
        val rendimiento: Boolean = false,
        val casetas: Boolean = false
    )

    /** Los dos estados posibles de la zona de resultados. */
    sealed interface EstadoResultado {

        /** Todavía no hay datos válidos para calcular (la pantalla muestra un texto de ayuda). */
        data object Inicial : EstadoResultado

        /**
         * Ya hay un cálculo listo; todo viene formateado por el ViewModel.
         * Los montos son String (ej. "$560.00") para que la pantalla solo los muestre.
         * "casetas" es null cuando no hay casetas, y así la pantalla oculta esa fila.
         * "progreso" va de 0.0 a 1.0 y alimenta la barra de progreso.
         */
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
    // Patrón de cada dato: un MutableStateFlow privado (solo el ViewModel lo modifica)
    // y un StateFlow público de solo lectura (la pantalla solo lo observa).
    // Así nadie desde fuera puede cambiar el estado sin pasar por los eventos de abajo.

    // Los campos de texto se guardan como String para poder mostrar lo que el usuario escribe tal cual
    private val _distancia = MutableStateFlow("")
    val distancia: StateFlow<String> = _distancia.asStateFlow()

    private val _rendimiento = MutableStateFlow("")
    val rendimiento: StateFlow<String> = _rendimiento.asStateFlow()

    private val _casetas = MutableStateFlow("")
    val casetas: StateFlow<String> = _casetas.asStateFlow()

    // Switch "¿Pagarás caseta?": si está apagado, el monto de casetas se ignora
    private val _pagaCaseta = MutableStateFlow(false)
    val pagaCaseta: StateFlow<Boolean> = _pagaCaseta.asStateFlow()

    // Valor inicial: 2 pasajeros
    private val _pasajeros = MutableStateFlow(2)
    val pasajeros: StateFlow<Int> = _pasajeros.asStateFlow()

    // Switch "viaje redondo": si está activo, la distancia se cuenta dos veces (ida y vuelta)
    private val _viajeRedondo = MutableStateFlow(false)
    val viajeRedondo: StateFlow<Boolean> = _viajeRedondo.asStateFlow()

    private val _combustible = MutableStateFlow(Combustible.MAGNA)
    val combustible: StateFlow<Combustible> = _combustible.asStateFlow()

    // El precio no se escribe: lo determina el combustible elegido (ya formateado)
    private val _precioLitro = MutableStateFlow(formatearMoneda(Combustible.MAGNA.precioBase))
    val precioLitro: StateFlow<String> = _precioLitro.asStateFlow()

    // ---------- Salidas, data classes inmutables ----------
    // Son lo que la pantalla dibuja: qué campos tienen error y el resultado del cálculo.
    private val _errores = MutableStateFlow(ErroresEntrada())
    val errores: StateFlow<ErroresEntrada> = _errores.asStateFlow()

    private val _resultado = MutableStateFlow<EstadoResultado>(EstadoResultado.Inicial)
    val resultado: StateFlow<EstadoResultado> = _resultado.asStateFlow()

    // ---------- Eventos que llegan desde la UI ----------
    // La pantalla llama a estas funciones cuando el usuario toca o escribe algo.
    // Todas hacen lo mismo: guardan el nuevo valor y llaman a calcular() para
    // que el resultado siempre esté actualizado.

    fun onDistanciaChange(nuevoTexto: String) {
        _distancia.value = nuevoTexto.soloNumero() // filtra letras y símbolos raros
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
        // El slider entrega un Float (ej. 3.0); se redondea a entero y se limita al rango permitido
        _pasajeros.value = nuevoValor.roundToInt().coerceIn(PASAJEROS_MIN, PASAJEROS_MAX)
        calcular()
    }

    fun onViajeRedondoChange(activo: Boolean) {
        _viajeRedondo.value = activo
        calcular()
    }

    fun onCombustibleChange(nuevo: Combustible) {
        _combustible.value = nuevo
        _precioLitro.value = formatearMoneda(nuevo.precioBase) // actualiza el precio mostrado
        calcular()
    }

    // ---------- Lógica de negocio ----------

    /**
     * Función central: lee todas las entradas, valida, calcula y publica el resultado.
     *
     * Fórmulas:
     *   distancia total = distancia x (2 si es viaje redondo, 1 si no)
     *   litros          = distancia total / rendimiento (km por litro)
     *   costo total     = litros x precio por litro + casetas
     *   costo por persona = costo total / número de pasajeros
     */
    private fun calcular() {
        // Convierte los textos a número. Si el texto está vacío o es inválido, queda en null
        val dist = _distancia.value.aDouble()
        val rend = _rendimiento.value.aDouble()
        val precio = _combustible.value.precioBase
        // Las casetas solo cuentan si el usuario activó "¿Pagarás caseta?"
        val montoCasetas = if (_pagaCaseta.value) _casetas.value.aDouble() else null
        // "peajes" siempre es un número válido (0 si no hay casetas o el dato no sirve).
        // Nota: se suma una sola vez, aunque el viaje sea redondo.
        val peajes = (montoCasetas ?: 0.0).coerceAtLeast(0.0)

        // Paso 1: marcar qué campos tienen error para que la pantalla los pinte en rojo
        _errores.value = ErroresEntrada(
            distancia = esInvalido(_distancia.value, dist),
            rendimiento = esInvalido(_rendimiento.value, rend),
            casetas = _pagaCaseta.value && _casetas.value.isNotEmpty() && montoCasetas == null
        )

        // Paso 2: si falta distancia o rendimiento válidos, no se puede calcular.
        // Se muestra el estado "Inicial" y se termina aquí.
        if (dist == null || dist <= 0.0 || rend == null || rend <= 0.0) {
            _resultado.value = EstadoResultado.Inicial
            return
        }

        // Paso 3: hacer las cuentas
        val distanciaTotal = dist * if (_viajeRedondo.value) 2.0 else 1.0
        val totalLitros = distanciaTotal / rend
        val totalCosto = totalLitros * precio + peajes
        val costoPorIndividuo = totalCosto / _pasajeros.value

        // Paso 4: publicar el resultado ya formateado para que la pantalla solo lo muestre
        _resultado.value = EstadoResultado.Calculado(
            litros = formatearNumero(totalLitros),
            casetas = if (peajes > 0.0) formatearMoneda(peajes) else null, // null = ocultar fila
            costoTotal = formatearMoneda(totalCosto),
            costoPorPersona = formatearMoneda(costoPorIndividuo),
            // Qué tan lleno va la barra: costo por persona entre el costo de referencia, limitado entre 0 y 1
            progreso = (costoPorIndividuo / COSTO_REFERENCIA).coerceIn(0.0, 1.0).toFloat(),
            nivel = obtenerNivel(costoPorIndividuo)
        )
    }

    /** Clasifica el gasto por persona: menor a $100 = BAJO, menor a $300 = MEDIO, desde $300 = ALTO. */
    private fun obtenerNivel(costoPorPersona: Double): NivelCosto = when {
        costoPorPersona < UMBRAL_MEDIO -> NivelCosto.BAJO
        costoPorPersona < UMBRAL_ALTO -> NivelCosto.MEDIO
        else -> NivelCosto.ALTO
    }

    // Hay error solo si el usuario ya escribió algo y no es un número mayor que 0
    // (un campo vacío NO es error: simplemente aún no ha empezado a escribir)
    private fun esInvalido(texto: String, valor: Double?): Boolean =
        texto.isNotEmpty() && (valor == null || valor <= 0.0)

    // ---------- Funciones auxiliares ----------

    // Deja solo dígitos, punto y coma (así el usuario no puede escribir letras)
    private fun String.soloNumero(): String =
        filter { it.isDigit() || it == '.' || it == ',' }

    // Acepta coma o punto como separador decimal. Devuelve null si no es un número válido
    private fun String.aDouble(): Double? = replace(',', '.').toDoubleOrNull()

    // Da formato de pesos mexicanos, por ejemplo 560.0 -> "$560.00"
    private fun formatearMoneda(monto: Double): String =
        NumberFormat.getCurrencyInstance(LOCALE_MX).format(monto)

    // Da formato numérico con exactamente 2 decimales, por ejemplo 20.0 -> "20.00"
    private fun formatearNumero(valor: Double): String =
        NumberFormat.getNumberInstance(LOCALE_MX).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(valor)

    private companion object {
        // Configuración regional de México, para que los formatos usen su estilo de moneda y números
        val LOCALE_MX: Locale = Locale.forLanguageTag("es-MX")
    }
}