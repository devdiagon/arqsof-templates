package ec.edu.monster.dto;

/**
 * Forma común de toda respuesta del servidor (regla R5 de docs/CONVENCION_REST.md): {@code estado} (OK/ERROR), {@code codigo} y {@code mensaje}.
 * Es un POJO del cliente: la implementación de {@code api} la llena al leer el JSON de la respuesta. Los DTO de cada
 * operación del dominio la extienden.
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

    public boolean esOk() { return "OK".equals(estado); }

    public void setEstado(String estado) { this.estado = estado; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
