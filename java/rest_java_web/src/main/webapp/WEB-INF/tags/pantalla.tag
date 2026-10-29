<%@ tag description="Estructura común de toda pantalla: HTML y script. Modo «acceso»: disposición dividida (columna de contenido + imagen decorativa). Modo «aplicacion»: ancho completo con la imagen como fondo." pageEncoding="UTF-8" body-content="scriptless" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="titulo" required="true" description="Título de la pestaña (se completa con el nombre de la aplicación)." %>
<%@ attribute name="modo" required="false" description="acceso (dividida; por defecto) o aplicacion (ancho completo, imagen de fondo con opacidad)." %>
<%@ attribute name="ancho" required="false" description="Solo en modo aplicacion: ancho máximo del contenido. formulario (480; por defecto), amplio (640) o completo." %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${titulo}"/> · <c:out value="${initParam.nombreAplicacion}"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/recursos/css/estilos.css">
</head>
<body class="${empty initParam.tema ? 'tema-oscuro' : initParam.tema}">
<c:choose>
    <c:when test="${modo == 'aplicacion'}">
        <%-- La imagen es una capa fija detrás de la página (CSS); no contiene información ni controles. --%>
        <div class="pagina pagina--aplicacion">
            <main class="contenido contenido--${empty ancho ? 'formulario' : ancho}">
                <div class="columna">
                    <jsp:doBody/>
                </div>
            </main>
        </div>
    </c:when>
    <c:otherwise>
        <div class="pagina">
            <main class="zona-contenido">
                <div class="columna">
                    <jsp:doBody/>
                </div>
            </main>
            <%-- La imagen es parte de la ventana, no de cada pantalla. Solo decora: sin información ni controles. --%>
            <div class="zona-imagen" aria-hidden="true"></div>
        </div>
    </c:otherwise>
</c:choose>
<script src="${pageContext.request.contextPath}/recursos/js/app.js"></script>
</body>
</html>
