<%@ tag description="Botón secundario «Cerrar sesión» (POST /logout), separado 32 del bloque anterior. Va en toda pantalla protegida que ofrezca salir." pageEncoding="UTF-8" %>
<form class="acciones-salida" method="post" action="${pageContext.request.contextPath}/logout" data-ocupable>
    <button class="boton boton--secundario boton--ancho" type="submit">Cerrar sesión</button>
    <div class="progreso" role="status" aria-label="Cargando"></div>
</form>
