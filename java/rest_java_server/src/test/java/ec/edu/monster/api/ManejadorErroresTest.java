package ec.edu.monster.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionMonster;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

/**
 * Clasificación de excepciones. Las excepciones de JAX-RS se simulan con Mockito porque construirlas
 * exige la implementación de JAX-RS del servidor; la respuesta HTTP completa se verifica a mano.
 */
class ManejadorErroresTest {

    @Test
    void unaExcepcionDeNegocioSePasaTalCual() {
        ExcepcionMonster original = new ExcepcionMonster(CodigoError.ELE_001);

        assertSame(original, ManejadorErrores.clasificar(original));
    }

    @Test
    void rutaInexistenteEsErr404() {
        ExcepcionMonster e = ManejadorErrores.clasificar(mock(NotFoundException.class));

        assertEquals(CodigoError.ERR_404, e.getError());
        assertEquals("Ruta no encontrada", e.getMessage());
    }

    @Test
    void verboNoPermitidoEsErr405() {
        ExcepcionMonster e = ManejadorErrores.clasificar(mock(NotAllowedException.class));

        assertEquals(CodigoError.ERR_405, e.getError());
        assertEquals("Método no permitido", e.getMessage());
    }

    @Test
    void contentTypeIncorrectoEsErr415() {
        ExcepcionMonster e = ManejadorErrores.clasificar(mock(NotSupportedException.class));

        assertEquals(CodigoError.ERR_415, e.getError());
        assertEquals("Tipo de contenido no soportado", e.getMessage());
    }

    @Test
    void unaPeticionMalFormadaEsData001() {
        ExcepcionMonster e = ManejadorErrores.clasificar(mock(BadRequestException.class));

        assertEquals(CodigoError.DATA_001, e.getError());
        assertEquals("Cuerpo de la petición inválido", e.getMessage());
    }

    @Test
    void cualquierOtraExcepcionEsErr500SinDetallesInternos() {
        ExcepcionMonster otra = ManejadorErrores.clasificar(new IllegalStateException("detalle interno"));
        ExcepcionMonster web = ManejadorErrores.clasificar(mock(WebApplicationException.class));

        assertEquals(CodigoError.ERR_500, otra.getError());
        assertEquals("Error interno del servidor", otra.getMessage());
        assertEquals(CodigoError.ERR_500, web.getError());
    }
}
