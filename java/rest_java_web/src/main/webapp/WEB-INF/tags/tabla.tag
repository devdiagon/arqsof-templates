<%@ tag description="Tabla de registros: encabezado fijo, filas de 36, columnas numéricas a la derecha. Se desplaza dentro de su contenedor; la página nunca se desplaza en horizontal." pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="columnas" type="java.util.List" required="true" description="Títulos de las columnas." %>
<%@ attribute name="alDerecha" type="java.util.List" required="true" description="Por columna: true si va alineada a la derecha." %>
<%@ attribute name="filas" type="java.util.List" required="true" description="Una lista de textos por fila, uno por columna." %>
<%@ attribute name="titulo" required="false" description="Línea sobre la tabla (p. ej. «Movimientos de la cuenta …»)." %>
<%@ attribute name="cantidad" required="false" description="Texto a la derecha del título (p. ej. «3 movimiento(s)»)." %>
<div class="tabla-bloque">
    <c:if test="${not empty titulo}">
        <div class="tabla-resumen"><strong><c:out value="${titulo}"/></strong><span><c:out value="${cantidad}"/></span></div>
    </c:if>
    <div class="tabla-contenedor">
        <table class="tabla">
            <thead>
                <tr>
                    <c:forEach var="columna" items="${columnas}" varStatus="c">
                        <th scope="col"${alDerecha[c.index] ? ' class="num"' : ''}><c:out value="${columna}"/></th>
                    </c:forEach>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="fila" items="${filas}">
                    <tr>
                        <c:forEach var="celda" items="${fila}" varStatus="c">
                            <td${alDerecha[c.index] ? ' class="num"' : ''}><c:out value="${celda}"/></td>
                        </c:forEach>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>
