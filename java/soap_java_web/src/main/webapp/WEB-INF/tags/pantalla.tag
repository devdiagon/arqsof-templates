<%@ tag description="Estructura común de toda pantalla: HTML, disposición dividida (columna de contenido + imagen decorativa) y script." pageEncoding="UTF-8" body-content="scriptless" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="titulo" required="true" description="Título de la pestaña (se completa con el nombre de la aplicación)." %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${titulo}"/> · <c:out value="${initParam.nombreAplicacion}"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/recursos/css/estilos.css">
</head>
<body class="${empty initParam.tema ? 'tema-oscuro' : initParam.tema}">
<div class="pagina">
    <main class="zona-contenido">
        <div class="columna">
            <jsp:doBody/>
        </div>
    </main>
    <%-- La imagen es parte de la ventana, no de cada pantalla. Solo decora: sin información ni controles. --%>
    <div class="zona-imagen" aria-hidden="true"></div>
</div>
<script src="${pageContext.request.contextPath}/recursos/js/app.js"></script>
</body>
</html>
