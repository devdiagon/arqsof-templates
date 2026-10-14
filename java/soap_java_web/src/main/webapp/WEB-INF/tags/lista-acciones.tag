<%@ tag description="Lista de acciones: una columna de botones primarios de ancho completo, generados con datos del servidor (nunca fijos en la vista)." pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="elementos" type="java.util.Collection" required="true" %>
<%@ attribute name="destino" required="true" description="Ruta relativa al contexto a la que lleva cada botón, p. ej. /detalle." %>
<%@ attribute name="parametro" required="true" description="Nombre del parámetro que recibe el texto del elemento." %>
<div class="lista-acciones">
    <c:forEach var="elemento" items="${elementos}">
        <c:url var="enlace" value="${destino}"><c:param name="${parametro}" value="${elemento}"/></c:url>
        <a class="boton boton--primario boton--ancho" href="${enlace}"><c:out value="${elemento}"/></a>
    </c:forEach>
</div>
