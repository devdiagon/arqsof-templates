<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- Pantalla de acceso (disposición dividida) lista para los proyectos que definen autenticación (la plantilla base no trae
     controlador de login: no hay uno común a todo SOA). Atributos que debe entregar el controlador: «error» (mensaje),
     «usuario» (se conserva al fallar) e «invalido» («usuario» o «clave»: el campo con borde de error). La clave nunca se devuelve. --%>
<ui:pantalla titulo="${Textos.INICIAR_SESION}">
    <ui:formulario accion="/login" ocupado="${Textos.INICIANDO_SESION}">
        <ui:encabezado aplicacion="true"><c:out value="${initParam.nombreAplicacion}"/></ui:encabezado>
        <ui:campo id="usuario" etiqueta="${Textos.USUARIO}" valor="${usuario}" autocomplete="username"
                  autofocus="${invalido != 'clave'}" invalido="${invalido == 'usuario'}"/>
        <ui:campo id="clave" etiqueta="${Textos.CLAVE}" tipo="password" autocomplete="current-password"
                  autofocus="${invalido == 'clave'}" invalido="${invalido == 'clave'}"/>
        <ui:boton centrado="true">${Textos.INICIAR_SESION}</ui:boton>
        <ui:mensaje mensaje="${error}"/>
    </ui:formulario>
</ui:pantalla>
