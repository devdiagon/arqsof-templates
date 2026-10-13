package ec.edu.monster.excepciones;

/**
 * @author: Frederick Tipán
 */
public class ExcepcionServicio extends RuntimeException {

    public static final String RED = "RED";

    private final String codigo;

    public ExcepcionServicio(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public ExcepcionServicio(String codigo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
    
    public boolean exigeLogin() {
        return "AUTH-002".equals(codigo) || "AUTH-003".equals(codigo);
    }
}
