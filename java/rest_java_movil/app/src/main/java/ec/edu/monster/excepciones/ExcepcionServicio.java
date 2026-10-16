package ec.edu.monster.excepciones;

/**
 * @author: Frederick Tipán
 */
public class ExcepcionServicio extends RuntimeException {

    public static final String RED = "RED";
    public static final String MENSAJE_SERVIDOR_NO_DISPONIBLE = "Servidor no disponible, intente más tarde";

    private final String codigo;

    public ExcepcionServicio(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public ExcepcionServicio(String codigo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = codigo;
    }

    /** Sin respuesta del servidor: caido, API no desplegada, sin red o timeout. */
    public static ExcepcionServicio servidorNoDisponible(Throwable causa) {
        return new ExcepcionServicio(RED, MENSAJE_SERVIDOR_NO_DISPONIBLE, causa);
    }

    public String getCodigo() {
        return codigo;
    }

    public boolean exigeLogin() {
        return "AUTH-002".equals(codigo) || "AUTH-003".equals(codigo);
    }

    public boolean servidorNoDisponible() {
        return RED.equals(codigo);
    }
}
