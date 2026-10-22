namespace ec.edu.monster.api;

/// <summary>
/// URL base de la API REST. Se arma COMPLETA (host, puerto y prefijo) y no se asume su forma: cambiar de servidor
/// es cambiar solo esta clase o la variable de entorno. Termina siempre en "/" porque con
/// <c>HttpClient.BaseAddress</c> una ruta relativa sin barra inicial ("auth/login") solo se concatena bien
/// si la base termina en "/".
/// Android: el emulador ve el PC como 10.0.2.2 y un teléfono físico necesita la IP del PC; <c>localhost</c> no sirve.
/// </summary>
public static class ConfiguracionApi
{
    // CAMBIAR: puerto y prefijo de la API del servidor. Kestrel (perfil http): 5267; IIS Express: 57592.
    // Prefijo de ejemplo: "api" (http://host:5267/api/); un servidor Java usa su contexto (Contexto/api).
    public const int Puerto = 5267;
    public const string Ruta = "CAMBIAR_PREFIJO_API";

    // CAMBIAR: solo para teléfono físico, IP del PC en la red local
    public const string IpPc = "127.0.0.1";

    public const string VariableEntorno = "API_BASE_URL";

    public static readonly TimeSpan TiempoLimite = TimeSpan.FromSeconds(10);

    /// <summary>Variable <c>API_BASE_URL</c> si existe; si no, la URL por defecto de la plataforma.</summary>
    public static string ObtenerUrlBase() => Resolver(Environment.GetEnvironmentVariable(VariableEntorno));

    /// <summary>URL base por defecto según la plataforma en ejecución (con «/» final).</summary>
    public static string UrlPorDefecto()
    {
        var host = DeviceInfo.Platform == DevicePlatform.Android
            ? (DeviceInfo.DeviceType == DeviceType.Virtual ? "10.0.2.2" : IpPc)
            : "localhost";
        return $"http://{host}:{Puerto}/{Ruta}/";
    }

    // La URL por defecto solo se pide si no hay valor: depende de DeviceInfo, que solo existe con la app en ejecución.
    internal static string Resolver(string? valorVariable) =>
        Normalizar(string.IsNullOrWhiteSpace(valorVariable) ? UrlPorDefecto() : valorVariable);

    internal static string Normalizar(string url) => url.Trim().TrimEnd('/') + "/";
}
