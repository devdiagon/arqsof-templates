<%@ tag description="Tarjeta de resultado: resume el cambio de un valor. Título centrado, monto con signo (verde si suma, rojo si resta), variación, divisor y pie a la derecha. Recibe los textos ya preparados." pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="titulo" required="false" description="Etiqueta o mensaje de éxito." %>
<%@ attribute name="suma" type="java.lang.Boolean" required="true" description="true: el valor sube (verde, +). false: baja (rojo, -)." %>
<%@ attribute name="monto" required="true" description="Monto con signo y símbolo, p. ej. +$250.00." %>
<%@ attribute name="variacion" required="true" description="«antes → después»." %>
<%@ attribute name="pie" required="true" description="Identifica el origen del cambio; va a la derecha." %>
<div class="tarjeta" role="group">
    <c:if test="${not empty titulo}"><div class="tarjeta__titulo"><c:out value="${titulo}"/></div></c:if>
    <div class="tarjeta__monto ${suma ? 'tarjeta__monto--suma' : 'tarjeta__monto--resta'}"><c:out value="${monto}"/></div>
    <div class="tarjeta__variacion"><c:out value="${variacion}"/></div>
    <div class="tarjeta__pie"><c:out value="${pie}"/></div>
</div>
