using ec.edu.monster.excepciones;
using ec.edu.monster.servicio;

namespace ec.edu.monster.controlador;

/// <summary>Pantalla de acceso. Al autenticarse avisa con <c>alAutenticar</c>; no conoce la pantalla siguiente.</summary>
public class LoginControlador : ControladorBase
{
    private readonly IAutenticador _autenticador;
    private readonly Action _alAutenticar;
    private string _usuario = string.Empty;

    /// <param name="mensajeInicial">Aviso a mostrar al abrir (p. ej. sesión expirada).</param>
    public LoginControlador(IAutenticador autenticador, Action alAutenticar, string? mensajeInicial = null)
    {
        _autenticador = autenticador;
        _alAutenticar = alAutenticar;
        Mensaje = mensajeInicial;
        IniciarSesion = new ComandoAsincrono(IniciarSesionAsync, () => !Ocupado, this);
    }

    public string Usuario
    {
        get => _usuario;
        set => Asignar(ref _usuario, value);
    }

    /// <summary>
    /// La vista entrega aquí cómo leer la contraseña (un PasswordBox no admite enlace de datos). Se lee solo al
    /// iniciar sesión: el controlador no la guarda ni la expone.
    /// </summary>
    public Func<string> LeerContrasenia { get; set; } = () => string.Empty;

    public ComandoAsincrono IniciarSesion { get; }

    private async Task IniciarSesionAsync()
    {
        var usuario = Usuario.Trim();
        var contrasenia = LeerContrasenia();
        if (usuario.Length == 0 || contrasenia.Length == 0)
        {
            Mensaje = "Ingrese el usuario y la contraseña.";
            return;
        }

        Mensaje = null;
        Ocupado = true;
        try
        {
            await _autenticador.IniciarSesionAsync(usuario, contrasenia);
        }
        catch (ExcepcionServicio ex)
        {
            // Solo el mensaje, nunca el código; se conservan los datos capturados.
            Mensaje = ex.Message;
            return;
        }
        catch (Exception)
        {
            Mensaje = "Ocurrió un error inesperado. Intente de nuevo.";
            return;
        }
        finally
        {
            Ocupado = false;
        }

        _alAutenticar();
    }
}
