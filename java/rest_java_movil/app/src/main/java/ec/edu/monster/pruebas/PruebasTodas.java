package ec.edu.monster.pruebas;

/**
 * Ejecuta todas las pruebas manuales del proyecto, numeradas de corrido, y termina con código 0 si todas pasan. Cada clase
 * también se puede ejecutar por separado con <i>Run File</i> desde Android Studio. Solo Java puro: sin {@code android.*},
 * sin red y sin servidor.
 *
 * DOMINIO: la plantilla no trae pruebas manuales (se generan al final con {@code JAVA_TESTS_CONVENTIONS.md}, como réplicas
 * de las JUnit del proyecto). Por cada clase nueva (p. ej. {@code PruebasServicio}, {@code PruebasApi}) agregar aquí:
 * <pre>
 * Verificador servicio = PruebasServicio.ejecutar(1);
 * Verificador api = PruebasApi.ejecutar(servicio.total() + 1);
 * int total = servicio.total() + api.total();
 * int codigo = Math.max(servicio.resumen(), api.resumen());
 * </pre>
 * No hay pruebas manuales de los controladores (usan {@code LiveData}) ni de {@code TransporteHttp} (abre conexiones).
 */
public class PruebasTodas {

    public static void main(String[] args) {
        System.out.println("=== No hay pruebas manuales todavía (README §4, paso 7) ===");
        System.exit(0);
    }
}
