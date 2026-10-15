package ec.edu.monster.modelo;

/**
 * @author: Frederick Tipán
 */
public class Sesion {

    private String token;

    public Sesion() {
    }

    public Sesion(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
