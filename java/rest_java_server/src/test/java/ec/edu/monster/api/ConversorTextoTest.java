package ec.edu.monster.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConversorTextoTest {

    private Locale localeOriginal;

    @BeforeEach
    void usarUnaConfiguracionRegionalConComaDecimal() {
        localeOriginal = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("es-EC"));
    }

    @AfterEach
    void restaurarLaConfiguracionRegional() {
        Locale.setDefault(localeOriginal);
    }

    @Test
    void elDecimalSiempreLlevaDosDecimalesYPunto() {
        assertEquals("100.00", ConversorTexto.decimal(new BigDecimal("100")));
        assertEquals("7000.50", ConversorTexto.decimal(new BigDecimal("7000.5")));
        assertEquals("0.30", ConversorTexto.decimal(new BigDecimal("0.30")));
    }

    @Test
    void elDecimalNuncaUsaNotacionCientificaNiSeparadorDeMiles() {
        assertEquals("1000.00", ConversorTexto.decimal(new BigDecimal("1E+3")));
        assertEquals("1234567.89", ConversorTexto.decimal(new BigDecimal("1234567.89")));
    }

    @Test
    void elDecimalNuloQuedaNulo() {
        assertNull(ConversorTexto.decimal(null));
    }

    @Test
    void laFechaSeEscribeEnIso8601() {
        assertEquals("2026-10-26", ConversorTexto.fecha(LocalDate.of(2026, 10, 26)));
        assertEquals("2022-01-06", ConversorTexto.fecha(LocalDate.of(2022, 1, 6)));
    }

    @Test
    void laFechaNulaQuedaNula() {
        assertNull(ConversorTexto.fecha(null));
    }

    @Test
    void elEnteroSeEscribeSinSeparadores() {
        assertEquals("1234567", ConversorTexto.entero(1234567));
        assertEquals("0", ConversorTexto.entero(0));
    }
}
