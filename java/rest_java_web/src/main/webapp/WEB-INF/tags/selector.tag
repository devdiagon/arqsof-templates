<%@ tag description="Lista desplegable con su etiqueta encima. Las opciones salen de datos del servidor." pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="nombre" required="false" description="Nombre del parámetro (por defecto, el id)." %>
<%@ attribute name="etiqueta" required="false" %>
<%@ attribute name="opciones" type="java.util.Collection" required="true" description="Textos; se envían tal cual los entregó el servidor." %>
<%@ attribute name="seleccionada" required="false" %>
<div>
    <c:if test="${not empty etiqueta}"><label class="etiqueta" for="${id}"><c:out value="${etiqueta}"/></label></c:if>
    <select class="campo" id="${id}" name="${empty nombre ? id : nombre}">
        <c:forEach var="opcion" items="${opciones}">
            <option value="<c:out value='${opcion}'/>"${opcion == seleccionada ? ' selected' : ''}><c:out value="${opcion}"/></option>
        </c:forEach>
    </select>
</div>
