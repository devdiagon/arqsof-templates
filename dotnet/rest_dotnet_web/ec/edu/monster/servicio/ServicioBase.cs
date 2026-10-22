using System.Text.Json;
using ec.edu.monster.api;
using ec.edu.monster.dto;
using ec.edu.monster.excepciones;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio;

/// <summary>
/// Base de los servicios del dominio: token de la sesión, envío por <see cref="IClienteApi"/> y traducción de la
/// respuesta HTTP a un DTO o a <see cref="ExcepcionServicio"/>. Los servicios son la única capa que conoce
/// <c>api</c> y <c>dto</c>.
/// </summary>
public abstract class ServicioBase
{
    private const int HttpOk = 200;
    private const int HttpNoAutorizado = 401;
    private const string CodigoErrorInterno = "ERR-500";

    private static readonly JsonSerializerOptions OpcionesJson = new(JsonSerializerDefaults.Web);

    private readonly IClienteApi _cliente;

    protected Sesion Sesion { get; }

    protected ServicioBase(IClienteApi cliente, Sesion sesion)
    {
        _cliente = cliente;
        Sesion = sesion;
    }

    public bool HaySesion => Sesion.Token is not null;

    public void CerrarSesion() => Sesion.Token = null;

    /// <summary>Token de la sesión; sin él lanza AUTH-002 sin llamar al servidor.</summary>
    protected string ExigirToken() =>
        Sesion.Token ?? throw new ExcepcionServicio("Debe iniciar sesión", ExcepcionServicio.CodigoTokenInvalido);

    /// <summary>
    /// Envía y decide por el código HTTP: solo el 200 devuelve el DTO; cualquier otro código lanza
    /// <see cref="ExcepcionServicio"/> con el código y mensaje del servidor. Con <paramref name="requiereToken"/> exige
    /// sesión antes de llamar y envía el token en la cabecera; el login lo pone en <c>false</c>.
    /// </summary>
    protected async Task<T> EnviarAsync<T>(HttpMethod metodo, string ruta, object? cuerpo = null,
        bool requiereToken = true) where T : RespuestaBaseDto
    {
        var token = requiereToken ? ExigirToken() : null;
        var http = await _cliente.EnviarAsync(metodo, ruta, cuerpo, token);
        var dto = Leer<T>(http.Cuerpo);

        if (http.Codigo == HttpOk)
            return dto ?? throw new ExcepcionServicio(
                "Respuesta no válida del servidor", CodigoErrorInterno, http.Codigo);

        throw CrearExcepcion(http.Codigo, dto, requiereToken);
    }

    private ExcepcionServicio CrearExcepcion(int estadoHttp, RespuestaBaseDto? dto, bool protegida)
    {
        // Cuerpo sin JSON (página de IIS Express, proxy): mensaje genérico con el código HTTP.
        // Un 401 en una operación protegida sin cuerpo útil se trata como token inválido.
        var codigo = dto?.Codigo
            ?? (estadoHttp == HttpNoAutorizado && protegida ? ExcepcionServicio.CodigoTokenInvalido : $"HTTP-{estadoHttp}");
        var mensaje = !string.IsNullOrWhiteSpace(dto?.Mensaje)
            ? dto.Mensaje
            : $"El servidor respondió con un error inesperado (HTTP {estadoHttp})";

        var excepcion = new ExcepcionServicio(mensaje, codigo, estadoHttp);
        if (excepcion.ExigeLogin)
            CerrarSesion();
        return excepcion;
    }

    private static T? Leer<T>(string cuerpo) where T : class
    {
        if (string.IsNullOrWhiteSpace(cuerpo))
            return null;
        try
        {
            return JsonSerializer.Deserialize<T>(cuerpo, OpcionesJson);
        }
        catch (JsonException)
        {
            return null;
        }
    }

    /// <summary>El servidor solo admite el punto como separador decimal; la coma se cambia por punto.</summary>
    public static string NormalizarValor(string valor) => valor.Trim().Replace(',', '.');
}
