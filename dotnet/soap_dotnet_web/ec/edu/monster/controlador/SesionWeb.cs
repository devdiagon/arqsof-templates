using ec.edu.monster.modelo;

namespace ec.edu.monster.controlador;

/// <summary>
/// Une la <see cref="Sesion"/> (token en memoria) con la sesión de ASP.NET Core del usuario: se lee al crearla por
/// petición y <see cref="PersistirSesionFiltro"/> la guarda antes de escribir la respuesta. El token no se guarda
/// en disco, ni en cookies (la cookie solo lleva el identificador de la sesión), ni en logs.
/// </summary>
public static class SesionWeb
{
    public const string Clave = "token";

    public static Sesion Crear(IServiceProvider servicios)
    {
        var sesionHttp = servicios.GetRequiredService<IHttpContextAccessor>().HttpContext?.Session;
        return new Sesion { Token = sesionHttp?.GetString(Clave) };
    }

    public static void Guardar(ISession sesionHttp, Sesion sesion)
    {
        if (sesion.Token is null)
            sesionHttp.Remove(Clave);
        else
            sesionHttp.SetString(Clave, sesion.Token);
    }
}
