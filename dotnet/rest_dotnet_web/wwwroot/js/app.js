// Único script de la aplicación (CLIENT_STYLES_DOTNET.md §7.2): al enviar un formulario deshabilita sus
// controles y muestra el indicador de progreso, para evitar el doble envío.
(function () {
    "use strict";

    function marcarOcupado(formulario, ocupado) {
        var controles = formulario.querySelector(".formulario__controles");
        var progreso = formulario.querySelector(".progreso");
        if (controles) controles.disabled = ocupado;
        if (progreso) progreso.hidden = !ocupado;
    }

    document.querySelectorAll("form[data-formulario]").forEach(function (formulario) {
        formulario.addEventListener("submit", function () {
            // Un fieldset deshabilitado no envía sus campos: se deshabilita después de que el navegador los reúna.
            setTimeout(function () { marcarOcupado(formulario, true); }, 0);
        });
        // Al volver con "atrás" el navegador puede restaurar la página en estado ocupado.
        window.addEventListener("pageshow", function () { marcarOcupado(formulario, false); });
    });
})();
