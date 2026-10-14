<%@ tag description="Encabezado de pantalla: panel con un único texto, siempre arriba y sin botones." pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="aplicacion" type="java.lang.Boolean" required="false" description="true en la pantalla de acceso: título de aplicación, centrado y más grande." %>
<h1 class="encabezado${aplicacion ? ' encabezado--aplicacion' : ''}"><jsp:doBody/></h1>
