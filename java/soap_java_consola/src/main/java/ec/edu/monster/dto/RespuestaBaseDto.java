package ec.edu.monster.dto;

/**
 * Forma común de toda respuesta del servidor (regla R6 de CONVENCION_SOAP.md): {@code estado} (OK/ERROR), {@code codigo} y {@code mensaje}.
 * Es un POJO del cliente: el envoltorio de {@code ws} lo llena copiando la respuesta que genera wsimport. Los DTO de cada
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
