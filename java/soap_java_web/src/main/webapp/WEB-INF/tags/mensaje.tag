<%@ tag description="Panel de mensaje o resultado. No se dibuja si no hay texto. Mostrar solo el mensaje del sistema, nunca un código técnico." pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="mensaje" required="false" description="Texto; vacío = no se muestra." %>
<%@ attribute name="tipo" required="false" description="error (por defecto), exito o resultado (centrado, tamaño de resultado)." %>
<c:if test="${not empty mensaje}">
    <c:choose>
        <c:when test="${tipo == 'resultado'}"><div class="mensaje mensaje--resultado" role="status"><c:out value="${mensaje}"/></div></c:when>
        <c:when test="${tipo == 'exito'}"><div class="mensaje mensaje--exito" role="status"><c:out value="${mensaje}"/></div></c:when>
        <c:otherwise><div class="mensaje" role="alert"><c:out value="${mensaje}"/></div></c:otherwise>
    </c:choose>
</c:if>
