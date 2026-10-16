package ec.edu.monster.api;

/**
 * Lo que devuelve el transporte: codigo de estado HTTP y cuerpo (texto, normalmente JSON).
 *
 * @author: Frederick Tipán
 */
public final class RespuestaHttp {

    private final int codigo;
    private final String cuerpo;

    public RespuestaHttp(int codigo, String cuerpo) {
        this.codigo = codigo;
        this.cuerpo = cuerpo;
    }

    public int codigo() { return codigo; }

    public String cuerpo() { return cuerpo; }

    public boolean esExito() {
        return codigo >= 200 && codigo < 300;
    }
}
