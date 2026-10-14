// Mientras hay una petición en curso: controles deshabilitados e indicador visible.
// Se deshabilita después del envío (setTimeout) porque un campo deshabilitado no viaja en el formulario.
(function () {
    'use strict';

    function ocupar(contenedor, ocupado) {
        contenedor.classList.toggle('ocupado', ocupado);
        contenedor.querySelectorAll('input, select, button, a.boton').forEach(function (control) {
            if (control.tagName === 'A') {
                control.setAttribute('aria-disabled', String(ocupado));
            } else {
                control.disabled = ocupado;
            }
        });
    }

    document.querySelectorAll('form[data-ocupable]').forEach(function (formulario) {
        formulario.addEventListener('submit', function () {
            setTimeout(function () { ocupar(formulario.closest('.columna'), true); }, 0);
        });
    });

    // Al volver con «Atrás» el navegador puede restaurar la página en estado ocupado.
    window.addEventListener('pageshow', function (e) {
        if (e.persisted) {
            document.querySelectorAll('.columna.ocupado').forEach(function (c) { ocupar(c, false); });
        }
    });

    // Enlaces de acción: evita el doble clic mientras carga la siguiente pantalla.
    document.querySelectorAll('a.boton').forEach(function (enlace) {
        enlace.addEventListener('click', function (e) {
            if (enlace.getAttribute('aria-disabled') === 'true') {
                e.preventDefault();
                return;
            }
            if (e.ctrlKey || e.metaKey || e.shiftKey || e.altKey) {
                return; // abre en otra pestaña: esta página sigue activa
            }
            setTimeout(function () { ocupar(enlace.closest('.columna'), true); }, 0);
        });
    });
})();
