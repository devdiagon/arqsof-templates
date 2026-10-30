using ec.edu.monster.vista;
using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.controlador;

/// <summary>
/// Página de último recurso (<c>/error</c>) para una excepción que ningún controlador trató: el mensaje genérico, sin traza.
/// Los controladores tratan sus errores; esto solo cubre lo imprevisto.
/// </summary>
public class ErrorControlador : ControladorBase
{
    [Route("error")]
    [ResponseCache(NoStore = true, Location = ResponseCacheLocation.None)]
    public IActionResult Error() => View("Error", new ErrorModelo(Textos.ErrorInesperado));
}
