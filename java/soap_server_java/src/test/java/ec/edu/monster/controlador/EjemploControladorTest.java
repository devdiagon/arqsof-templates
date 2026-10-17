package ec.edu.monster.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.dto.RespuestaEcoDto;
import ec.edu.monster.dto.RespuestaListaDto;
import ec.edu.monster.servicios.ServicioEjemplo;
import java.util.List;
import org.junit.jupiter.api.Test;

class EjemploControladorTest {

    private final EjemploControlador controlador = new EjemploControlador(new ServicioEjemplo());

    @Test
    void exitoResponde_OK_200() {
        RespuestaEcoDto r = controlador.eco("hola");

        assertEquals("OK", r.getEstado());
        assertEquals("200", r.getCodigo());
        assertEquals("Operación realizada correctamente", r.getMensaje());
        assertEquals("hola", r.getTexto());
    }

    @Test
    void errorDeNegocioSeDevuelveComoRespuestaConElMismoTipo() {
        RespuestaEcoDto r = controlador.eco("");

        assertEquals("ERROR", r.getEstado());
        assertEquals("DATO-001", r.getCodigo());
        assertEquals("El texto es obligatorio", r.getMensaje());
        assertNull(r.getTexto(), "los campos propios quedan nulos en error");
    }

    @Test
    void laListaLlegaVaciaYNoNulaEnError() {
        ServicioEjemplo roto = new ServicioEjemplo() {
            @Override
            public List<String> listar() {
                throw new IllegalStateException("detalle-interno");
            }
        };

        RespuestaListaDto r = new EjemploControlador(roto).listar();

        assertEquals("ERROR", r.getEstado());
        assertTrue(r.getElementos().isEmpty());
    }

    @Test
    void unaExcepcionInesperadaSeConvierteEnERR_500SinFiltrarDetalles() {
        ServicioEjemplo roto = new ServicioEjemplo() {
            @Override
            public String eco(String texto) {
                throw new IllegalStateException("detalle-interno");
            }
        };

        RespuestaEcoDto r = new EjemploControlador(roto).eco("x");

        assertEquals("ERROR", r.getEstado());
        assertEquals("ERR-500", r.getCodigo());
        assertEquals("Error interno del servidor", r.getMensaje());
        assertFalse(r.getMensaje().contains("detalle-interno"));
    }
}
