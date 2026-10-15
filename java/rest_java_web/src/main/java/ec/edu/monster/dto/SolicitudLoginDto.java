package ec.edu.monster.dto;

/** Cuerpo de la petición de inicio de sesión. */
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
