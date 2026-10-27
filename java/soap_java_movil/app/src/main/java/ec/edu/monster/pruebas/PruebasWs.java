package ec.edu.monster.pruebas;

import static ec.edu.monster.pruebas.Verificador.falso;
import static ec.edu.monster.pruebas.Verificador.igual;
import static ec.edu.monster.pruebas.Verificador.lanza;
import static ec.edu.monster.pruebas.Verificador.verdadero;

import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.ws.ConfiguracionServicio;
import ec.edu.monster.ws.SoapXml;
import ec.edu.monster.ws.TraductorRed;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pruebas manuales de la capa {@code ws} (ejecutar con <i>Run File</i> desde Android Studio): traducción de fallos de red y
 * armado y lectura del XML SOAP. Solo Java puro, sin red ni servidor ni {@code android.*}. Cada una replica un método JUnit
 * del mismo nombre; el Javadoc indica de cuál.
 */
public class PruebasWs {

    private static final String TNS = ConfiguracionServicio.TNS;

    private static final String FAULT = "<?xml version='1.0' encoding='UTF-8'?>"
            + "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>"
            + "<S:Fault><faultcode>S:Server</faultcode><faultstring>Error interno</faultstring></S:Fault>"
            + "</S:Body></S:Envelope>";

    public static void main(String[] args) {
        System.exit(ejecutar(1).resumen());
    }

    public static Verificador ejecutar(int primera) {
        Verificador v = new Verificador("Pruebas de ws", primera);
        PruebasWs p = new PruebasWs();

        v.ejecutar("unFalloDeRedSeTraduceACodigoRedConElMensajeGenericoYConservaLaCausa",
                p::unFalloDeRedSeTraduceACodigoRedConElMensajeGenericoYConservaLaCausa);
        v.ejecutar("unErrorDeProgramacionQueNoEsDeRedNoSeDisfrazaDeRed",
                p::unErrorDeProgramacionQueNoEsDeRedNoSeDisfrazaDeRed);
        v.ejecutar("faultEsFalloDeComunicacion", p::faultEsFalloDeComunicacion);
        v.ejecutar("cuerpoVacioOInvalidoEsRespuestaIlegible", p::cuerpoVacioOInvalidoEsRespuestaIlegible);
        v.ejecutar("peticionRespetaElOrdenYCalificaLosParametros", p::peticionRespetaElOrdenYCalificaLosParametros);
        return v;
    }

    /** Réplica de TraductorRedTest.unFalloDeRedSeTraduceACodigoRedConElMensajeGenericoYConservaLaCausa */
    private void unFalloDeRedSeTraduceACodigoRedConElMensajeGenericoYConservaLaCausa() {
        IOException causa = new IOException("Connection refused");

        ExcepcionServicio e = lanza(ExcepcionServicio.RED, () -> TraductorRed.llamar(() -> {
            throw causa;
        }));

        igual(ExcepcionServicio.MENSAJE_RED, e.getMessage());
        verdadero(e.getCause() == causa, "conserva la causa original");
        verdadero(e.servidorNoDisponible(), "se reconoce como servidor no disponible");
    }

    /** Réplica de TraductorRedTest.unErrorDeProgramacionQueNoEsDeRedNoSeDisfrazaDeRed */
    private void unErrorDeProgramacionQueNoEsDeRedNoSeDisfrazaDeRed() {
        lanza(IllegalStateException.class, () -> TraductorRed.llamar(() -> {
            throw new IllegalStateException("error de programación");
        }));
    }

    /** Réplica de SoapXmlTest.faultEsFalloDeComunicacion */
    private void faultEsFalloDeComunicacion() {
        ExcepcionServicio e = lanza(ExcepcionServicio.RED, () -> SoapXml.extraerRespuesta(FAULT));

        igual(ExcepcionServicio.MENSAJE_RED, e.getMessage());
    }

    /** Réplica de SoapXmlTest.cuerpoVacioOInvalidoEsRespuestaIlegible */
    private void cuerpoVacioOInvalidoEsRespuestaIlegible() {
        String[] cuerpos = {null, "", "   ", "<html>no es soap", "texto plano"};
        for (String cuerpo : cuerpos) {
            ExcepcionServicio e = lanza(ExcepcionServicio.SRV_000, () -> SoapXml.extraerRespuesta(cuerpo));
            igual(ExcepcionServicio.MENSAJE_SRV_000, e.getMessage());
            verdadero(e.respuestaIlegible(), "se reconoce como respuesta ilegible");
        }
    }

    /** Réplica de SoapXmlTest.peticionRespetaElOrdenYCalificaLosParametros */
    private void peticionRespetaElOrdenYCalificaLosParametros() {
        Map<String, String> parametros = new LinkedHashMap<>();
        parametros.put("cuentaOrigen", "00100001");
        parametros.put("importe", "100.50");

        String xml = SoapXml.construirPeticion(TNS, "realizarRetiro", parametros);

        verdadero(xml.contains("<ns:realizarRetiro xmlns:ns=\"" + TNS + "\">"), "la operación va en el namespace del servicio");
        int cuenta = xml.indexOf("<ns:cuentaOrigen>00100001</ns:cuentaOrigen>");
        int importe = xml.indexOf("<ns:importe>100.50</ns:importe>");
        verdadero(cuenta >= 0 && importe >= 0, "ambos parámetros van calificados");
        verdadero(cuenta < importe, "los parámetros salen en el orden indicado");
        falso(xml.contains("<cuentaOrigen>"), "ningún parámetro sin calificar");
    }
}
