package ec.edu.monster.dto;

/**
 * Forma común de toda respuesta JSON del servidor. Los DTO de cada operación la extienden.
 * Los campos pueden faltar (errores de la plataforma traen solo estas tres).
 *
 * @author: Frederick Tipán
 */
public class RespuestaBaseDto {

    protected String estado;
    protected String codigo;
    protected String mensaje;

    public String getEstado() { return estado; }
    public String getCodigo() { return codigo; }
    public String getMensaje() { return mensaje; }

    public void setEstado(String estado) { this.estado = estado; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
