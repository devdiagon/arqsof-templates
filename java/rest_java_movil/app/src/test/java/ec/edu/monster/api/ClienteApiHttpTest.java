package ec.edu.monster.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.excepciones.ExcepcionServicio;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Prueba el transporte contra un servidor HTTP minimo de la JVM (un ServerSocket que responde lo programado).
 * No es el HttpURLConnection de Android: el comportamiento ante un 401 sin WWW-Authenticate solo se confirma
 * en emulador o telefono.
 */
public class ClienteApiHttpTest {

    private ServerSocket servidor;
    private Thread hilo;
    private ClienteApi api;
    private final AtomicReference<String> recibido = new AtomicReference<>();
    private volatile int codigoRespuesta = 200;
    private volatile String cuerpoRespuesta = "{}";

    @Before
    public void iniciar() throws IOException {
        servidor = new ServerSocket(0, 5, InetAddress.getLoopbackAddress());
        hilo = new Thread(() -> {
            while (!servidor.isClosed()) {
                try (Socket cliente = servidor.accept()) {
                    atender(cliente);
                } catch (IOException e) {
                    return;   // servidor cerrado
                }
            }
        });
        hilo.setDaemon(true);
        hilo.start();
        api = new ClienteApiHttp("http://127.0.0.1:" + servidor.getLocalPort() + "/api/");
    }

    @After
    public void detener() throws IOException {
        servidor.close();
    }

    /** Lee la peticion completa (cabeceras + Content-Length bytes), la guarda como texto y responde. */
    private void atender(Socket cliente) throws IOException {
        InputStream entrada = cliente.getInputStream();
        ByteArrayOutputStream cabeceras = new ByteArrayOutputStream();
        int a = 0, b = 0, c = 0, d;
        while ((d = entrada.read()) != -1) {
            cabeceras.write(d);
            if (a == '\r' && b == '\n' && c == '\r' && d == '\n') {
                break;
            }
            a = b;
            b = c;
            c = d;
        }
        String texto = new String(cabeceras.toByteArray(), StandardCharsets.UTF_8);
        int largo = 0;
        for (String linea : texto.split("\r\n")) {
            if (linea.toLowerCase().startsWith("content-length:")) {
                largo = Integer.parseInt(linea.substring(15).trim());
            }
        }
        byte[] cuerpo = new byte[largo];
        int leidos = 0;
        while (leidos < largo) {
            int n = entrada.read(cuerpo, leidos, largo - leidos);
            if (n == -1) {
                break;
            }
            leidos += n;
        }
        recibido.set(texto + new String(cuerpo, StandardCharsets.UTF_8));

        byte[] salida = cuerpoRespuesta.getBytes(StandardCharsets.UTF_8);
        OutputStream respuesta = cliente.getOutputStream();
        respuesta.write(("HTTP/1.1 " + codigoRespuesta + " X\r\nContent-Type: application/json; charset=UTF-8\r\n"
                + "Content-Length: " + salida.length + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        respuesta.write(salida);
        respuesta.flush();
    }

    private String peticion() {
        return recibido.get().toLowerCase();
    }

    @Test
    public void postLlevaCuerpoJsonUtf8YCabeceras() {
        cuerpoRespuesta = "{\"estado\":\"OK\"}";
        RespuestaHttp r = api.enviar(ClienteApi.POST, "/auth/login", "{\"u\":\"ñ\"}", null);
        assertEquals(200, r.codigo());
        assertEquals("{\"estado\":\"OK\"}", r.cuerpo());
        assertTrue(recibido.get().startsWith("POST /api/auth/login HTTP/1.1"));
        assertTrue(peticion().contains("content-type: application/json; charset=utf-8"));
        assertTrue(peticion().contains("accept: application/json"));
        assertFalse(peticion().contains("authorization"));
        assertTrue(recibido.get().endsWith("{\"u\":\"ñ\"}"));
    }

    @Test
    public void getLlevaElTokenBearerYNoLlevaCuerpo() {
        api.enviar(ClienteApi.GET, "/items", null, "T1");
        assertTrue(recibido.get().startsWith("GET /api/items HTTP/1.1"));
        assertTrue(peticion().contains("authorization: bearer t1"));
        assertFalse(peticion().contains("content-type"));
    }

    @Test
    public void unError401DevuelveElCodigoYElCuerpoEnVezDeLanzar() {
        codigoRespuesta = 401;
        cuerpoRespuesta = "{\"codigo\":\"AUTH-003\",\"mensaje\":\"Sesión vencida\"}";
        RespuestaHttp r = api.enviar(ClienteApi.POST, "/x", "{}", "T1");
        assertEquals(401, r.codigo());
        assertEquals("{\"codigo\":\"AUTH-003\",\"mensaje\":\"Sesión vencida\"}", r.cuerpo());
        assertFalse(r.esExito());
    }

    @Test
    public void unErrorSinCuerpoDaCuerpoVacio() {
        codigoRespuesta = 500;
        cuerpoRespuesta = "";
        RespuestaHttp r = api.enviar(ClienteApi.GET, "/x", null, null);
        assertEquals(500, r.codigo());
        assertEquals("", r.cuerpo());
    }

    @Test
    public void sinServidorLanzaServidorNoDisponible() throws IOException {
        servidor.close();
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> api.enviar(ClienteApi.GET, "/x", null, null));
        assertTrue(e.servidorNoDisponible());
    }
}
