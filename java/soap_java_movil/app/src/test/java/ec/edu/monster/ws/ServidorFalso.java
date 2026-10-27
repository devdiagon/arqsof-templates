package ec.edu.monster.ws;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Servidor HTTP mínimo para las pruebas de {@code ws} (ServerSocket: el android.jar de compilación no trae
 * com.sun.net.httpserver). Responde lo que se le indique y registra lo que recibió, sin red real ni servidor del dominio.
 */
class ServidorFalso implements AutoCloseable {

    private final ServerSocket servidor;
    volatile String rutaRecibida;
    volatile String accionRecibida;
    volatile String cuerpoRecibido;
    volatile int estadoHttp = 200;
    volatile String respuesta = "";

    ServidorFalso() throws Exception {
        servidor = new ServerSocket(0, 5, InetAddress.getLoopbackAddress());
        Thread hilo = new Thread(() -> {
            while (!servidor.isClosed()) {
                try (Socket conexion = servidor.accept()) {
                    atender(conexion);
                } catch (Exception e) {
                    // servidor cerrado al terminar la prueba
                }
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    /** URL completa con una ruta libre, como la publicaría cualquier servidor (Java o .NET). */
    String url(String ruta) {
        return "http://localhost:" + servidor.getLocalPort() + ruta;
    }

    @Override
    public void close() throws Exception {
        servidor.close();
    }

    /** Lee una petición HTTP/1.1 (línea, cabeceras, cuerpo por Content-Length) y responde con {@link #respuesta}. */
    private void atender(Socket conexion) throws Exception {
        InputStream in = conexion.getInputStream();
        String linea = leerLinea(in);
        rutaRecibida = linea.split(" ")[1];
        int largo = 0;
        accionRecibida = null;
        String cabecera;
        while (!(cabecera = leerLinea(in)).isEmpty()) {
            String minuscula = cabecera.toLowerCase();
            if (minuscula.startsWith("content-length:")) {
                largo = Integer.parseInt(cabecera.substring(cabecera.indexOf(':') + 1).trim());
            } else if (minuscula.startsWith("soapaction:")) {
                accionRecibida = cabecera.substring(cabecera.indexOf(':') + 1).trim();
            }
        }
        byte[] cuerpo = new byte[largo];
        int leidos = 0;
        while (leidos < largo) {
            int n = in.read(cuerpo, leidos, largo - leidos);
            if (n < 0) {
                break;
            }
            leidos += n;
        }
        cuerpoRecibido = new String(cuerpo, StandardCharsets.UTF_8);
        byte[] salida = respuesta.getBytes(StandardCharsets.UTF_8);
        OutputStream out = conexion.getOutputStream();
        out.write(("HTTP/1.1 " + estadoHttp + " X\r\nContent-Type: text/xml; charset=utf-8\r\nContent-Length: " + salida.length
                + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(salida);
        out.flush();
    }

    private static String leerLinea(InputStream in) throws Exception {
        ByteArrayOutputStream linea = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1 && b != '\n') {
            if (b != '\r') {
                linea.write(b);
            }
        }
        return new String(linea.toByteArray(), StandardCharsets.UTF_8);
    }

    /** Sobre de respuesta SOAP con el contenido indicado dentro del Body. */
    static String sobre(String contenido) {
        return "<?xml version='1.0' encoding='UTF-8'?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>"
                + contenido + "</S:Body></S:Envelope>";
    }
}
