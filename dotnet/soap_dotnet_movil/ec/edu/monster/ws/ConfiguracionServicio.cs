namespace ec.edu.monster.ws;

/// <summary>
/// URL del servicio SOAP. Regla R9 de CONVENCION_SOAP.md: se recibe COMPLETA y no se asume su forma
/// (la ruta de un servidor Java y la de uno .NET son distintas); cambiar de servidor es cambiar solo esta configuración.
/// En móvil <c>localhost</c> no sirve en Android: el emulador ve el PC como 10.0.2.2 y un teléfono físico necesita la
/// IP del PC en la red local. <see cref="UrlPorDefecto"/> resuelve el host según la plataforma.
/// </summary>
public static class ConfiguracionServicio
{
    // CAMBIAR: puerto y ruta del servicio. Ejemplos:
    //   .NET (IIS Express, HTTP)    57592  /RutaServicio
    //   .NET (Kestrel, perfil http) 5267   /RutaServicio   (con applicationUrl http://0.0.0.0:5267)
    //   Java (Payara)               8080   /Contexto/Servicio
    public const int Puerto = 57592;
    public const string Ruta = "CAMBIAR_RUTA_SERVICIO";

    // CAMBIAR (solo para teléfono físico): IP del PC en la red local (ipconfig → IPv4), en la misma Wi-Fi. Cambia con la red.
    public const string IpPc = "127.0.0.1";

    public const string VariableEntorno = "WS_URL";

    public static readonly TimeSpan TiempoLimite = TimeSpan.FromSeconds(10);

    /// <summary>Variable <c>WS_URL</c> si existe; si no, la URL por defecto de la plataforma.</summary>
    public static string ObtenerUrl() => Resolver(Environment.GetEnvironmentVariable(VariableEntorno));

    /// <summary>
    /// URL completa del servicio, sin "?wsdl" ni "/" final, según la plataforma en ejecución:
    /// Android emulador → 10.0.2.2; Android físico → <see cref="IpPc"/>; Windows → localhost.
    /// </summary>
    public static string UrlPorDefecto()
    {
        var host = DeviceInfo.Platform == DevicePlatform.Android
            ? (DeviceInfo.DeviceType == DeviceType.Virtual ? "10.0.2.2" : IpPc)
            : "localhost";
        return $"http://{host}:{Puerto}/{Ruta}";
    }

    internal static string Resolver(string? valorVariable) =>
        Normalizar(string.IsNullOrWhiteSpace(valorVariable) ? UrlPorDefecto() : valorVariable);

    internal static string Normalizar(string url) => url.Trim().TrimEnd('/');
}
