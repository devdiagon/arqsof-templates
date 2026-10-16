package ec.edu.monster.api;

/**
 * Doble de prueba del transporte (sin red). El login acepta solo el usuario "admin" y devuelve el token "T1";
 * cualquier otra peticion responde 404 con un error de negocio.
 * DOMINIO: agregar aqui las rutas nuevas con respuestas simuladas.
 */
public class ApiFalsa implements ClienteApi {

    public static String json(String estado, String codigo, String mensaje) {
        return "{\"estado\":\"" + estado + "\",\"codigo\":\"" + codigo + "\",\"mensaje\":\"" + mensaje + "\"}";
    }

    @Override
    public RespuestaHttp enviar(String metodo, String ruta, String cuerpoJson, String token) {
        if (POST.equals(metodo) && ConfiguracionApi.RUTA_LOGIN.equals(ruta)) {
            if (cuerpoJson != null && cuerpoJson.contains("\"usuario\":\"admin\"")) {
                return new RespuestaHttp(200,
                        "{\"estado\":\"OK\",\"codigo\":\"200\",\"mensaje\":\"ok\",\"token\":\"T1\"}");
            }
            return new RespuestaHttp(401, json("ERROR", "AUTH-001", "Usuario o contrasenia incorrectos"));
        }
        return new RespuestaHttp(404, json("ERROR", "SRV-404", "No existe"));
    }
}
