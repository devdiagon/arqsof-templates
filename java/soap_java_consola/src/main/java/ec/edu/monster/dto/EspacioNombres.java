package ec.edu.monster.dto;

/**
 * Namespace de destino (TNS) del servicio SOAP: el {@code targetNamespace} del WSDL del servidor.
 * Es el ÚNICO lugar donde se escribe en el cliente: lo usan {@code package-info.java}, {@code RespuestaBaseDto},
 * el contrato ({@code @WebService}, {@code @WebParam}, {@code @WebResult}, {@code action}) y la fábrica {@code ClienteWS}.
 * Regla R2 de CONVENCION_SOAP.md. CAMBIAR al namespace del servidor del proyecto.
 */
public final class EspacioNombres {

    public static final String SERVICIO = "http://CAMBIAR.namespace/";

    private EspacioNombres() {}
}
