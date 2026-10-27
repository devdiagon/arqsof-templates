<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- DOMINIO (ejemplo): pantalla inicial mínima (punto de enganche), en modo «aplicacion» (ancho completo con la imagen de fondo);
     se reemplaza por las del dominio. Solo usa los componentes de WEB-INF/tags y los textos de Textos (${Textos.X}).
     Si el proyecto tiene login, aquí va ui:cerrar-sesion (POST /logout). --%>
<ui:pantalla titulo="${Textos.INICIO}" modo="aplicacion">
    <ui:encabezado><c:out value="${Textos.INICIO}"/></ui:encabezado>
    <%-- DOMINIO: aquí van los datos y acciones de la pantalla, p. ej.:
         <ui:cuadricula-acciones><ui:boton href="/items" ancho="true">Elementos</ui:boton></ui:cuadricula-acciones> --%>
</ui:pantalla>
