package ec.edu.monster.api;

/**
 * Respuesta cruda del servidor: código HTTP y cuerpo (JSON, o cualquier texto si es una página de error).
 *
 * @author: Frederick Tipán
 */
public record RespuestaHttp(int codigo, String cuerpo) {

    public boolean esExito() {
        return codigo >= 200 && codigo < 300;
    }
}
