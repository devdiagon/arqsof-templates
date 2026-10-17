using ec.edu.monster.excepciones;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.controlador;

/// <summary>Pantalla de acceso (compartida por todo dominio). Al autenticarse pasa a <c>Inicio</c>; no conoce la pantalla siguiente más allá de eso.</summary>
public class AccesoControlador(IAutenticador autenticador, Sesion sesion) : ControladorBase
{
    [HttpGet]
    public IActionResult Acceder()
    {
        if (sesion.Token is not null)
            return RedirectToAction("Index", "Inicio");

        return View(new AccesoModelo { Mensaje = TempData[ClaveAviso] as string });
    }

    [HttpPost]
    public async Task<IActionResult> Acceder(AccesoModelo modelo)
    {
        var usuario = (modelo.Usuario ?? string.Empty).Trim();
        var contrasenia = modelo.Contrasenia ?? string.Empty;
        if (usuario.Length == 0 || contrasenia.Length == 0)
            return Fallo(usuario, "Ingrese el usuario y la contraseña.");

        try
        {
            await autenticador.IniciarSesionAsync(usuario, contrasenia);
        }
        catch (ExcepcionServicio ex)
        {
            // Solo el mensaje, nunca el código; se conserva el usuario capturado.
            return Fallo(usuario, ex.Message);
        }
        catch (Exception)
        {
            return Fallo(usuario, "Ocurrió un error inesperado. Intente de nuevo.");
        }

        return RedirectToAction("Index", "Inicio");
    }

    [HttpPost]
    public IActionResult CerrarSesion()
    {
        sesion.Token = null;
        return RedirectToAction(nameof(Acceder));
    }

    private ViewResult Fallo(string usuario, string mensaje) =>
        View(nameof(Acceder), new AccesoModelo { Usuario = usuario, Mensaje = mensaje });
}
