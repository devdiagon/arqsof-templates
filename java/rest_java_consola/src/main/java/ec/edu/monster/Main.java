package ec.edu.monster;

import ec.edu.monster.api.ConfiguracionApi;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Punto de composición: crea el cliente de la API, los servicios, las vistas y los controladores, y ejecuta el bucle.
 *
 * @author: Frederick Tipán
 */
public class Main {

    public static void main(String[] args) {
        // Las tildes y la ñ salen bien si la terminal también está en UTF-8 (chcp 65001 en cmd)
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        try {
            // TODO: cliente = XClienteApi.crear();   (README §4)
            //       crear servicios, vistas y controladores, y alternar según el enum Resultado de cada controlador
            System.out.println("Plantilla lista. API: " + ConfiguracionApi.urlBase());
        } catch (java.util.NoSuchElementException e) {
            System.out.println(); // entrada cerrada: fin del programa
        } catch (RuntimeException e) {
            System.err.println("No fue posible iniciar el cliente: " + e.getMessage());
        }
    }
}
