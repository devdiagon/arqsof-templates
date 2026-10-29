package ec.edu.monster.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Convierte los tipos reales del servidor a texto para el contrato (R7): decimales con punto
 * y dos decimales, fechas en ISO-8601 (yyyy-MM-dd) y enteros sin separadores.
 * Es independiente del idioma o la configuración regional del equipo.
 */
public final class ConversorTexto {

    private ConversorTexto() {
    }

    public static String decimal(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? null : fecha.toString();
    }

    public static String entero(int valor) {
        return Integer.toString(valor);
    }
}
