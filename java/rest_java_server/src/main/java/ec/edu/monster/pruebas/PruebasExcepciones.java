package ec.edu.monster.pruebas;

import static ec.edu.monster.pruebas.Verificador.falso;
import static ec.edu.monster.pruebas.Verificador.igual;
import static ec.edu.monster.pruebas.Verificador.verdadero;

import ec.edu.monster.excepciones.CodigoError;

/**
 * Pruebas manuales del catálogo de errores.
 * Cada prueba replica un método de CodigoErrorTest.
 */
public class PruebasExcepciones {

    public static void main(String[] args) {
        System.exit(ejecutar(1).resumen());
    }

    public static Verificador ejecutar(int primera) {
        Verificador v = new Verificador("Pruebas de excepciones", primera);
        PruebasExcepciones p = new PruebasExcepciones();

        v.ejecutar("Cada entrada del catálogo tiene código, mensaje y categoría",
                p::cadaEntradaTieneCodigoMensajeYCategoria);
        v.ejecutar("ERR-500 es técnico y con mensaje genérico", p::err500EsTecnicoConMensajeGenerico);

        return v;
    }

    /** Réplica de CodigoErrorTest.cadaEntradaTieneCodigoMensajeYCategoria */
    private void cadaEntradaTieneCodigoMensajeYCategoria() {
        for (CodigoError error : CodigoError.values()) {
            verdadero(error.getCodigo().matches("[A-Z]{2,4}-\\d{3}"),
                    "código con formato PREFIJO-NNN: " + error.getCodigo());
            falso(error.getMensaje().isBlank(), "mensaje vacío en " + error);
            verdadero(error.getCategoria() != null, "categoría definida en " + error);
        }
    }

    /** Réplica de CodigoErrorTest.err500EsTecnicoConMensajeGenerico */
    private void err500EsTecnicoConMensajeGenerico() {
        igual("ERR-500", CodigoError.ERR_500.getCodigo());
        igual("Error interno del servidor", CodigoError.ERR_500.getMensaje());
        igual(CodigoError.Categoria.TECNICO, CodigoError.ERR_500.getCategoria());
    }
}
