package ec.edu.monster.api.contrato;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * EJEMPLO de respuesta con lista (R5): la lista se inicializa vacía, de modo que en caso de error
 * o sin resultados llega como {@code []}, nunca ausente.
 */
@JsonbPropertyOrder({"estado", "codigo", "mensaje", "elementos"})
public class RespuestaElementos extends RespuestaBase {

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
