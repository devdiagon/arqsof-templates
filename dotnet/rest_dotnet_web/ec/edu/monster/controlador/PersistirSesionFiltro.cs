using ec.edu.monster.modelo;
using Microsoft.AspNetCore.Mvc.Filters;

namespace ec.edu.monster.controlador;

/// <summary>
/// Filtro global: antes de escribir el resultado guarda la <see cref="Sesion"/> de la petición en la sesión del
/// usuario (token nuevo tras el login, o borrado tras cerrar sesión / token vencido). Debe ir antes de que se
/// inicie la respuesta para que la cookie de la sesión llegue al navegador.
/// </summary>
public class PersistirSesionFiltro : IResultFilter
{
    public void OnResultExecuting(ResultExecutingContext contexto)
    {
        var sesion = contexto.HttpContext.RequestServices.GetRequiredService<Sesion>();
        SesionWeb.Guardar(contexto.HttpContext.Session, sesion);
    }

    public void OnResultExecuted(ResultExecutedContext contexto) { }
}
