using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.controlador;

/// <summary>
/// DOMINIO (ejemplo): pantalla inicial de la plantilla (<c>/</c>). Muestra el patrón de toda pantalla: una acción por ruta
/// con atributo, que entrega su vista. Se reemplaza por los controladores del dominio (README §4).
/// </summary>
public class InicioControlador : ControladorBase
{
    [HttpGet("/")]
    public IActionResult Inicio() => View("Inicio");
}
