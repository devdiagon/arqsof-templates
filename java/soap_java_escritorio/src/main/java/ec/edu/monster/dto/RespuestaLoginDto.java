package ec.edu.monster.dto;


/**
 * @author: Frederick Tipán
 */
public class RespuestaLoginDto extends RespuestaBaseDto {

    protected String token;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
