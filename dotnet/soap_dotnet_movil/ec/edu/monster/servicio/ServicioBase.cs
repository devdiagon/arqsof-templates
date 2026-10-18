using ec.edu.monster.excepciones;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio;

/// <summary>
/// Base de los servicios del dominio: token de la sesión y traducción de la respuesta del servidor a
/// <see cref="ExcepcionServicio"/>. Los servicios son la única capa que conoce los tipos generados del proxy.
/// </summary>
public abstract class ServicioBase
{
    private const string EstadoOk = "OK";

    protected Sesion Sesion { get; }

    protected ServicioBase(Sesion sesion)
    {
        Sesion = sesion;
    }

    public bool HaySesion => Sesion.Token is not null;

    public void CerrarSesion() => Sesion.Token = null;

    /// <summary>Token de la sesión; sin él lanza AUTH-002 sin llamar al servidor.</summary>
    protected string ExigirToken() =>
        Sesion.Token ?? throw new ExcepcionServicio("Debe iniciar sesión", ExcepcionServicio.CodigoTokenInvalido);

    /// <summary>Lanza si la respuesta no es nula; úsese antes de leer <c>estado</c>, <c>codigo</c> y <c>mensaje</c>.</summary>
    protected static T ExigirRespuesta<T>(T? respuesta) where T : class =>
        respuesta ?? throw new ExcepcionServicio("Respuesta vacía del servidor", "ERR-500");

    /// <summary>
    /// Pasa si <c>estado</c> es OK; si no, lanza la excepción de negocio con el código y mensaje del servidor
    /// y, si el código exige login (AUTH-002 / AUTH-003), descarta el token. Ejemplo:
    /// <c>Verificar(r.estado, r.codigo, r.mensaje)</c> con los campos de <c>RespuestaBase</c> del proxy.
    /// </summary>
    protected void Verificar(string? estado, string? codigo, string? mensaje)
    {
        if (string.Equals(estado, EstadoOk, StringComparison.OrdinalIgnoreCase))
            return;

        var excepcion = new ExcepcionServicio(mensaje ?? "Error desconocido", codigo ?? "ERR-500");
        if (excepcion.ExigeLogin)
            CerrarSesion();
        throw excepcion;
    }

    /// <summary>El servidor solo admite el punto como separador decimal (R6); la coma se cambia por punto.</summary>
    public static string NormalizarValor(string valor) => valor.Trim().Replace(',', '.');
}
