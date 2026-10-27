<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- Pantalla de acceso lista para los proyectos que definen autenticación (la plantilla base no trae controlador de login: no hay uno común a todo SOA).
     Atributos que debe entregar el controlador: «error» (mensaje) y «usuario» (se conserva al fallar). --%>
<ui:pantalla titulo="Iniciar sesión">
    <ui:formulario accion="/login">
        <ui:encabezado aplicacion="true"><c:out value="${initParam.nombreAplicacion}"/></ui:encabezado>
        <ui:campo id="usuario" etiqueta="Usuario" valor="${usuario}" autocomplete="username" autofocus="true"/>
        <ui:campo id="contrasenia" etiqueta="Clave" tipo="password" autocomplete="current-password"/>
        <ui:mensaje mensaje="${error}"/>
        <ui:boton centrado="true">Iniciar sesión</ui:boton>
    </ui:formulario>
</ui:pantalla>
