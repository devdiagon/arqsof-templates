using ec.edu.monster.controlador;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.vista;

/// <summary>
/// Crea los controladores de cada pantalla y les entrega las acciones de navegación (Shell). Los controladores no
/// se conocen entre sí ni conocen Shell: piden el cambio de pantalla por estos delegados.
/// Flujo de la plantilla: login → inicio; al cerrar o expirar la sesión se vuelve al login (<see cref="IrAlLogin"/>).
/// </summary>
public class Navegacion
{
    public const string RutaLogin = "login";
    public const string RutaInicio = "inicio";

    private readonly IAutenticador _autenticador;
    private readonly Sesion _sesion;
    private string? _avisoPendiente;

    // DOMINIO: recibir aquí los servicios del dominio (ServicioXxx) para crear los controladores de sus pantallas.
    public Navegacion(IAutenticador autenticador, Sesion sesion)
    {
        _autenticador = autenticador;
        _sesion = sesion;
    }

    /// <summary>Login con el aviso pendiente (p. ej. sesión expirada), que se muestra una sola vez.</summary>
    public LoginControlador CrearLogin()
    {
        var aviso = _avisoPendiente;
        _avisoPendiente = null;
        return new LoginControlador(_autenticador, () => Ir(RutaInicio), aviso);
    }

    // DOMINIO: reemplazar por los controladores de las pantallas del dominio. Una pantalla que recibe un dato
    // (p. ej. el elemento elegido en una lista) lo pide por parámetro de navegación:
    //   Ir(RutaDetalle, new Dictionary<string, object> { ["id"] = id });
    // y toda pantalla que consulte al servidor recibe `mensaje => IrAlLogin(mensaje)` como alExpirarSesion.
    public InicioControlador CrearInicio() => new(alCerrarSesion: () =>
    {
        _sesion.Token = null;
        IrAlLogin(null);
    });

    /// <summary>Ruta absoluta: descarta las pantallas apiladas, así «atrás» no vuelve a una sesión cerrada.</summary>
    public void IrAlLogin(string? aviso)
    {
        _avisoPendiente = aviso;
        Ir($"//{RutaLogin}");
    }

    public void Ir(string ruta, IDictionary<string, object>? parametros = null)
    {
        // Los controladores llaman desde el hilo de la interfaz; el retorno no se espera.
        MainThread.BeginInvokeOnMainThread(async () =>
        {
            if (parametros is null)
                await Shell.Current.GoToAsync(ruta);
            else
                await Shell.Current.GoToAsync(ruta, parametros);
        });
    }
}
