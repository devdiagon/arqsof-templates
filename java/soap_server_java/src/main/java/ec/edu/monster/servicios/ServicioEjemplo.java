package ec.edu.monster.servicios;

import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionNegocio;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * EJEMPLO de lógica de aplicación. No conoce SOAP, JAX-WS ni los DTO de respuesta: recibe y devuelve tipos simples
 * y lanza {@link ExcepcionNegocio}. Es reutilizable y se prueba con JUnit sin servidor.
 * Sustitúyalo por los servicios del dominio.
 */
@ApplicationScoped
public class ServicioEjemplo {

    public String eco(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ExcepcionNegocio(CodigoError.DATO_001, "El texto es obligatorio");
        }
        return texto.strip();
    }

    public List<String> listar() {
        return List.of("Uno", "Dos", "Tres");
    }
}
