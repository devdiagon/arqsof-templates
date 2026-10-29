package ec.edu.monster.ws.contrato;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * EJEMPLO de elemento anidado en una lista (R7): todos los campos son texto.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ElementoDetalle", propOrder = {"codigoElemento", "nombre", "valor"})
public class ElementoDetalle {

    private String codigoElemento;
    private String nombre;
    private String valor;

    public ElementoDetalle() {
    }

    public ElementoDetalle(String codigoElemento, String nombre, String valor) {
        this.codigoElemento = codigoElemento;
        this.nombre = nombre;
        this.valor = valor;
    }

    public String getCodigoElemento() {
        return codigoElemento;
    }

    public String getNombre() {
        return nombre;
    }

    public String getValor() {
        return valor;
    }
}
