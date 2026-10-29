package ec.edu.monster.ws;

import ec.edu.monster.dto.Resultado;
import ec.edu.monster.modelo.Elemento;
import ec.edu.monster.ws.contrato.ElementoDetalle;
import ec.edu.monster.ws.contrato.RespuestaElemento;
import ec.edu.monster.ws.contrato.RespuestaElementos;
import java.util.ArrayList;
import java.util.List;

/**
 * EJEMPLO de mapeador: convierte Resultado en tipos de contrato, pasando los tipos reales a texto.
 */
public final class ElementoMapeador {

    private ElementoMapeador() {
    }

    public static RespuestaElemento aRespuestaElemento(Resultado<Elemento> resultado) {
        RespuestaElemento respuesta = new RespuestaElemento();
        respuesta.aplicarEnvoltura(resultado.estado().name(), resultado.codigo(), resultado.mensaje());
        Elemento datos = resultado.datos();
        if (datos != null) {
            respuesta.cargarDatos(datos.codigo(), datos.nombre(), ConversorTexto.decimal(datos.valor()));
        }
        return respuesta;
    }

    public static RespuestaElementos aRespuestaElementos(Resultado<List<Elemento>> resultado) {
        RespuestaElementos respuesta = new RespuestaElementos();
        respuesta.aplicarEnvoltura(resultado.estado().name(), resultado.codigo(), resultado.mensaje());
        if (resultado.datos() != null) {
            List<ElementoDetalle> detalles = new ArrayList<>();
            for (Elemento e : resultado.datos()) {
                detalles.add(new ElementoDetalle(e.codigo(), e.nombre(), ConversorTexto.decimal(e.valor())));
            }
            respuesta.cargarDatos(detalles);
        }
        return respuesta;
    }
}
