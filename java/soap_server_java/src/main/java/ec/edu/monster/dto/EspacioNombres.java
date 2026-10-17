package ec.edu.monster.dto;

/**
 * Namespace de destino (TNS) del contrato SOAP. Es el ÚNICO lugar donde se escribe: lo usan el servicio
 * ({@code @WebService}, {@code @WebParam}, {@code @WebResult}, {@code action}), {@code package-info.java} y los clientes.
 * Forma sugerida: {@code http://<dominio-invertido>/} con barra final (regla R2 de CONVENCION_SOAP.md).
 */
public final class EspacioNombres {

    public static final String SERVICIO = "http://ws.ejemplo.edu.ec/";

    private EspacioNombres() {}
}
