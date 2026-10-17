package ec.edu.monster.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * EJEMPLO de respuesta con una lista (regla R7): un elemento contenedor con el elemento repetido dentro,
 * {@code <elementos><elemento>..</elemento>..</elementos>}. La lista se inicializa vacía: en caso de error llega
 * vacía, nunca ausente.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RespuestaLista", propOrder = {"elementos"})
public class RespuestaListaDto extends RespuestaBaseDto {

    @XmlElementWrapper(name = "elementos")
    @XmlElement(name = "elemento")
    private List<String> elementos = new ArrayList<>();

    public RespuestaListaDto() {}

    public List<String> getElementos() {
        return elementos;
    }

    public void setElementos(List<String> elementos) {
        this.elementos = elementos;
    }
}
