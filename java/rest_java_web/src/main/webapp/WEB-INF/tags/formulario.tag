<%@ tag description="Formulario POST de una pantalla. Al enviarse deshabilita los controles y muestra el indicador de progreso (app.js)." pageEncoding="UTF-8" body-content="scriptless" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ attribute name="accion" required="true" description="Ruta relativa al contexto, p. ej. /login." %>
<%@ attribute name="clase" required="false" description="Clase CSS del formulario (por defecto «formulario»: separa los hijos 16)." %>
<%@ attribute name="ocupado" required="false" description="Texto mientras espera (Procesando…, Consultando…, Iniciando sesión…)." %>
<form class="${empty clase ? 'formulario' : clase}" method="post" action="${pageContext.request.contextPath}${accion}" data-ocupable>
    <jsp:doBody/>
    <ui:progreso texto="${ocupado}"/>
</form>
