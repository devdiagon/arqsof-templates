package ec.edu.monster.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Parte común de toda respuesta, de éxito o de error (regla R6): {@code estado} (OK/ERROR), {@code codigo} y
 * {@code mensaje}. Los tipos de cada operación la extienden y añaden sus campos DESPUÉS, en orden fijo.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RespuestaBase", propOrder = {"estado", "codigo", "mensaje"})
public class RespuestaBaseDto {

    private String estado;
    private String codigo;
    private String mensaje;

    public RespuestaBaseDto() {}

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
