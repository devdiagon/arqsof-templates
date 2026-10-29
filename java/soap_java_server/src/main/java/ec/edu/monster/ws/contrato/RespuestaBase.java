package ec.edu.monster.ws.contrato;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Envoltura común de toda respuesta, de éxito o de error (R6). Los tipos de cada
 * operación la extienden y agregan sus campos propios después.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RespuestaBase", propOrder = {"estado", "codigo", "mensaje"})
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
