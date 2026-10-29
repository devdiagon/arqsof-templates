package ec.edu.monster.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ec.edu.monster.db.AccesoDB;
import ec.edu.monster.db.ElementoRepositorio;
import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionMonster;
import ec.edu.monster.modelo.Elemento;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * EJEMPLO de prueba de servicio con la base de datos simulada: se comprueba qué se pide al repositorio y
 * cómo termina la transacción. Sin servidor ni red.
 */
@ExtendWith(MockitoExtension.class)
class ElementoServicioTest {

    private static final Elemento PRIMERO = new Elemento("0001", "Primero", new BigDecimal("10.50"));

    @Mock
    private AccesoDB accesoDB;
    @Mock
    private Connection conexion;
    @Mock
    private ElementoRepositorio repositorio;

    private ElementoServicio servicio;

    @BeforeEach
    void preparar() throws Exception {
        servicio = new ElementoServicio(accesoDB, repositorio);
        lenient().when(accesoDB.obtenerConexion()).thenReturn(conexion);
    }

    @Test
    void consultarDevuelveElElementoYCierraLaConexion() throws Exception {
        when(repositorio.buscarPorCodigo(conexion, "0001")).thenReturn(Optional.of(PRIMERO));

        assertSame(PRIMERO, servicio.consultar("0001"));
        verify(conexion).close();
    }

    @Test
    void consultarUnElementoInexistenteLanzaEle001() throws Exception {
        when(repositorio.buscarPorCodigo(conexion, "9999")).thenReturn(Optional.empty());

        ExcepcionMonster e = assertThrows(ExcepcionMonster.class, () -> servicio.consultar("9999"));

        assertEquals(CodigoError.ELE_001, e.getError());
        verify(conexion).close();
    }

    @Test
    void listarDevuelveLosElementosDelRepositorio() throws Exception {
        when(repositorio.listar(conexion)).thenReturn(List.of(PRIMERO));

        assertEquals(List.of(PRIMERO), servicio.listar());
    }

    @Test
    void registrarInsertaConfirmaYCierra() throws Exception {
        when(repositorio.buscarPorCodigo(conexion, "0004")).thenReturn(Optional.empty());

        Elemento nuevo = servicio.registrar("0004", "Cuarto", new BigDecimal("5"));

        assertEquals(new BigDecimal("5.00"), nuevo.valor());
        InOrder orden = inOrder(conexion, repositorio);
        orden.verify(conexion).setAutoCommit(false);
        orden.verify(repositorio).insertar(conexion, nuevo);
        orden.verify(conexion).commit();
        orden.verify(conexion).close();
        verify(conexion, never()).rollback();
    }

    @Test
    void registrarUnCodigoExistenteRevierteYLanzaEle002() throws Exception {
        when(repositorio.buscarPorCodigo(conexion, "0001")).thenReturn(Optional.of(PRIMERO));

        ExcepcionMonster e = assertThrows(ExcepcionMonster.class,
                () -> servicio.registrar("0001", "Otro", BigDecimal.ONE));

        assertEquals(CodigoError.ELE_002, e.getError());
        verify(repositorio, never()).insertar(any(), any());
        verify(conexion).rollback();
        verify(conexion, never()).commit();
        verify(conexion).close();
    }

    @Test
    void unFalloDeLaBaseDeDatosRevierteYContinuaHaciaElControlador() throws Exception {
        when(repositorio.buscarPorCodigo(conexion, "0004")).thenReturn(Optional.empty());
        doThrow(new SQLException("falla")).when(repositorio).insertar(any(), any());

        assertThrows(SQLException.class, () -> servicio.registrar("0004", "Cuarto", BigDecimal.ONE));

        verify(conexion).rollback();
        verify(conexion).close();
    }

    @Test
    void elValorSeRedondeaADosDecimales() {
        assertEquals(new BigDecimal("1.01"), ElementoServicio.redondearValor(new BigDecimal("1.005")));
        assertEquals(new BigDecimal("2.00"), ElementoServicio.redondearValor(new BigDecimal("2")));
    }
}
