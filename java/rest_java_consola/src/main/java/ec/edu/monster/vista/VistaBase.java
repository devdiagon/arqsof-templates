package ec.edu.monster.vista;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * Única capa que lee el teclado y escribe en la consola.
 *
 * @author: Frederick Tipán
 */
public abstract class VistaBase {

    private static final Scanner TECLADO = new Scanner(System.in, StandardCharsets.UTF_8);

    /**
     * Pide un dato y lo devuelve recortado. Si la entrada se cierra (Ctrl+Z / Ctrl+D o {@code stdin} agotado) lanza
     * {@link java.util.NoSuchElementException}: el bucle principal la trata como fin del programa en lugar de girar
     * indefinidamente en un menú.
     */
    public String pedir(String etiqueta) {
        System.out.print(etiqueta + ": ");
        return TECLADO.nextLine().trim();
    }

    /** Muestra una lista numerada y devuelve el indice elegido (0 basado), o -1 si cancela. */
    protected int elegir(String titulo, List<String> opciones) {
        return elegir(titulo, opciones, Textos.CANCELAR);
    }

    /**
     * Igual que {@link #elegir(String, List)}, pero la opción «0» lleva el texto indicado (p. ej. «Salir» o
     * «Cerrar sesión»). Devuelve el indice elegido (0 basado) o -1 si elige «0». Un título vacío no se imprime.
     */
    protected int elegir(String titulo, List<String> opciones, String textoCero) {
        if (!titulo.isEmpty()) {
            System.out.println(titulo);
        }
        for (int i = 0; i < opciones.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, opciones.get(i));
        }
        System.out.printf("  0. %s%n", textoCero);
        while (true) {
            String entrada = pedir(Textos.OPCION);
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
            mostrarError(Textos.OPCION_INVALIDA);
        }
    }

    /** Detiene la pantalla hasta que el usuario pulse Enter. */
    public void pausar() {
        System.out.print(Textos.PULSE_ENTER);
        TECLADO.nextLine();
    }

    /** Línea en blanco, el título y su subrayado. */
    public void mostrarTitulo(String titulo) {
        System.out.println();
        System.out.println(titulo);
        System.out.println("-".repeat(titulo.length()));
    }

    /** Imprime {@link Textos#PORTADA} si tiene contenido. */
    public void mostrarPortada() {
        if (!Textos.PORTADA.isBlank()) {
            mostrar(Textos.PORTADA.stripTrailing());
        }
    }

    public void mostrar(String mensaje) {
        System.out.println(mensaje);
    }

    public void mostrarError(String mensaje) {
        System.out.println("[!] " + mensaje);
    }
}
