package ec.edu.monster.dto;

/**
 * Cuerpo de la peticion de inicio de sesion.
 *
 * @author: Frederick Tipán
 */
public class SolicitudLoginDto {

    private final String usuario;
    private final String contrasenia;

    public SolicitudLoginDto(String usuario, String contrasenia) {
        this.usuario = usuario;
        this.contrasenia = contrasenia;
    }

    public String getUsuario() { return usuario; }
    public String getContrasenia() { return contrasenia; }
}
