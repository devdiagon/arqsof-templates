package ec.edu.monster.dto;

/**
 * Estructura neutral que devuelve todo controlador: estado, código, mensaje y,
 * si hubo éxito, los datos. No conoce cómo se serializa.
 *
 * @param <T> tipo de los datos devueltos en caso de éxito
 */
public record Resultado<T>(Estado estado, String codigo, String mensaje, T datos) {

    public static final String CODIGO_EXITO = "200";
    public static final String MENSAJE_EXITO = "Operación exitosa";

    public enum Estado {
        OK, ERROR
    }

    public static <T> Resultado<T> exito(T datos) {
        return new Resultado<>(Estado.OK, CODIGO_EXITO, MENSAJE_EXITO, datos);
    }

    public static <T> Resultado<T> error(String codigo, String mensaje) {
        return new Resultado<>(Estado.ERROR, codigo, mensaje, null);
    }
}
