package ec.edu.monster.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Frederick Tipán
 */
public class RespuestaListaDto extends RespuestaBaseDto {

    protected List<String> elementos;

    /** Nunca nulo: en errores el servidor envía la lista vacía o no envía el campo. */
    public List<String> getElementos() {
        if (elementos == null) {
            elementos = new ArrayList<>();
        }
        return elementos;
    }
}
