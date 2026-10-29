package ec.edu.monster.pruebas;

import static ec.edu.monster.pruebas.Verificador.igual;

import ec.edu.monster.servicios.ElementoServicio;
import java.math.BigDecimal;

/**
 * Pruebas manuales de la capa de servicios: los cálculos, que son funciones puras sin acceso a
 * datos. Cada prueba replica un método de ElementoServicioTest.
 */
public class PruebasServicio {

    public static void main(String[] args) {
        System.exit(ejecutar(1).resumen());
    }

    public static Verificador ejecutar(int primera) {
        Verificador v = new Verificador("Pruebas de servicio", primera);
        PruebasServicio p = new PruebasServicio();

        v.ejecutar("El valor se redondea a dos decimales", p::elValorSeRedondeaADosDecimales);

        return v;
    }

    /** Réplica de ElementoServicioTest.elValorSeRedondeaADosDecimales */
    private void elValorSeRedondeaADosDecimales() {
        igual(new BigDecimal("1.01"), ElementoServicio.redondearValor(new BigDecimal("1.005")));
        igual(new BigDecimal("2.00"), ElementoServicio.redondearValor(new BigDecimal("2")));
    }
}
