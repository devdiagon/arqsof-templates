package ec.edu.monster.api.contrato;

import jakarta.json.bind.annotation.JsonbPropertyOrder;

/**
 * EJEMPLO de respuesta con campos propios. Un campo propio no puede llamarse estado, codigo ni mensaje
 * (chocaría con la envoltura): por eso el código se publica como {@code codigoElemento}.
 * Los campos solo existen si hubo éxito; JSON-B omite los nulos.
 */
@JsonbPropertyOrder({"estado", "codigo", "mensaje", "codigoElemento", "nombre", "valor"})
public class RespuestaElemento extends RespuestaBase {

    private String codigoElemento;
    private String nombre;
    private String valor;

    public RespuestaElemento() {
    }

    public void cargarDatos(String codigoElemento, String nombre, String valor) {
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
