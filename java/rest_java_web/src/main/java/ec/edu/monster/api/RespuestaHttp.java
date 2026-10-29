package ec.edu.monster.api;

/** Lo que devuelve el transporte: código HTTP y cuerpo (texto, posiblemente vacío). Se lee siempre, también en un error. */
public record RespuestaHttp(int codigo, String cuerpo) {
}
