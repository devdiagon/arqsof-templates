using System.Text.Json;
using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.controlador;

/// <summary>
/// Base de los controladores de pantalla (ARQUITECTURA_CLIENTE.md §4.6, web). Las clases se llaman <c>XxxControlador</c> (la
/// convención del equipo), no <c>XxxController</c>: el atributo <see cref="ControllerAttribute"/>, que heredan, hace que MVC
/// las reconozca. Las rutas se declaran con atributos en cada controlador. Quien llama al servidor es el controlador, nunca
/// el navegador.
///
/// Cada controlador valida los campos vacíos antes de llamar (en el orden de la pantalla), muestra solo el mensaje del error
/// (nunca el código) y conserva lo escrito ante un error. Una operación que cambia datos usa Post/Redirect/Get con
/// <see cref="GuardarFlash{T}"/> y <see cref="TomarFlash{T}"/>; una consulta devuelve la página directamente.
/// </summary>
[Controller]
public abstract class ControladorBase : Controller
{
    /// <summary>Texto sin espacios en los extremos; nulo se trata como vacío.</summary>
    protected static string Texto(string? valor) => valor?.Trim() ?? "";

    protected static bool Vacio(string? valor) => string.IsNullOrWhiteSpace(valor);

    /// <summary>
    /// Deja lo que la siguiente petición <c>GET</c> mostrará una sola vez (Post/Redirect/Get): recargar la página no repite la
    /// operación. Va en <c>TempData</c>; nunca debe llevar una contraseña.
    /// </summary>
    protected void GuardarFlash<T>(T modelo) => TempData[ClaveFlash<T>()] = JsonSerializer.Serialize(modelo);

    /// <summary>Lo guardado por <see cref="GuardarFlash{T}"/>, que se borra al leerlo; <c>null</c> si no hay nada.</summary>
    protected T? TomarFlash<T>() where T : class =>
        TempData[ClaveFlash<T>()] is string json ? JsonSerializer.Deserialize<T>(json) : null;

    private static string ClaveFlash<T>() => "flash:" + typeof(T).Name;
}
