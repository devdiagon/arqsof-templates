package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * EJEMPLO de entidad (tabla elemento). Sustituir por las entidades del dominio.
 * Los formatos del dominio se declaran aquí como constantes y el controlador los combina con ValidadorForma.
 */
public record Elemento(
        String codigo,
        String nombre,
        BigDecimal valor) {

    public static final int LONGITUD_CODIGO = 4;
    public static final int LONGITUD_MAXIMA_NOMBRE = 40;
    public static final int MAX_ENTEROS_VALOR = 10;
    public static final int MAX_DECIMALES_VALOR = 2;
}
