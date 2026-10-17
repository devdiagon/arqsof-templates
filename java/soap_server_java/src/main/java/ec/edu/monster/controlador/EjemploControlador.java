package ec.edu.monster.controlador;

import ec.edu.monster.dto.FabricaRespuestas;
import ec.edu.monster.dto.RespuestaEcoDto;
import ec.edu.monster.dto.RespuestaListaDto;
import ec.edu.monster.servicios.ServicioEjemplo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * EJEMPLO de controlador: coordina (validar token -> servicio -> respuesta) y traduce excepciones a respuestas.
 * En un servicio con sesión, aquí se valida el token antes de llamar al servicio.
 */
@ApplicationScoped
public class EjemploControlador extends ControladorBase {

    private ServicioEjemplo servicio;

    // Constructor sin argumentos exigido por CDI para el proxy de @ApplicationScoped.
    EjemploControlador() {}

    @Inject
    public EjemploControlador(ServicioEjemplo servicio) {
        this.servicio = servicio;
    }

    public RespuestaEcoDto eco(String texto) {
        return ejecutar(() -> FabricaRespuestas.okEco(servicio.eco(texto)), FabricaRespuestas::errorEco);
    }

    public RespuestaListaDto listar() {
        return ejecutar(() -> FabricaRespuestas.okLista(servicio.listar()), FabricaRespuestas::errorLista);
    }
}
