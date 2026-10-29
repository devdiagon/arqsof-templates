package ec.edu.monster.api.contrato;

import jakarta.json.bind.annotation.JsonbPropertyOrder;

/**
 * Envoltura común de toda respuesta, de éxito o de error (R5). Los tipos de cada
 * operación la extienden y agregan sus campos propios después. Los campos sin valor
 * se omiten del JSON.
 * <p>
 * JSON-B ordena las propiedades alfabéticamente por defecto: por eso cada subclase repite en su
 * {@code @JsonbPropertyOrder} {@code "estado", "codigo", "mensaje"} antes de sus campos propios.
 */
@JsonbPropertyOrder({"estado", "codigo", "mensaje"})
public class RespuestaBase {

    private String estado;
    private String codigo;
    private String mensaje;

    public RespuestaBase() {
    }

    public void aplicarEnvoltura(String estado, String codigo, String mensaje) {
        this.estado = estado;
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public String getEstado() {
        return estado;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getMensaje() {
        return mensaje;
    }
}
