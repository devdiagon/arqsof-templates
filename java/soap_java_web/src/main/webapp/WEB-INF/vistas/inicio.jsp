<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- DOMINIO (ejemplo): pantalla inicial mínima (punto de enganche); se reemplaza por las del dominio. Solo usa los componentes de WEB-INF/tags.
     Si el proyecto tiene login, aquí va ui:cerrar-sesion (POST /logout). --%>
<ui:pantalla titulo="Inicio">
    <ui:encabezado>Inicio</ui:encabezado>
    <%-- DOMINIO: aquí van los datos y acciones de la pantalla, p. ej.:
         <ui:lista-acciones elementos="${categorias}" destino="/detalle" parametro="categoria"/> --%>
</ui:pantalla>
