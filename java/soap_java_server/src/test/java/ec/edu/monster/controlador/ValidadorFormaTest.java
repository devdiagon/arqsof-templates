package ec.edu.monster.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionMonster;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ValidadorFormaTest {

    private static ExcepcionMonster rechazo(Runnable accion) {
        ExcepcionMonster e = assertThrows(ExcepcionMonster.class, accion::run);
        assertEquals(CodigoError.DATA_001, e.getError());
        return e;
    }

    // ------------------------------------------------------------------ obligatorio

    @Test
    void obligatorioRecortaElValor() {
        assertEquals("abc", ValidadorForma.obligatorio("  abc  ", "campo"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "   "})
    void obligatorioRechazaNuloVacioYBlanco(String valor) {
        ExcepcionMonster e = rechazo(() -> ValidadorForma.obligatorio(valor, "nombre"));

        assertEquals("El campo nombre es obligatorio", e.getMessage());
    }

    // ------------------------------------------------------------------ longitudes

    @Test
    void textoHastaAceptaElLimiteExactoYRechazaElExceso() {
        assertEquals("123456789012345", ValidadorForma.textoHasta("123456789012345", "código", 15));

        rechazo(() -> ValidadorForma.textoHasta("1234567890123456", "código", 15));
    }

    @Test
    void textoExactoExigeLaLongitudIndicada() {
        assertEquals("123456", ValidadorForma.textoExacto("123456", "código", 6));

        rechazo(() -> ValidadorForma.textoExacto("12345", "código", 6));
        rechazo(() -> ValidadorForma.textoExacto("1234567", "código", 6));
    }

    @Test
    void digitosExactosAceptaSoloLaCantidadIndicadaDeDigitos() {
        assertEquals("00100001", ValidadorForma.digitosExactos(" 00100001 ", "código", 8));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "123456789", "0010000A", "0010 001", "-0100001"})
    void digitosExactosRechazaLongitudIncorrectaOCaracteresQueNoSonDigitos(String valor) {
        rechazo(() -> ValidadorForma.digitosExactos(valor, "código", 8));
    }

    // ------------------------------------------------------------------ decimal positivo

    @ParameterizedTest
    @ValueSource(strings = {"100", "100.5", "100.50", "0.01", " 50 ", "9999999999.99"})
    void decimalPositivoAceptaNumerosConPuntoDentroDeLosLimites(String valor) {
        BigDecimal numero = ValidadorForma.decimalPositivo(valor, "valor", 10, 2);

        assertEquals(0, new BigDecimal(valor.trim()).compareTo(numero));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-5", "+5", "10,5", "abc", "1e3", ".5", "5.", "1.2.3", "1 000", "10.5.0"})
    void decimalPositivoRechazaFormatosQueNoSonNumeroConPunto(String valor) {
        rechazo(() -> ValidadorForma.decimalPositivo(valor, "valor", 10, 2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "0.0"})
    void decimalPositivoRechazaElCero(String valor) {
        ExcepcionMonster e = rechazo(() -> ValidadorForma.decimalPositivo(valor, "valor", 10, 2));

        assertTrue(e.getMessage().contains("mayor que cero"));
    }

    @Test
    void decimalPositivoRechazaMasDecimalesDeLosPermitidos() {
        ExcepcionMonster e = rechazo(() -> ValidadorForma.decimalPositivo("10.999", "valor", 10, 2));

        assertTrue(e.getMessage().contains("2 decimales"));
    }

    @Test
    void decimalPositivoRechazaMasDigitosEnterosDeLosPermitidos() {
        ExcepcionMonster e = rechazo(() ->
                ValidadorForma.decimalPositivo("12345678901", "valor", 10, 2));

        assertTrue(e.getMessage().contains("10 dígitos enteros"));
    }

    @Test
    void decimalPositivoRechazaNuloYBlanco() {
        rechazo(() -> ValidadorForma.decimalPositivo(null, "valor", 10, 2));
        rechazo(() -> ValidadorForma.decimalPositivo("   ", "valor", 10, 2));
    }
}
