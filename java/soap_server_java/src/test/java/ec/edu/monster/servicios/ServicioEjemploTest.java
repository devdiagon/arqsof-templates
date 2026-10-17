package ec.edu.monster.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionNegocio;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/** La lógica se prueba sin servidor ni SOAP (requisito: separar el cálculo de los puntos de exposición). */
class ServicioEjemploTest {

    private final ServicioEjemplo servicio = new ServicioEjemplo();

    @Test
    void ecoDevuelveElTextoSinEspaciosSobrantes() {
        assertEquals("hola", servicio.eco("  hola "));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void ecoSinTextoLanzaDATO_001(String texto) {
        ExcepcionNegocio e = assertThrows(ExcepcionNegocio.class, () -> servicio.eco(texto));
        assertEquals(CodigoError.DATO_001, e.getCodigoError());
        assertEquals("El texto es obligatorio", e.getMessage());
    }

    @Test
    void listarDevuelveLosElementosEnOrden() {
        assertEquals(List.of("Uno", "Dos", "Tres"), servicio.listar());
    }
}
