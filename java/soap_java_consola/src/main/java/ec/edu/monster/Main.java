package ec.edu.monster;

import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.ws.ClienteWS;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Punto de composición: crea la sesión, el puerto del servicio, el servicio, las vistas y los controladores, y ejecuta el bucle.
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
            // TODO: cliente = new ClienteWSXxx(ClienteWS.configurar(new WSXxx_Service().getWSXxxPort()));   (README §4)
            //       crear servicio(contrato, sesion), vistas y controladores, y alternar según su enum Resultado
            System.out.println("Plantilla lista. WS: " + ClienteWS.urlConfigurada()
                    + " (sesion: " + (sesion.getToken() != null) + ")");
        } catch (RuntimeException e) {
            System.err.println("No fue posible iniciar el cliente: " + e.getMessage());
        }
    }
}
