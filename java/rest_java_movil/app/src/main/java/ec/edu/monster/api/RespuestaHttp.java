package ec.edu.monster.api;

import java.util.Objects;

/**
 * Lo que devuelve el transporte: código HTTP y cuerpo (texto, posiblemente vacío). Se lee siempre, también en un error.
 * Es una clase y no un {@code record} porque el proyecto Android compila con Java 11; expone los mismos accesores.
 */
public final class RespuestaHttp {

    private final int codigo;
    private final String cuerpo;

    public RespuestaHttp(int codigo, String cuerpo) {
        this.codigo = codigo;
        this.cuerpo = cuerpo;
    }

    public int codigo() {
        return codigo;
    }

    public String cuerpo() {
        return cuerpo;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof RespuestaHttp)) {
            return false;
        }
        RespuestaHttp r = (RespuestaHttp) otro;
        return codigo == r.codigo && Objects.equals(cuerpo, r.cuerpo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo, cuerpo);
    }

    @Override
    public String toString() {
        return "RespuestaHttp[codigo=" + codigo + ", cuerpo=" + cuerpo + "]";
    }
}
