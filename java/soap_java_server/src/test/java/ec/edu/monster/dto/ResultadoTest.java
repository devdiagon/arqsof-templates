package ec.edu.monster.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ResultadoTest {

    @Test
    void elExitoLlevaEstadoOkCodigo200MensajeFijoYDatos() {
        Resultado<String> resultado = Resultado.exito("datos");

        assertEquals(Resultado.Estado.OK, resultado.estado());
        assertEquals("200", resultado.codigo());
        assertEquals(Resultado.MENSAJE_EXITO, resultado.mensaje());
        assertEquals("datos", resultado.datos());
    }

    @Test
    void elErrorLlevaCodigoMensajeYNingunDato() {
        Resultado<String> resultado = Resultado.error("ELE-001", "El elemento no existe");

        assertEquals(Resultado.Estado.ERROR, resultado.estado());
        assertEquals("ELE-001", resultado.codigo());
        assertEquals("El elemento no existe", resultado.mensaje());
        assertNull(resultado.datos());
    }
}
