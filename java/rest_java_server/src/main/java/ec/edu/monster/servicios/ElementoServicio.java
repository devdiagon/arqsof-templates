package ec.edu.monster.servicios;

import ec.edu.monster.db.AccesoDB;
import ec.edu.monster.db.ElementoRepositorio;
import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionMonster;
import ec.edu.monster.modelo.Elemento;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EJEMPLO de servicio: reglas de negocio, cálculos puros y el patrón único de conexión y transacción
 * ({@code enTransaccion}). No conoce el protocolo ni los tipos de contrato.
 */
public class ElementoServicio {

    private static final Logger LOGGER = Logger.getLogger(ElementoServicio.class.getName());

    private final AccesoDB accesoDB;
    private final ElementoRepositorio elementoRepositorio;

    @Inject
    public ElementoServicio(AccesoDB accesoDB, ElementoRepositorio elementoRepositorio) {
        this.accesoDB = accesoDB;
        this.elementoRepositorio = elementoRepositorio;
    }

    // ------------------------------------------------------------------ operaciones

    public Elemento consultar(String codigo) throws SQLException {
        try (Connection conexion = accesoDB.obtenerConexion()) {
            return elementoRepositorio.buscarPorCodigo(conexion, codigo)
                    .orElseThrow(() -> new ExcepcionMonster(CodigoError.ELE_001));
        }
    }

    public List<Elemento> listar() throws SQLException {
        try (Connection conexion = accesoDB.obtenerConexion()) {
            return elementoRepositorio.listar(conexion);
        }
    }

    public Elemento registrar(String codigo, String nombre, BigDecimal valor) throws SQLException {
        return enTransaccion(conexion -> {
            if (elementoRepositorio.buscarPorCodigo(conexion, codigo).isPresent()) {
                throw new ExcepcionMonster(CodigoError.ELE_002);
            }
            Elemento elemento = new Elemento(codigo, nombre, redondearValor(valor));
            elementoRepositorio.insertar(conexion, elemento);
            return elemento;
        });
    }

    // ------------------------------------------------------------------ cálculos (sin acceso a datos)

    public static BigDecimal redondearValor(BigDecimal valor) {
        return valor.setScale(Elemento.MAX_DECIMALES_VALOR, RoundingMode.HALF_UP);
    }

    // ------------------------------------------------------------------ transacción

    @FunctionalInterface
    private interface OperacionTransaccional<T> {
        T ejecutar(Connection conexion) throws SQLException;
    }

    /**
     * Patrón único de conexión y transacción: abre la conexión, ejecuta la operación,
     * confirma si todo salió bien, revierte si hubo una excepción (que continúa hacia
     * el controlador) y cierra siempre la conexión.
     */
    private <T> T enTransaccion(OperacionTransaccional<T> operacion) throws SQLException {
        try (Connection conexion = accesoDB.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(conexion);
                conexion.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                revertir(conexion);
                throw e;
            }
        }
    }

    private void revertir(Connection conexion) {
        try {
            conexion.rollback();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "No se pudo revertir la transacción", e);
        }
    }
}
