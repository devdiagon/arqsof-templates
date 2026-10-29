package ec.edu.monster.api;

import ec.edu.monster.controlador.ElementoControlador;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * EJEMPLO de recurso REST ({@code api/elementos}). Solo recibe, delega y devuelve.
 * <ul>
 *   <li>El recurso va en plural y todas sus operaciones en una sola clase (R10): dos clases con la misma
 *       ruta raíz pueden entrar en conflicto.</li>
 *   <li>No se declara {@code @Produces}: el tipo de contenido lo fija {@link RespuestasHttp}, para no recibir
 *       un 406 ajeno a la envoltura.</li>
 *   <li>Los parámetros simples pasan como texto al controlador, que valida y convierte.</li>
 *   <li>Todo éxito responde 200, también al crear (CONVENCION_REST.md 2).</li>
 * </ul>
 */
@Path("elementos")
@RequestScoped
public class ElementoRecurso {

    @Inject
    private ElementoControlador controlador;

    public ElementoRecurso() {
    }

    /** Para pruebas, sin contenedor. */
    public ElementoRecurso(ElementoControlador controlador) {
        this.controlador = controlador;
    }

    /** {@code GET elementos} */
    @GET
    public Response listarElementos() {
        return RespuestasHttp.de(ElementoMapeador.aRespuestaElementos(controlador.listar()));
    }

    /** {@code GET elementos/{codigoElemento}} */
    @GET
    @Path("{codigoElemento}")
    public Response consultarElemento(@PathParam("codigoElemento") String codigoElemento) {
        return RespuestasHttp.de(ElementoMapeador.aRespuestaElemento(controlador.consultar(codigoElemento)));
    }

    /** {@code POST elementos} con cuerpo {@code {"codigoElemento": …, "nombre": …, "valor": …}}. */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrarElemento(String cuerpo) {
        CuerpoJson json = CuerpoJson.leer(cuerpo);
        return RespuestasHttp.de(ElementoMapeador.aRespuestaElemento(controlador.registrar(
                json.texto("codigoElemento"), json.texto("nombre"), json.texto("valor"))));
    }
}
