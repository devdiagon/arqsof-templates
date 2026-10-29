<%@ tag description="Encabezado de pantalla: panel con un único texto, siempre arriba y sin botones." pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="aplicacion" type="java.lang.Boolean" required="false" description="true en la pantalla de acceso: título de aplicación, centrado y más grande." %>
<%@ attribute name="plano" type="java.lang.Boolean" required="false" description="true para un texto grande en negrita sin panel (el saludo del menú)." %>
<h1 class="encabezado${aplicacion ? ' encabezado--aplicacion' : ''}${plano ? ' encabezado--plano' : ''}"><jsp:doBody/></h1>
