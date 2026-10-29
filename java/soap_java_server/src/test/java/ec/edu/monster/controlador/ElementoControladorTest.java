package ec.edu.monster.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ec.edu.monster.dto.Resultado;
import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionMonster;
import ec.edu.monster.modelo.Elemento;
import ec.edu.monster.servicios.ElementoServicio;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * EJEMPLO de prueba de controlador: valida la forma, convierte y traduce errores a Resultado. Sin servidor.
 */
@ExtendWith(MockitoExtension.class)
class ElementoControladorTest {

    @Mock
    private ElementoServicio servicio;
    @InjectMocks
    private ElementoControlador controlador;

    @Test
    void consultarConvierteLaEntradaYDevuelveElResultadoOk() throws Exception {
        Elemento elemento = new Elemento("0001", "Primero", new BigDecimal("10.50"));
        when(servicio.consultar("0001")).thenReturn(elemento);

        Resultado<Elemento> resultado = controlador.consultar(" 0001 ");

        assertEquals(Resultado.Estado.OK, resultado.estado());
        assertEquals(elemento, resultado.datos());
    }

    @Test
    void unaEntradaMalFormadaDevuelveData001SinLlamarAlServicio() {
        Resultado<Elemento> resultado = controlador.consultar("12");

        assertEquals("DATA-001", resultado.codigo());
        assertNull(resultado.datos());
        verifyNoInteractions(servicio);
    }

    @Test
    void registrarValidaElValorAntesDeLlamarAlServicio() {
        Resultado<Elemento> resultado = controlador.registrar("0004", "Cuarto", "10.999");

        assertEquals("DATA-001", resultado.codigo());
        verifyNoInteractions(servicio);
    }

    @Test
    void registrarEntregaAlServicioLosTiposReales() throws Exception {
        controlador.registrar("0004", " Cuarto ", "5.5");

        verify(servicio).registrar("0004", "Cuarto", new BigDecimal("5.5"));
    }

    @Test
    void unErrorDeNegocioDelServicioSeConvierteEnResultadoDeError() throws Exception {
        when(servicio.consultar(any())).thenThrow(new ExcepcionMonster(CodigoError.ELE_001));

        Resultado<Elemento> resultado = controlador.consultar("9999");

        assertEquals(Resultado.Estado.ERROR, resultado.estado());
        assertEquals("ELE-001", resultado.codigo());
    }
}
