package ec.edu.monster.filtro;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.jsp.JspFactory;

/**
 * Hace visible la clase {@code Textos} en todas las expresiones EL de la aplicación (JSP y tag files), de modo que las
 * páginas leen los textos compartidos con los demás clientes sin copiarlos: {@code ${Textos.CODIGO_CUENTA}},
 * {@code ${Textos.saludo(nombre, paterno)}}. Importarla con {@code <%@ page import %>} en cada página <b>no</b> basta:
 * Payara no la pasa a EL (se comprobó con Payara 7.2026.9).
 */
@WebListener
public class ImportaTextos implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        JspFactory.getDefaultFactory().getJspApplicationContext(evento.getServletContext())
                .addELContextListener(e -> e.getELContext().getImportHandler().importClass("ec.edu.monster.vista.Textos"));
    }
}
