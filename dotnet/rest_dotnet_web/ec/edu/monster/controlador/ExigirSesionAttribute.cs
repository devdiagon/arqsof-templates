using ec.edu.monster.modelo;
using Microsoft.AspNetCore.Mvc.Filters;

namespace ec.edu.monster.controlador;

/// <summary>Protege una pantalla: sin token en la <see cref="Sesion"/> redirige al acceso (CLIENTES_CONSTRUCT.md, «Web»).</summary>
[AttributeUsage(AttributeTargets.Class | AttributeTargets.Method)]
public sealed class ExigirSesionAttribute : Attribute, IActionFilter
{
    public void OnActionExecuting(ActionExecutingContext contexto)
    {
        var sesion = contexto.HttpContext.RequestServices.GetRequiredService<Sesion>();
        if (sesion.Token is null)
            contexto.Result = new RedirectToActionResult(nameof(AccesoControlador.Acceder), "Acceso", null);
    }

    public void OnActionExecuted(ActionExecutedContext contexto) { }
}
