package ec.edu.monster.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.dto.RespuestaBaseDto;
import ec.edu.monster.excepciones.ExcepcionServicio;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TraductorApiTest {

    /** DTO de ejemplo: un campo propio de texto y una lista que nunca es nula. */
    static class RespuestaEjemploDto extends RespuestaBaseDto {
        protected String valor;
        protected List<String> elementos;

        public String getValor() {
            return valor;
        }

        public List<String> getElementos() {
            if (elementos == null) {
                elementos = new ArrayList<>();
            }
            return elementos;
        }
    }

    @Test
    void codificaLasVariablesDeRutaConPorcientoNoMas() {
        assertEquals("a%20b%2Fc", TraductorApi.codificar("a b/c"));
        assertEquals("12345678", TraductorApi.codificar("12345678"));
    }

    @Test
    void laAutorizacionBasicLlevaUsuarioYClaveEnBase64Utf8() {
        // base64("usuario:clave")
        assertEquals("Basic dXN1YXJpbzpjbGF2ZQ==", TraductorApi.autorizacionBasic("usuario", "clave"));
    }

    @Test
    void elCuerpoEsUnObjetoJsonConCamposDeTexto() {
        assertEquals("{\"destino\":\"2\",\"importe\":\"200.00\"}", TraductorApi.cuerpo("destino", "2", "importe", "200.00"));
    }

    @Test
    void leeLaEnvolturaDeUnExitoConSusCamposPropios() {
        String json = "{\"estado\":\"OK\",\"codigo\":\"200\",\"mensaje\":\"Operación exitosa\",\"valor\":\"7100.50\"}";

        RespuestaEjemploDto r = TraductorApi.leer(new RespuestaHttp(200, json), RespuestaEjemploDto.class);

        assertTrue(r.esOk());
        assertEquals("7100.50", r.getValor());
    }

    @Test
    void leeLaEnvolturaTambienConHttpDeErrorSinDescartarElMensaje() {
        for (int http : new int[] {400, 401, 404, 422, 500}) {
            String json = "{\"estado\":\"ERROR\",\"codigo\":\"X-001\",\"mensaje\":\"Mensaje del servidor\"}";

            RespuestaBaseDto r = TraductorApi.leer(new RespuestaHttp(http, json), RespuestaEjemploDto.class);

            assertEquals("X-001", r.getCodigo());
            assertEquals("Mensaje del servidor", r.getMensaje());
        }
    }

    @Test
    void laListaLlegaVaciaEnUnErrorYConElementosEnUnExito() {
        RespuestaEjemploDto error = TraductorApi.leer(new RespuestaHttp(404,
                "{\"estado\":\"ERROR\",\"codigo\":\"X-001\",\"mensaje\":\"No existe\",\"elementos\":[]}"),
                RespuestaEjemploDto.class);
        RespuestaEjemploDto sinCampo = TraductorApi.leer(new RespuestaHttp(404,
                "{\"estado\":\"ERROR\",\"codigo\":\"X-001\",\"mensaje\":\"No existe\"}"), RespuestaEjemploDto.class);
        RespuestaEjemploDto ok = TraductorApi.leer(new RespuestaHttp(200,
                "{\"estado\":\"OK\",\"codigo\":\"200\",\"mensaje\":\"x\",\"elementos\":[\"a\",\"b\"]}"),
                RespuestaEjemploDto.class);

        assertTrue(error.getElementos().isEmpty());
        assertTrue(sinCampo.getElementos().isEmpty());
        assertEquals(List.of("a", "b"), ok.getElementos());
    }

    @Test
    void unCuerpoQueNoEsJsonVacioOSinEstadoEsSrv000ConElCodigoHttp() {
        String[] cuerpos = {"<html>Bad Gateway</html>", "", "   ", null, "[]", "{\"otro\":\"x\"}"};
        for (String cuerpo : cuerpos) {
            ExcepcionServicio e = assertThrows(ExcepcionServicio.class,
                    () -> TraductorApi.leer(new RespuestaHttp(502, cuerpo), RespuestaEjemploDto.class));

            assertEquals(ExcepcionServicio.SRV_000, e.getCodigo());
            assertTrue(e.getMessage().contains("HTTP 502"));
        }
    }

    @Test
    void sinRespuestaDelServidorEsRedConLaCausaOriginal() {
        IOException causa = new IOException("Connection refused");
        Transporte caido = (metodo, ruta, cuerpo, autorizacion) -> {
            throw causa;
        };

        ExcepcionServicio e = assertThrows(ExcepcionServicio.class,
                () -> TraductorApi.llamar(caido, "GET", "x", null, null, RespuestaEjemploDto.class));

        assertEquals(ExcepcionServicio.RED, e.getCodigo());
        assertEquals(ExcepcionServicio.MENSAJE_RED, e.getMessage());
        assertSame(causa, e.getCause());
    }

    @Test
    void unErrorDeProgramacionQueNoEsDeRedNoSeDisfrazaDeRed() {
        Transporte roto = (metodo, ruta, cuerpo, autorizacion) -> {
            throw new IllegalStateException("error de programación");
        };

        assertThrows(IllegalStateException.class,
                () -> TraductorApi.llamar(roto, "GET", "x", null, null, RespuestaEjemploDto.class));
    }
}
