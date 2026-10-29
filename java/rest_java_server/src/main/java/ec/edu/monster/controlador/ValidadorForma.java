package ec.edu.monster.controlador;

import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionMonster;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Reglas genéricas de forma de los datos de entrada (obligatoriedad y formato).
 * No conoce ningún dominio: cada controlador las combina con los formatos de su dominio.
 * Todo texto se recorta antes de validar; un valor nulo o en blanco cuenta como no enviado.
 * Un incumplimiento lanza DATA-001 con un mensaje específico del campo.
 */
public final class ValidadorForma {

    private static final Pattern SOLO_DIGITOS = Pattern.compile("\\d+");
    private static final Pattern DECIMAL_POSITIVO = Pattern.compile("(\\d+)(\\.(\\d+))?");

    private ValidadorForma() {
    }

    /**
     * El valor debe venir informado. Devuelve el valor recortado.
     */
    public static String obligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw invalido("El campo " + campo + " es obligatorio");
        }
        return valor.trim();
    }

    /**
     * Obligatorio y de hasta {@code maximo} caracteres.
     */
    public static String textoHasta(String valor, String campo, int maximo) {
        String texto = obligatorio(valor, campo);
        if (texto.length() > maximo) {
            throw invalido("El campo " + campo + " no puede tener más de " + maximo + " caracteres");
        }
        return texto;
    }

    /**
     * Obligatorio y de exactamente {@code longitud} caracteres.
     */
    public static String textoExacto(String valor, String campo, int longitud) {
        String texto = obligatorio(valor, campo);
        if (texto.length() != longitud) {
            throw invalido("El campo " + campo + " debe tener exactamente " + longitud + " caracteres");
        }
        return texto;
    }

    /**
     * Obligatorio y formado por exactamente {@code longitud} dígitos.
     */
    public static String digitosExactos(String valor, String campo, int longitud) {
        String texto = obligatorio(valor, campo);
        if (texto.length() != longitud || !SOLO_DIGITOS.matcher(texto).matches()) {
            throw invalido("El campo " + campo + " debe tener exactamente " + longitud + " dígitos");
        }
        return texto;
    }

    /**
     * Obligatorio, con punto decimal opcional (sin coma, signo ni separador de miles),
     * mayor que cero y dentro de los límites de dígitos indicados.
     */
    public static BigDecimal decimalPositivo(String valor, String campo, int maxEnteros,
            int maxDecimales) {
        String texto = obligatorio(valor, campo);
        var coincidencia = DECIMAL_POSITIVO.matcher(texto);
        if (!coincidencia.matches()) {
            throw invalido("El campo " + campo + " debe ser un número con punto decimal, sin signo");
        }
        String decimales = coincidencia.group(3);
        if (decimales != null && decimales.length() > maxDecimales) {
            throw invalido("El campo " + campo + " no puede tener más de " + maxDecimales
                    + " decimales");
        }
        if (coincidencia.group(1).length() > maxEnteros) {
            throw invalido("El campo " + campo + " no puede tener más de " + maxEnteros
                    + " dígitos enteros");
        }
        BigDecimal numero = new BigDecimal(texto);
        if (numero.signum() <= 0) {
            throw invalido("El campo " + campo + " debe ser mayor que cero");
        }
        return numero;
    }

    private static ExcepcionMonster invalido(String mensaje) {
        return new ExcepcionMonster(CodigoError.DATA_001, mensaje);
    }
}
