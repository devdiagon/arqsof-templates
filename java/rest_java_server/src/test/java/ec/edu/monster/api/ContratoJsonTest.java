package ec.edu.monster.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ec.edu.monster.api.contrato.ElementoDetalle;
import ec.edu.monster.api.contrato.RespuestaElemento;
import ec.edu.monster.api.contrato.RespuestaElementos;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.junit.jupiter.api.Test;

/** Forma exacta del JSON que produce JSON-B para los tipos de contrato (R4, R5, R9). */
class ContratoJsonTest {

    private final Jsonb jsonb = JsonbBuilder.create();

    @Test
    void laEnvolturaSaleAntesDeLosCamposPropiosYEnElOrdenDelContrato() {
        RespuestaElemento r = new RespuestaElemento();
        r.aplicarEnvoltura("OK", "200", "Operación exitosa");
        r.cargarDatos("0001", "Primero", "10.50");

        assertEquals("{\"estado\":\"OK\",\"codigo\":\"200\",\"mensaje\":\"Operación exitosa\","
                + "\"codigoElemento\":\"0001\",\"nombre\":\"Primero\",\"valor\":\"10.50\"}", jsonb.toJson(r));
    }

    @Test
    void enUnErrorLosCamposPropiosSeOmiten() {
        RespuestaElemento r = new RespuestaElemento();
        r.aplicarEnvoltura("ERROR", "ELE-001", "El elemento no existe");

        assertEquals("{\"estado\":\"ERROR\",\"codigo\":\"ELE-001\",\"mensaje\":\"El elemento no existe\"}",
                jsonb.toJson(r));
    }

    @Test
    void laListaLlegaVaciaNuncaAusente() {
        RespuestaElementos r = new RespuestaElementos();
        r.aplicarEnvoltura("ERROR", "ERR-500", "Error interno del servidor");

        assertEquals("{\"estado\":\"ERROR\",\"codigo\":\"ERR-500\",\"mensaje\":\"Error interno del servidor\","
                + "\"elementos\":[]}", jsonb.toJson(r));
    }

    @Test
    void laListaConElementosConservaElOrdenDeLosCampos() {
        RespuestaElementos r = new RespuestaElementos();
        r.aplicarEnvoltura("OK", "200", "Operación exitosa");
        r.cargarDatos(java.util.List.of(new ElementoDetalle("0001", "Primero", "10.50")));

        assertEquals("{\"estado\":\"OK\",\"codigo\":\"200\",\"mensaje\":\"Operación exitosa\","
                + "\"elementos\":[{\"codigoElemento\":\"0001\",\"nombre\":\"Primero\",\"valor\":\"10.50\"}]}",
                jsonb.toJson(r));
    }
}
