package ec.edu.monster.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Frederick Tipán
 */
public class RespuestaListaDto extends RespuestaBaseDto {

    protected List<String> elementos;

    /** Nunca nulo: en errores el servidor envia la lista vacia o sin el elemento. */
    public List<String> getElementos() {
        if (elementos == null) {
            elementos = new ArrayList<>();
        }
        return elementos;
    }
}
