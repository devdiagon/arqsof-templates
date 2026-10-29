package ec.edu.monster.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ec.edu.monster.controlador.ValidadorForma;
import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionMonster;
import org.junit.jupiter.api.Test;

class CuerpoJsonTest {

    private static void rechaza(String cuerpo) {
        ExcepcionMonster e = assertThrows(ExcepcionMonster.class, () -> CuerpoJson.leer(cuerpo));
        assertEquals(CodigoError.DATA_001, e.getError());
        assertEquals("Cuerpo de la petición inválido", e.getMessage());
    }

    @Test
    void leeLosCamposDeTextoTalCual() {
        CuerpoJson json = CuerpoJson.leer("{\"codigoElemento\":\"0001\",\"nombre\":\"Primero\"}");

        assertEquals("0001", json.texto("codigoElemento"));
        assertEquals("Primero", json.texto("nombre"));
    }

    @Test
    void losNombresSeBuscanSinDistinguirMayusculas() {
        CuerpoJson json = CuerpoJson.leer("{\"CodigoElemento\":\"0001\",\"NOMBRE\":\"Primero\"}");

        assertEquals("0001", json.texto("codigoElemento"));
        assertEquals("Primero", json.texto("nombre"));
    }

    @Test
    void losCamposDesconocidosSeIgnoran() {
        CuerpoJson json = CuerpoJson.leer("{\"nombre\":\"Primero\",\"otro\":\"x\"}");

        assertEquals("Primero", json.texto("nombre"));
    }

    @Test
    void unCampoAusenteONuloEsNulo() {
        CuerpoJson json = CuerpoJson.leer("{\"nombre\":null}");

        assertNull(json.texto("nombre"));
        assertNull(json.texto("valor"));
    }

    @Test
    void unNumeroJsonSeEntregaConSuTextoParaQueLoValideElControlador() {
        CuerpoJson json = CuerpoJson.leer("{\"valor\":10.50,\"entero\":7}");

        assertEquals("10.50", json.texto("valor"));
        assertEquals("7", json.texto("entero"));
    }

    @Test
    void unNumeroConNotacionCientificaOConDemasiadosDecimalesLlegaTalCualYElValidadorLoRechaza() {
        CuerpoJson json = CuerpoJson.leer("{\"valor\":1E+2,\"otro\":10.999}");

        assertEquals("1E+2", json.texto("valor"));
        assertEquals("10.999", json.texto("otro"));
        for (String texto : new String[]{json.texto("valor"), json.texto("otro")}) {
            ExcepcionMonster e = assertThrows(ExcepcionMonster.class,
                    () -> ValidadorForma.decimalPositivo(texto, "valor", 10, 2));
            assertEquals(CodigoError.DATA_001, e.getError());
        }
    }

    @Test
    void unObjetoOUnaListaComoValorSeTrataComoAusente() {
        CuerpoJson json = CuerpoJson.leer("{\"valor\":{\"a\":1},\"nombre\":[1]}");

        assertNull(json.texto("valor"));
        assertNull(json.texto("nombre"));
    }

    @Test
    void unCuerpoVacioMalFormadoOQueNoEsUnObjetoDaData001() {
        rechaza(null);
        rechaza("");
        rechaza("   ");
        rechaza("{\"valor\":");
        rechaza("no es json");
        rechaza("[1,2]");
        rechaza("\"texto\"");
    }
}
