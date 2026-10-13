package ec.edu.monster.ws;

import ec.edu.monster.dto.RespuestaBaseDto;
import ec.edu.monster.dto.RespuestaLoginDto;

/**
 * Doble de prueba del contrato SOAP (sin red). El login acepta solo el usuario "admin".
 * DOMINIO: agregar aqui las operaciones nuevas del contrato con respuestas simuladas.
 */
public class ContratoFalso implements ContratoWS {

    public static <T extends RespuestaBaseDto> T respuesta(T r, String estado, String codigo, String mensaje) {
        r.setEstado(estado);
        r.setCodigo(codigo);
        r.setMensaje(mensaje);
        return r;
    }

    @Override
    public RespuestaLoginDto login(String usuario, String contrasenia) {
        RespuestaLoginDto r = "admin".equals(usuario)
                ? respuesta(new RespuestaLoginDto(), "OK", "200", "ok")
                : respuesta(new RespuestaLoginDto(), "ERROR", "AUTH-001", "Usuario o contrasenia incorrectos");
        r.setToken("T1");
        return r;
    }
}
