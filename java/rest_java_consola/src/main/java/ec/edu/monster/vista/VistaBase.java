package ec.edu.monster.vista;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * @author: Frederick Tipán
 */
public abstract class VistaBase {

    private static final Scanner TECLADO = new Scanner(System.in, StandardCharsets.UTF_8);

    public String pedir(String etiqueta) {
        System.out.print(etiqueta + ": ");
        return TECLADO.hasNextLine() ? TECLADO.nextLine().trim() : "";
    }

    /** Muestra una lista numerada y devuelve el indice elegido (0 basado), o -1 si cancela. */
    protected int elegir(String titulo, List<String> opciones) {
        System.out.println(titulo);
        for (int i = 0; i < opciones.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, opciones.get(i));
        }
        System.out.println("  0. Cancelar");
        while (true) {
            String entrada = pedir("Opción");
            try {
                int n = Integer.parseInt(entrada);
                if (n == 0) {
                    return -1;
                }
                if (n >= 1 && n <= opciones.size()) {
                    return n - 1;
                }
            } catch (NumberFormatException ignorada) {
                // se vuelve a pedir
            }
            mostrarError("Opción inválida");
        }
    }

    public void mostrar(String mensaje) {
        System.out.println(mensaje);
    }

    public void mostrarError(String mensaje) {
        System.out.println("[!] " + mensaje);
    }
}
