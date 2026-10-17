using ec.edu.monster.modelo;

namespace ec.edu.monster.controlador;

/// <summary>
/// DOMINIO: pantalla de ejemplo que se muestra tras el acceso. Se reemplaza por las pantallas del dominio;
/// sirve de molde mínimo (un controlador por pantalla, protegido con <see cref="ExigirSesionAttribute"/>).
/// </summary>
public class InicioControlador : ControladorBase
{
    [ExigirSesion]
    public IActionResult Index() => View();

    // La reejecuta UseExceptionHandler con el verbo original (incluso POST): sin verbo ni token anti-falsificación.
    [IgnoreAntiforgeryToken]
    public IActionResult Error() => View(new ErrorModelo());
}
