package ec.edu.monster.api;

/** Lo que devuelve el transporte: código de estado HTTP y cuerpo (texto, normalmente JSON). */
public record RespuestaHttp(int codigo, String cuerpo) {

    public boolean esExito() {
        return codigo >= 200 && codigo < 300;
    }
}
