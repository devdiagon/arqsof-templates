<%@ tag description="Un campo con sus botones a la derecha, alineados con su base. Bajo 600 px los botones pasan debajo." pageEncoding="UTF-8" %>
<%@ attribute name="campo" fragment="true" required="true" description="El ui:campo." %>
<%@ attribute name="acciones" fragment="true" required="true" description="Los botones (un ui:fila-botones)." %>
<div class="campo-con-acciones">
    <div class="campo-con-acciones__campo"><jsp:invoke fragment="campo"/></div>
    <div class="campo-con-acciones__acciones"><jsp:invoke fragment="acciones"/></div>
</div>
