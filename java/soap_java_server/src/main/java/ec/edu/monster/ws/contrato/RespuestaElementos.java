package ec.edu.monster.ws.contrato;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * EJEMPLO de respuesta con lista (R7): contenedor con el elemento repetido dentro. La lista se inicializa vacía:
 * en caso de error llega vacía, nunca ausente.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RespuestaElementos", propOrder = {"elementos"})
public class RespuestaElementos extends RespuestaBase {

    @XmlElementWrapper(name = "elementos")
    @XmlElement(name = "elemento")
    private List<ElementoDetalle> elementos = new ArrayList<>();

    public RespuestaElementos() {
    }

    public void cargarDatos(List<ElementoDetalle> elementos) {
        this.elementos = elementos;
    }

    public List<ElementoDetalle> getElementos() {
        return elementos;
    }
}
