package ec.edu.monster.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * EJEMPLO de respuesta con un campo propio de tipo texto. Los campos opcionales se dejan nulos en caso de error
 * y JAXB omite el elemento (regla R7). El nombre del tipo ({@code name}) forma parte del contrato (regla R5).
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RespuestaEco", propOrder = {"texto"})
public class RespuestaEcoDto extends RespuestaBaseDto {

    private String texto;

    public RespuestaEcoDto() {}

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
