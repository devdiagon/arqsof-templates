package ec.edu.monster.api;

import ec.edu.monster.api.contrato.ConvencionRest;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Activa JAX-RS con el prefijo común de la API (R1). Los recursos y el manejador de errores
 * se descubren por escaneo del WAR.
 */
@ApplicationPath(ConvencionRest.PREFIJO)
public class AplicacionRest extends Application {
}
