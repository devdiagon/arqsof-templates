package ec.edu.monster.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Forma común de toda respuesta del servidor (regla R6 de CONVENCION_SOAP.md): {@code estado} (OK/ERROR),
 * {@code codigo} y {@code mensaje}. Los DTO de cada operación la extienden y se declaran en {@code @XmlSeeAlso}.
 * El nombre del tipo ({@code name}) es el del complexType del WSDL: forma parte del contrato (R5), no del código Java.
 * El namespace sale de {@code package-info.java} ({@link EspacioNombres#SERVICIO}).
 *
 * @author: Frederick Tipán
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RespuestaBase", namespace = EspacioNombres.SERVICIO)
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
