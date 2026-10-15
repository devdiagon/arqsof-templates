package ec.edu.monster.filtro;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

/** Lee los formularios como UTF-8 (tildes, «ñ») antes de que alguien consulte un parámetro. Aplica a toda la aplicación. */
@WebFilter(filterName = "FiltroCodificacion", urlPatterns = "/*")
public class FiltroCodificacion implements Filter {

    @Override
    public void doFilter(ServletRequest peticion, ServletResponse respuesta, FilterChain cadena)
            throws IOException, ServletException {
        peticion.setCharacterEncoding("UTF-8");
        cadena.doFilter(peticion, respuesta);
    }
}
