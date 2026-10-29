package ec.edu.monster.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.dto.Resultado;
import ec.edu.monster.modelo.Elemento;
import ec.edu.monster.api.contrato.RespuestaElemento;
import ec.edu.monster.api.contrato.RespuestaElementos;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ElementoMapeadorTest {

    @Test
    void unElementoExitosoSeConvierteATextoConLaEnvoltura() {
        RespuestaElemento r = ElementoMapeador.aRespuestaElemento(
                Resultado.exito(new Elemento("0001", "Primero", new BigDecimal("10.5"))));

        assertEquals("OK", r.getEstado());
        assertEquals("200", r.getCodigo());
        assertEquals("0001", r.getCodigoElemento());
        assertEquals("10.50", r.getValor());
    }

    @Test
    void unErrorNoCargaDatosPeroConservaCodigoYMensaje() {
        RespuestaElemento r = ElementoMapeador.aRespuestaElemento(Resultado.error("ELE-001", "El elemento no existe"));

        assertEquals("ERROR", r.getEstado());
        assertEquals("ELE-001", r.getCodigo());
        assertEquals("El elemento no existe", r.getMensaje());
        assertNull(r.getCodigoElemento());
    }

    @Test
    void laListaSeConvierteElementoAElemento() {
        RespuestaElementos r = ElementoMapeador.aRespuestaElementos(Resultado.exito(List.of(
                new Elemento("0001", "A", BigDecimal.ONE), new Elemento("0002", "B", BigDecimal.TEN))));

        assertEquals(2, r.getElementos().size());
        assertEquals("10.00", r.getElementos().get(1).getValor());
    }

    @Test
    void unErrorDevuelveLaListaVaciaPeroNuncaNula() {
        RespuestaElementos r = ElementoMapeador.aRespuestaElementos(Resultado.error("ERR-500", "Error"));

        assertNotNull(r.getElementos());
        assertTrue(r.getElementos().isEmpty());
    }
}
