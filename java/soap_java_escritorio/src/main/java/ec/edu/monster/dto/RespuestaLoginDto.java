package ec.edu.monster.dto;

import ec.edu.monster.ws.ConfiguracionServicio;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author: Frederick Tipán
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RespuestaLogin", namespace = ConfiguracionServicio.NAMESPACE)
public class RespuestaLoginDto extends RespuestaBaseDto {

    protected String token;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
