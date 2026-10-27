package ec.edu.monster.pruebas;

import ec.edu.monster.excepciones.ExcepcionServicio;
import java.util.Objects;

/**
 * Sustituto de las aserciones de JUnit para las pruebas manuales (JUnit tiene alcance test y no se ve desde
 * src/main). Ejecuta cada prueba sin detener a las demás e imprime el resultado. Las comprobaciones son estáticas
 * para usarlas con import estático.
 */
public class Verificador {

    /** Una prueba: lanza una excepción si algo no se cumple. */
    @FunctionalInterface
    public interface Prueba {
        void correr() throws Exception;
    }

    /** Fallo de una comprobación. */
    public static class FalloPrueba extends RuntimeException {
        public FalloPrueba(String mensaje) {
            super(mensaje);
        }
    }

    private final String titulo;
    private final int primera;
    private int ejecutadas;
    private int correctas;

    public Verificador(String titulo) {
        this(titulo, 1);
    }

    /**
     * @param primera número de la primera prueba, para numerar de corrido entre clases
     */
    public Verificador(String titulo, int primera) {
        this.titulo = titulo;
        this.primera = primera;
        System.out.println("=== " + titulo + " ===");
    }

    /** Corre una prueba, imprime [OK] o [FALLO] con la causa y no detiene a las demás. */
    public void ejecutar(String nombre, Prueba prueba) {
        int numero = primera + ejecutadas;
        ejecutadas++;
        try {
            prueba.correr();
            correctas++;
            System.out.printf("[OK]    %d. %s%n", numero, nombre);
        } catch (Throwable fallo) {
            System.out.printf("[FALLO] %d. %s%n", numero, nombre);
            System.out.println("          -> " + fallo.getClass().getSimpleName() + ": " + fallo.getMessage());
        }
    }

    /** Imprime el resumen y devuelve 0 si todo salió bien o 1 si alguna prueba falló. */
    public int resumen() {
        System.out.printf("--- %s: %d/%d pruebas correctas ---%n", titulo, correctas, ejecutadas);
        return correctas == ejecutadas ? 0 : 1;
    }

    /** Pruebas ejecutadas hasta ahora (para numerar la clase siguiente). */
    public int total() {
        return ejecutadas;
    }

    // ------------------------------------------------------------------ comprobaciones

    /** Equivale a assertEquals. */
    public static void igual(Object esperado, Object obtenido) {
        if (!Objects.equals(esperado, obtenido)) {
            throw new FalloPrueba("Se esperaba <" + esperado + "> pero fue <" + obtenido + ">");
        }
    }

    /** Equivale a assertTrue. */
    public static void verdadero(boolean condicion, String descripcion) {
        if (!condicion) {
            throw new FalloPrueba("Se esperaba que se cumpliera: " + descripcion);
        }
    }

    /** Equivale a assertFalse. */
    public static void falso(boolean condicion, String descripcion) {
        if (condicion) {
            throw new FalloPrueba("Se esperaba que NO se cumpliera: " + descripcion);
        }
    }

    /**
     * Equivale a assertThrows con comprobación del código: la acción debe lanzar una {@link ExcepcionServicio} con el
     * código esperado. Falla si no lanza nada o si lanza otra cosa. Devuelve la excepción para seguir comprobando
     * (por ejemplo su mensaje).
     */
    public static ExcepcionServicio lanza(String codigoEsperado, Prueba accion) {
        try {
            accion.correr();
        } catch (ExcepcionServicio e) {
            if (!Objects.equals(e.getCodigo(), codigoEsperado)) {
                throw new FalloPrueba("Se esperaba el código " + codigoEsperado + " pero se lanzó " + e.getCodigo());
            }
            return e;
        } catch (Exception e) {
            throw new FalloPrueba("Se esperaba ExcepcionServicio con el código " + codigoEsperado
                    + " pero se lanzó " + e.getClass().getName());
        }
        throw new FalloPrueba("Se esperaba ExcepcionServicio con el código " + codigoEsperado
                + " pero no se lanzó nada");
    }

    /** Equivale a assertThrows con una clase de excepción (errores de programación que no llevan código). */
    public static <T extends Throwable> T lanza(Class<T> esperada, Prueba accion) {
        try {
            accion.correr();
        } catch (Throwable e) {
            if (esperada.isInstance(e)) {
                return esperada.cast(e);
            }
            throw new FalloPrueba("Se esperaba " + esperada.getName() + " pero se lanzó " + e.getClass().getName());
        }
        throw new FalloPrueba("Se esperaba " + esperada.getName() + " pero no se lanzó nada");
    }
}
