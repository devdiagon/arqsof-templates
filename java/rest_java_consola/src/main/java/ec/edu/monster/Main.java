package ec.edu.monster;

import ec.edu.monster.api.ClienteApi;
import ec.edu.monster.api.ClienteApiHttp;
import ec.edu.monster.modelo.Sesion;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Punto de composición: crea la sesión, el transporte, el servicio, las vistas y los controladores, y ejecuta el bucle.
 *
 * @author: Frederick Tipán
 */
public class Main {

    public static void main(String[] args) {
        // Las tildes y la ñ salen bien si la terminal también está en UTF-8 (chcp 65001 en cmd)
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        try {
            Sesion sesion = new Sesion();
            ClienteApi api = new ClienteApiHttp();
            // TODO: crear servicio(api, sesion), vistas y controladores, y alternar según su enum Resultado
            System.out.println("Plantilla lista. API: " + ClienteApiHttp.baseConfigurada()
                    + " (sesion: " + (sesion.getToken() != null) + ", transporte: " + api.getClass().getSimpleName() + ")");
        } catch (RuntimeException e) {
            System.err.println("No fue posible iniciar el cliente: " + e.getMessage());
        }
    }
}
