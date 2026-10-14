<%@ tag description="Campo de texto con su etiqueta encima (texto, contraseña...). El valor se escapa." pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="nombre" required="false" description="Nombre del parámetro (por defecto, el id)." %>
<%@ attribute name="etiqueta" required="false" description="Texto de la etiqueta (encima del campo)." %>
<%@ attribute name="tipo" required="false" description="text (por defecto), password..." %>
<%@ attribute name="valor" required="false" %>
<%@ attribute name="placeholder" required="false" description="Describe el dato esperado; no repetir la etiqueta." %>
<%@ attribute name="autocomplete" required="false" description="Por defecto off." %>
<%@ attribute name="inputmode" required="false" description="decimal para números (teclado móvil); el campo sigue siendo texto. Por defecto text." %>
<%@ attribute name="invalido" type="java.lang.Boolean" required="false" description="Borde de error." %>
<%@ attribute name="autofocus" type="java.lang.Boolean" required="false" %>
<div>
    <c:if test="${not empty etiqueta}"><label class="etiqueta" for="${id}"><c:out value="${etiqueta}"/></label></c:if>
    <input class="campo${invalido ? ' campo--error' : ''}" type="${empty tipo ? 'text' : tipo}" id="${id}" name="${empty nombre ? id : nombre}"
           autocomplete="${empty autocomplete ? 'off' : autocomplete}" inputmode="${empty inputmode ? 'text' : inputmode}"${autofocus ? ' autofocus' : ''}
           placeholder="<c:out value='${placeholder}'/>" aria-label="<c:out value='${empty etiqueta ? placeholder : etiqueta}'/>"
           value="<c:out value='${valor}'/>">
</div>
