package ec.edu.monster.api.contrato;

import jakarta.json.bind.annotation.JsonbPropertyOrder;

/**
 * EJEMPLO de elemento anidado en una lista: todos los campos son texto (R9).
 */
@JsonbPropertyOrder({"codigoElemento", "nombre", "valor"})
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
