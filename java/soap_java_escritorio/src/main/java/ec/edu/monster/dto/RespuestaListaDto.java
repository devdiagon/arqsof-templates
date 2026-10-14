package ec.edu.monster.dto;

import ec.edu.monster.ws.ConfiguracionServicio;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Frederick Tipán
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RespuestaLista", namespace = ConfiguracionServicio.NAMESPACE)
public class RespuestaListaDto extends RespuestaBaseDto {

    @XmlElementWrapper(name = "elementos")
    @XmlElement(name = "elemento")
    protected List<String> elementos;

    /** Nunca nulo: en errores el servidor envia la lista vacia o sin el elemento. */
    public List<String> getElementos() {
        if (elementos == null) {
            elementos = new ArrayList<>();
        }
        return elementos;
    }
}
