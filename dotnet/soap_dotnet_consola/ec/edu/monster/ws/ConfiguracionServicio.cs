namespace ec.edu.monster.ws;

/// <summary>
/// URL del servicio SOAP. Regla R9 de CONVENCION_SOAP.md: se recibe COMPLETA y no se asume su forma
/// (la ruta de un servidor Java y la de uno .NET son distintas); cambiar de servidor es cambiar solo esta URL.
/// </summary>
public static class ConfiguracionServicio
{
    // CAMBIAR: URL completa del servicio, sin "?wsdl" ni "/" final. Se puede sobrescribir sin recompilar con la
    // variable de entorno WS_URL. Ejemplos:
    //   .NET (IIS Express, HTTP)  http://localhost:57592/RutaServicio
    //   .NET (Kestrel, perfil http) http://localhost:5267/RutaServicio
    //   Java (Payara)             http://localhost:8080/Contexto/Servicio
    public const string UrlPorDefecto = "http://localhost:57592/CAMBIAR_RUTA_SERVICIO";

    public const string VariableEntorno = "WS_URL";

    public static readonly TimeSpan TiempoLimite = TimeSpan.FromSeconds(10);

    /// <summary>Variable <c>WS_URL</c> si existe; si no, <see cref="UrlPorDefecto"/>.</summary>
    public static string ObtenerUrl() => Resolver(Environment.GetEnvironmentVariable(VariableEntorno));

    internal static string Resolver(string? valorVariable) =>
        Normalizar(string.IsNullOrWhiteSpace(valorVariable) ? UrlPorDefecto : valorVariable);

    internal static string Normalizar(string url) => url.Trim().TrimEnd('/');
}
