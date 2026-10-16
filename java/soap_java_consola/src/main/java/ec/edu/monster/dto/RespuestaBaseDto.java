package ec.edu.monster.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Forma común de toda respuesta del servidor. Los DTO de cada operación la extienden
 * y se declaran en @XmlSeeAlso. Cambiar el namespace al del servicio.
 *
 * @author: Frederick Tipán
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RespuestaBase", namespace = "http://CAMBIAR.namespace/")
public class RespuestaBaseDto {

    protected String estado;
    protected String codigo;
    protected String mensaje;

    public String getEstado() { return estado; }
    public String getCodigo() { return codigo; }
    public String getMensaje() { return mensaje; }

    public boolean esOk() { return "OK".equals(estado); }

    public void setEstado(String estado) { this.estado = estado; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
