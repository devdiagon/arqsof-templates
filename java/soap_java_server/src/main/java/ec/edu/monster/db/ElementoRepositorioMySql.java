package ec.edu.monster.db;

import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionMonster;
import ec.edu.monster.modelo.Elemento;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ElementoRepositorioMySql implements ElementoRepositorio {

    private static final int MYSQL_CLAVE_DUPLICADA = 1062;

    private static final String SQL_POR_CODIGO =
            "SELECT chr_elecodigo, vch_elenombre, dec_elevalor FROM elemento WHERE chr_elecodigo = ?";
    private static final String SQL_LISTAR =
            "SELECT chr_elecodigo, vch_elenombre, dec_elevalor FROM elemento ORDER BY chr_elecodigo";
    private static final String SQL_INSERTAR =
            "INSERT INTO elemento (chr_elecodigo, vch_elenombre, dec_elevalor) VALUES (?, ?, ?)";

    @Override
    public Optional<Elemento> buscarPorCodigo(Connection conexion, String codigo) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_POR_CODIGO)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Elemento> listar(Connection conexion) throws SQLException {
        List<Elemento> elementos = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                elementos.add(mapear(rs));
            }
        }
        return elementos;
    }

    @Override
    public void insertar(Connection conexion, Elemento elemento) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR)) {
            ps.setString(1, elemento.codigo());
            ps.setString(2, elemento.nombre());
            ps.setBigDecimal(3, elemento.valor());
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == MYSQL_CLAVE_DUPLICADA) {
                throw new ExcepcionMonster(CodigoError.ELE_002);
            }
            throw e;
        }
    }

    private Elemento mapear(ResultSet rs) throws SQLException {
        return new Elemento(
                rs.getString("chr_elecodigo"),
                rs.getString("vch_elenombre"),
                rs.getBigDecimal("dec_elevalor"));
    }
}
