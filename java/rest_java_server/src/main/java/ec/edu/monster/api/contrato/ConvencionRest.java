package ec.edu.monster.api.contrato;

/**
 * Constantes de la convención REST (CONVENCION_REST.md), en un solo lugar.
 */
public final class ConvencionRest {

    /** Prefijo común de la API, siempre después del context root (R1). */
    public static final String PREFIJO = "api";

    /** Tipo de contenido de toda respuesta (R3). */
    public static final String JSON_UTF8 = "application/json; charset=utf-8";

    /** Mensaje del DATA-001 por cuerpo ausente, mal formado o que no es un objeto JSON. */
    public static final String MENSAJE_CUERPO_INVALIDO = "Cuerpo de la petición inválido";

    private ConvencionRest() {
    }
}
