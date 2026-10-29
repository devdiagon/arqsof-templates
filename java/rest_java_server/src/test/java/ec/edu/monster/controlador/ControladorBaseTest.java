package ec.edu.monster.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import ec.edu.monster.dto.Resultado;
import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionMonster;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class ControladorBaseTest {

    /** Expone la operación protegida de la base para poder probarla. */
    private static class ControladorDePrueba extends ControladorBase {
        <T> Resultado<T> correr(Operacion<T> operacion) {
            return ejecutar(operacion);
        }
    }

    private final ControladorDePrueba controlador = new ControladorDePrueba();

    @Test
    void unaOperacionExitosaDevuelveResultadoOkConSusDatos() {
        Resultado<String> resultado = controlador.correr(() -> "listo");

        assertEquals(Resultado.Estado.OK, resultado.estado());
        assertEquals("200", resultado.codigo());
        assertEquals("listo", resultado.datos());
    }

    @Test
    void unaExcepcionDeNegocioDevuelveSuCodigoYSuMensaje() {
        Resultado<String> resultado = controlador.correr(() -> {
            throw new ExcepcionMonster(CodigoError.ELE_001);
        });

        assertEquals(Resultado.Estado.ERROR, resultado.estado());
        assertEquals("ELE-001", resultado.codigo());
        assertEquals(CodigoError.ELE_001.getMensaje(), resultado.mensaje());
        assertNull(resultado.datos());
    }

    @Test
    void unaExcepcionDeNegocioConMensajeEspecificoLoConserva() {
        Resultado<String> resultado = controlador.correr(() -> {
            throw new ExcepcionMonster(CodigoError.DATA_001, "El campo valor es obligatorio");
        });

        assertEquals("DATA-001", resultado.codigo());
        assertEquals("El campo valor es obligatorio", resultado.mensaje());
    }

    @Test
    void unaExcepcionInesperadaDevuelveErr500SinFiltrarDetallesInternos() {
        String detalleInterno = "Table 'ejemplo.elemento' doesn't exist (usuario ejemplo)";

        Resultado<String> resultado = controlador.correr(() -> {
            throw new SQLException(detalleInterno);
        });

        assertEquals(Resultado.Estado.ERROR, resultado.estado());
        assertEquals("ERR-500", resultado.codigo());
        assertEquals(CodigoError.ERR_500.getMensaje(), resultado.mensaje());
        assertFalse(resultado.mensaje().contains("ejemplo"));
    }

    @Test
    void unaExcepcionNoControladaDeTiempoDeEjecucionTambienSeTraduceAErr500() {
        Resultado<String> resultado = controlador.correr(() -> {
            throw new NullPointerException("detalle interno");
        });

        assertEquals("ERR-500", resultado.codigo());
        assertEquals(CodigoError.ERR_500.getMensaje(), resultado.mensaje());
    }
}
