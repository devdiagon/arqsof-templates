package ec.edu.monster.controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * DOMINIO (ejemplo): controlador de la pantalla inicial; se reemplaza por los del dominio (punto de enganche: el proyecto
 * lo sustituye por su login o su menú).
 *
 * Molde para una operación del servidor (ver README §4):
 * <pre>
 * try {
 *     peticion.setAttribute("items", ContextoWeb.x(peticion).items());
 * } catch (ExcepcionServicio e) {
 *     peticion.setAttribute("error", e.getMessage());         // solo el mensaje, nunca el código
 * }
 * ContextoWeb.mostrar(peticion, respuesta, "items");          // /WEB-INF/vistas/items.jsp
 * </pre>
 */
@WebServlet(name = "InicioControlador", urlPatterns = "/inicio")
public class InicioControlador extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        ContextoWeb.mostrar(peticion, respuesta, "inicio");
    }
}
