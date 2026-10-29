package ec.edu.monster.api;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import org.junit.Test;

/** El Base64 propio de {@code TraductorApi} (Android 24 no trae {@code java.util.Base64}) contra los vectores de la RFC 4648. */
public class Base64Test {

    private static String codificar(String texto) {
        return TraductorApi.base64(texto.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void coincideConLosVectoresDeLaRfc4648() {
        assertEquals("", codificar(""));
        assertEquals("Zg==", codificar("f"));
        assertEquals("Zm8=", codificar("fo"));
        assertEquals("Zm9v", codificar("foo"));
        assertEquals("Zm9vYg==", codificar("foob"));
        assertEquals("Zm9vYmE=", codificar("fooba"));
        assertEquals("Zm9vYmFy", codificar("foobar"));
    }

    @Test
    public void coincideConElBase64DelJdkConTextoNoAscii() {
        String credenciales = "usuario:cláve ñ/ó+?";

        assertEquals(java.util.Base64.getEncoder().encodeToString(credenciales.getBytes(StandardCharsets.UTF_8)),
                codificar(credenciales));
    }

    @Test
    public void laCabeceraBasicLlevaUsuarioYClaveEnBase64() {
        assertEquals("Basic dXN1YXJpbzpjbGF2ZQ==", TraductorApi.autorizacionBasic("usuario", "clave"));
    }
}
