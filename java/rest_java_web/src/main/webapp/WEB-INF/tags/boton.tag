<%@ tag description="Botón primario (acción principal) o secundario (salida o regreso). Con href es un enlace; sin href envía el formulario." pageEncoding="UTF-8" body-content="scriptless" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="tipo" required="false" description="primario (por defecto) o secundario." %>
<%@ attribute name="href" required="false" description="Ruta relativa al contexto. Sin ella, el botón envía el formulario." %>
<%@ attribute name="ancho" type="java.lang.Boolean" required="false" description="100 % del contenido (acciones de lista y de salida)." %>
<%@ attribute name="centrado" type="java.lang.Boolean" required="false" description="Ajustado al texto y centrado (acción principal de un formulario)." %>
<%@ attribute name="salida" type="java.lang.Boolean" required="false" description="Separa 32 del bloque anterior (acciones de salida)." %>
<%@ attribute name="deshabilitado" type="java.lang.Boolean" required="false" %>
<jsp:doBody var="texto"/>
<c:set var="clases" value="boton boton--${empty tipo ? 'primario' : tipo}${ancho ? ' boton--ancho' : ''}"/>
<c:if test="${centrado}"><div class="accion-principal"></c:if>
<c:if test="${salida}"><div class="acciones-salida"></c:if>
<c:choose>
    <c:when test="${not empty href}"><a class="${clases}" href="${pageContext.request.contextPath}${href}">${texto}</a></c:when>
    <c:otherwise><button class="${clases}" type="submit"${deshabilitado ? ' disabled' : ''}>${texto}</button></c:otherwise>
</c:choose>
<c:if test="${salida}"></div></c:if>
<c:if test="${centrado}"></div></c:if>
