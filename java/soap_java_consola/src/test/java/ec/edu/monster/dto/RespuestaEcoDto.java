package ec.edu.monster.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * DTO de EJEMPLO, solo para las pruebas de conformidad (src/test). Está en el MISMO paquete que los DTO reales para
 * que le apliquen {@code package-info.java} y el namespace del servicio. Es el modelo de un DTO del dominio:
 * extiende {@link RespuestaBaseDto}, {@code @XmlType(name = ...)} con el nombre del complexType del WSDL.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RespuestaEco", namespace = EspacioNombres.SERVICIO)
public class RespuestaEcoDto extends RespuestaBaseDto {

    protected String texto;

    public String getTexto() { return texto; }

    public void setTexto(String texto) { this.texto = texto; }
}
