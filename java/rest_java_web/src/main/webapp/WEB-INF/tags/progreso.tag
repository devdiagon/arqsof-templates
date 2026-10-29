<%@ tag description="Indicador de progreso con su texto. Oculto hasta que app.js marca el formulario como ocupado; ocupa el lugar del mensaje." pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="texto" required="false" description="Texto de la espera (Procesando…, Consultando…, Iniciando sesión…). Por defecto Procesando…." %>
<div class="progreso" role="status">
    <span class="progreso__giro" aria-hidden="true"></span>
    <span class="progreso__texto"><c:out value="${empty texto ? Textos.PROCESANDO : texto}"/></span>
</div>
