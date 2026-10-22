using ec.edu.monster.excepciones;

namespace ec.edu.monster.controlador;

/// <summary>
/// Base de los controladores web. <c>[Controller]</c> los hace descubribles aunque su nombre termine en
/// «Controlador». Los controladores hablan con los servicios y devuelven vistas con modelos de vista; nunca
/// importan <c>api</c> ni <c>dto</c> (los DTO no salen de <c>servicio</c>).
/// </summary>
[Controller]
public abstract class ControladorBase : Controller
{
    public const string ClaveAviso = "aviso";

    /// <summary>
    /// Token vencido o inválido (<see cref="ExcepcionServicio.ExigeLogin"/>): el servicio ya descartó el token;
    /// se vuelve al acceso con el mensaje del servidor.
    /// </summary>
    protected IActionResult VolverAlAcceso(ExcepcionServicio ex)
    {
        TempData[ClaveAviso] = ex.Message;
        return RedirectToAction(nameof(AccesoControlador.Acceder), "Acceso");
    }
}
