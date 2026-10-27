package ec.edu.monster.pruebas;

/**
 * Ejecuta todas las pruebas manuales del proyecto, numeradas de corrido, y termina con código 0 si todas pasan. Cada clase
 * también se puede ejecutar por separado con <i>Run File</i> desde Android Studio (docs/JAVA_TESTS_CONVENTIONS.md).
 *
 * DOMINIO: al construir el proyecto, agregar aquí las clases {@code Pruebas…} del dominio (p. ej. {@code PruebasServicio},
 * con sus dobles de las interfaces {@code XCliente}); cada una expone {@code static Verificador ejecutar(int primera)}.
 * Las pruebas manuales replican métodos JUnit de {@code src/test}, hasta un máximo de 15 en total. No hay pruebas manuales de
 * controladores: usan {@code LiveData} y no corren sin el entorno de Android.
 */
public class PruebasTodas {

    public static void main(String[] args) {
        Verificador ws = PruebasWs.ejecutar(1);
        int total = ws.total();
        int codigo = ws.resumen();
        System.out.println("=== Total: " + total + " pruebas manuales ===");
        System.exit(codigo);
    }
}
