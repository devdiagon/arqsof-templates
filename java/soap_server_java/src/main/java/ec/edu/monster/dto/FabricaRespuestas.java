package ec.edu.monster.dto;

import ec.edu.monster.excepciones.ExcepcionNegocio;
import ec.edu.monster.utilidades.ConfiguracionServidor;
import java.util.List;

/**
 * Único lugar que decide estado, código y mensaje de las respuestas. Los errores conservan el tipo de respuesta de
 * la operación para que el WSDL sea el mismo en éxito y en error (regla R6).
 */
public final class FabricaRespuestas {

    private FabricaRespuestas() {}

    public static RespuestaEcoDto okEco(String texto) {
        RespuestaEcoDto dto = new RespuestaEcoDto();
        marcarOk(dto);
        dto.setTexto(texto);
        return dto;
    }

    public static RespuestaListaDto okLista(List<String> elementos) {
        RespuestaListaDto dto = new RespuestaListaDto();
        marcarOk(dto);
        dto.setElementos(elementos);
        return dto;
    }

    public static RespuestaEcoDto errorEco(ExcepcionNegocio e) {
        RespuestaEcoDto dto = new RespuestaEcoDto();
        marcarError(dto, e);
        return dto;
    }

    public static RespuestaListaDto errorLista(ExcepcionNegocio e) {
        RespuestaListaDto dto = new RespuestaListaDto();
        marcarError(dto, e);
        return dto;
    }

    private static void marcarOk(RespuestaBaseDto dto) {
        dto.setEstado(ConfiguracionServidor.ESTADO_OK);
        dto.setCodigo(ConfiguracionServidor.CODIGO_OK);
        dto.setMensaje(ConfiguracionServidor.MENSAJE_OK);
    }

    private static void marcarError(RespuestaBaseDto dto, ExcepcionNegocio e) {
        dto.setEstado(ConfiguracionServidor.ESTADO_ERROR);
        dto.setCodigo(e.getCodigoError().getCodigo());
        dto.setMensaje(e.getMessage());
    }
}
