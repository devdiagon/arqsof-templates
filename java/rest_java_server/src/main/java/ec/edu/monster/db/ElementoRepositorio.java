package ec.edu.monster.db;

import ec.edu.monster.modelo.Elemento;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * EJEMPLO de persistencia de una entidad.
 */
public interface ElementoRepositorio {

    Optional<Elemento> buscarPorCodigo(Connection conexion, String codigo) throws SQLException;

    List<Elemento> listar(Connection conexion) throws SQLException;

    /**
     * Inserta el elemento. Una clave duplicada se traduce a ELE-002 (Conflicto).
     */
    void insertar(Connection conexion, Elemento elemento) throws SQLException;
}
