package ec.edu.monster.pruebas;

import static ec.edu.monster.pruebas.Verificador.igual;
import static ec.edu.monster.pruebas.Verificador.verdadero;

import ec.edu.monster.dto.Resultado;
import ec.edu.monster.modelo.Elemento;
import ec.edu.monster.ws.ConversorTexto;
import ec.edu.monster.ws.ElementoMapeador;
import ec.edu.monster.ws.contrato.RespuestaElemento;
import ec.edu.monster.ws.contrato.RespuestaElementos;
import java.math.BigDecimal;
import java.util.Locale;

/**
 * Pruebas manuales de los mapeadores y de la conversión a texto del contrato.
 * Cada prueba replica un método de ConversorTextoTest o de ElementoMapeadorTest.
 */
public class PruebasMapeador {

    public static void main(String[] args) {
        System.exit(ejecutar(1).resumen());
    }

    public static Verificador ejecutar(int primera) {
        Verificador v = new Verificador("Pruebas de mapeador", primera);
        PruebasMapeador p = new PruebasMapeador();

        v.ejecutar("El decimal lleva siempre dos decimales y punto",
                p::elDecimalSiempreLlevaDosDecimalesYPunto);
        v.ejecutar("Un elemento exitoso se convierte a texto con la envoltura",
                p::unElementoExitosoSeConvierteATextoConLaEnvoltura);
        v.ejecutar("Un error devuelve la lista vacía, nunca nula",
                p::unErrorDevuelveLaListaVaciaPeroNuncaNula);

        return v;
    }

    /**
     * Estado nuevo para cada prueba (equivale al @BeforeEach de JUnit): una configuración
     * regional que usa coma decimal, para comprobar que el texto no depende de ella.
     */
    private void preparar() {
        Locale.setDefault(Locale.forLanguageTag("es-EC"));
    }

    /** Réplica de ConversorTextoTest.elDecimalSiempreLlevaDosDecimalesYPunto */
    private void elDecimalSiempreLlevaDosDecimalesYPunto() {
        preparar();

        igual("100.00", ConversorTexto.decimal(new BigDecimal("100")));
        igual("7000.50", ConversorTexto.decimal(new BigDecimal("7000.5")));
        igual("0.30", ConversorTexto.decimal(new BigDecimal("0.30")));
    }

    /** Réplica de ElementoMapeadorTest.unElementoExitosoSeConvierteATextoConLaEnvoltura */
    private void unElementoExitosoSeConvierteATextoConLaEnvoltura() {
        preparar();

        RespuestaElemento r = ElementoMapeador.aRespuestaElemento(
                Resultado.exito(new Elemento("0001", "Primero", new BigDecimal("10.5"))));

        igual("OK", r.getEstado());
        igual("200", r.getCodigo());
        igual("0001", r.getCodigoElemento());
        igual("10.50", r.getValor());
    }

    /** Réplica de ElementoMapeadorTest.unErrorDevuelveLaListaVaciaPeroNuncaNula */
    private void unErrorDevuelveLaListaVaciaPeroNuncaNula() {
        preparar();

        RespuestaElementos r = ElementoMapeador.aRespuestaElementos(Resultado.error("ERR-500", "Error"));

        verdadero(r.getElementos() != null, "la lista no es nula");
        verdadero(r.getElementos().isEmpty(), "la lista está vacía");
    }
}
