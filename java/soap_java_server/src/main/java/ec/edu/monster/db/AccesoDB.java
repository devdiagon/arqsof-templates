package ec.edu.monster.db;

import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Entrega conexiones abiertas a la base de datos del proyecto.
 * La URL, el usuario y la contraseña son constantes en texto claro (entorno local académico):
 * es lo único que se cambia al copiar esta clase a un proyecto nuevo.
 * Cada operación pide su propia conexión y quien la pide la cierra.
 */
@ApplicationScoped
public class AccesoDB {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/ejemplo"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USUARIO = "ejemplo";
    private static final String CLAVE = "admin";

    /**
     * Abre una conexión nueva. Es un método de instancia para poder sustituirlo en las pruebas.
     */
    public Connection obtenerConexion() throws SQLException {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver JDBC de MySQL", e);
        }
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
