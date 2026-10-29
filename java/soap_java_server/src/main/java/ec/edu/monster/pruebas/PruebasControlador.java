package ec.edu.monster.pruebas;

import static ec.edu.monster.pruebas.Verificador.falso;
import static ec.edu.monster.pruebas.Verificador.igual;
import static ec.edu.monster.pruebas.Verificador.lanza;
import static ec.edu.monster.pruebas.Verificador.verdadero;

import ec.edu.monster.controlador.ControladorBase;
import ec.edu.monster.controlador.ValidadorForma;
import ec.edu.monster.dto.Resultado;
import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionMonster;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Pruebas manuales de la capa de controlador: captura central de errores y validación de forma.
 * Cada prueba replica un método de ControladorBaseTest o de ValidadorFormaTest.
 */
public class PruebasControlador {

    /** Expone la operación protegida de la base para poder probarla. */
    private static class ControladorDePrueba extends ControladorBase {
        <T> Resultado<T> correr(Callable<T> operacion) {
            return ejecutar(operacion::call);
        }
    }

    private ControladorDePrueba controlador;

    public static void main(String[] args) {
        System.exit(ejecutar(1).resumen());
    }

    public static Verificador ejecutar(int primera) {
        Verificador v = new Verificador("Pruebas de controlador", primera);
        PruebasControlador p = new PruebasControlador();

        v.ejecutar("Una excepción de negocio devuelve su código y su mensaje",
                p::unaExcepcionDeNegocioDevuelveSuCodigoYSuMensaje);
        v.ejecutar("Una excepción inesperada devuelve ERR-500 sin detalles internos",
                p::unaExcepcionInesperadaDevuelveErr500SinFiltrarDetallesInternos);
        v.ejecutar("Un decimal con más decimales de los permitidos se rechaza",
                p::decimalPositivoRechazaMasDecimalesDeLosPermitidos);
        v.ejecutar("Un código con longitud o caracteres inválidos se rechaza",
                p::digitosExactosRechazaLongitudIncorrectaOCaracteresQueNoSonDigitos);

        return v;
    }

    /** Estado nuevo para cada prueba (equivale al @BeforeEach de JUnit). */
    private void preparar() {
        controlador = new ControladorDePrueba();
    }

    /** Réplica de ControladorBaseTest.unaExcepcionDeNegocioDevuelveSuCodigoYSuMensaje */
    private void unaExcepcionDeNegocioDevuelveSuCodigoYSuMensaje() {
        preparar();

        Resultado<String> resultado = controlador.correr(() -> {
            throw new ExcepcionMonster(CodigoError.ELE_001);
        });

        igual(Resultado.Estado.ERROR, resultado.estado());
        igual("ELE-001", resultado.codigo());
        igual(CodigoError.ELE_001.getMensaje(), resultado.mensaje());
        igual(null, resultado.datos());
    }

    /** Réplica de ControladorBaseTest.unaExcepcionInesperadaDevuelveErr500SinFiltrarDetallesInternos */
    private void unaExcepcionInesperadaDevuelveErr500SinFiltrarDetallesInternos() {
        preparar();
        String detalleInterno = "Table 'ejemplo.elemento' doesn't exist (usuario ejemplo)";

        Resultado<String> resultado = controlador.correr(() -> {
            throw new SQLException(detalleInterno);
        });

        igual(Resultado.Estado.ERROR, resultado.estado());
        igual("ERR-500", resultado.codigo());
        igual(CodigoError.ERR_500.getMensaje(), resultado.mensaje());
        falso(resultado.mensaje().contains("ejemplo"), "el mensaje expone el detalle interno");
    }

    /** Réplica de ValidadorFormaTest.decimalPositivoRechazaMasDecimalesDeLosPermitidos */
    private void decimalPositivoRechazaMasDecimalesDeLosPermitidos() {
        preparar();

        ExcepcionMonster e = lanza(CodigoError.DATA_001,
                () -> ValidadorForma.decimalPositivo("10.999", "valor", 10, 2));

        verdadero(e.getMessage().contains("2 decimales"), "el mensaje indica el máximo de decimales");
    }

    /** Réplica de ValidadorFormaTest.digitosExactosRechazaLongitudIncorrectaOCaracteresQueNoSonDigitos */
    private void digitosExactosRechazaLongitudIncorrectaOCaracteresQueNoSonDigitos() {
        preparar();

        for (String valor : new String[]{"1234567", "123456789", "0010000A", "0010 001", "-0100001"}) {
            lanza(CodigoError.DATA_001, () -> ValidadorForma.digitosExactos(valor, "código", 8));
        }
    }
}
