<%@ tag description="Dos campos en horizontal con una flecha entre ambos (el de la izquierda apunta al de la derecha). Bajo 600 px se apilan y la flecha apunta hacia abajo." pageEncoding="UTF-8" %>
<%@ attribute name="izquierda" fragment="true" required="true" description="Campo de origen (un ui:campo)." %>
<%@ attribute name="derecha" fragment="true" required="true" description="Campo de destino (un ui:campo)." %>
<div class="fila-campos">
    <div><jsp:invoke fragment="izquierda"/></div>
    <svg class="fila-campos__flecha" width="24" height="24" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
        <path d="M4 12h15M13 6l6 6-6 6" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
    <div><jsp:invoke fragment="derecha"/></div>
</div>
