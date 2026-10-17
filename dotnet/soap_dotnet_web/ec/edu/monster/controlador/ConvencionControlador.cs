using Microsoft.AspNetCore.Mvc.ApplicationModels;

namespace ec.edu.monster.controlador;

/// <summary>
/// Las clases se llaman <c>XxxControlador</c> (convención del equipo) y ASP.NET Core solo quita el sufijo
/// «Controller»: aquí se quita «Controlador» para que la ruta y las vistas usen <c>Xxx</c>.
/// </summary>
public class ConvencionControlador : IControllerModelConvention
{
    private const string Sufijo = "Controlador";

    public void Apply(ControllerModel controlador)
    {
        if (controlador.ControllerName.EndsWith(Sufijo, StringComparison.Ordinal))
            controlador.ControllerName = controlador.ControllerName[..^Sufijo.Length];
    }
}
