namespace ec.edu.monster.api;

/// <summary>
/// URL base completa de la API REST (R1 de docs/CONVENCION_REST.md), <b>propia del móvil</b>: host, puerto y prefijo
/// <c>api</c>, con «/» final. Las rutas de las operaciones se le añaden relativas y sin barra inicial. No conoce el dominio.
/// La URL depende de dónde se ejecuta la aplicación (Android no tiene <c>localhost</c> ni variables de entorno), por eso es el
/// único archivo de <c>api</c> que se reescribe respecto al de la consola (ARQUITECTURA_CLIENTE.md 4.9). Mantiene los miembros
/// que usa <see cref="TransporteHttp"/> (<see cref="UrlBase()"/>, <see cref="TiempoLimite"/> y <c>NormalizarUrl</c>), así que el
/// resto de <c>api</c> es idéntico al de la consola.
/// Host por defecto: el emulador de Android ve el PC como <c>10.0.2.2</c>; un teléfono físico necesita la IP del PC
/// (<see cref="IpPc"/>); cualquier otra plataforma (Windows) usa <c>localhost</c>. Solo HTTP en desarrollo.
/// </summary>
public static class ConfiguracionApi
{
    /// <summary>
    /// Puerto HTTP del servidor. CAMBIAR. Servidor .NET con Kestrel (perfil <c>http</c>): 5267; con IIS Express: 57592;
    /// Java (Payara): 8080.
    /// </summary>
    public const int Puerto = 5267;

    /// <summary>
    /// Solo para el teléfono físico: IP del PC en la red local. CAMBIAR por la IP real (<c>ipconfig</c>) al probar con un
    /// teléfono: la plantilla trae un marcador de posición. El emulador y Windows no la usan.
    /// </summary>
    public const string IpPc = "127.0.0.1";

    /// <summary>
    /// Prefijo común de la API (R1), sin barras. CAMBIAR solo si el servidor tiene un contexto antes del prefijo
    /// (por ejemplo un servidor Java: <c>Contexto/api</c>).
    /// </summary>
    public const string Ruta = "api";

    /// <summary>Variable de entorno que sobrescribe la URL base. En Android no existe: solo rige en Windows.</summary>
    public const string VariableUrlBase = "API_BASE_URL";

    /// <summary>Tiempo límite de cada petición.</summary>
    public static readonly TimeSpan TiempoLimite = TimeSpan.FromSeconds(10);

    /// <summary>URL base por defecto según la plataforma en ejecución (con «/» final).</summary>
    public static string UrlBasePorDefecto =>
        UrlPorDefecto(DeviceInfo.Platform == DevicePlatform.Android, DeviceInfo.DeviceType == DeviceType.Virtual);

    /// <summary>
    /// Variable <c>API_BASE_URL</c> si existe (Windows); si no, la URL por defecto de la plataforma. Siempre normalizada.
    /// </summary>
    public static string UrlBase() => UrlBase(UrlBasePorDefecto, Environment.GetEnvironmentVariable);

    /// <summary>
    /// Igual que la anterior, con la URL por defecto y el origen de las variables inyectados (las pruebas no tocan el
    /// entorno real ni <c>DeviceInfo</c>, que solo existe con la aplicación en ejecución).
    /// </summary>
    internal static string UrlBase(string urlPorDefecto, Func<string, string?> variable)
    {
        string? url = variable(VariableUrlBase);
        if (string.IsNullOrWhiteSpace(url))
        {
            url = urlPorDefecto;
        }
        return NormalizarUrl(url);
    }

    /// <summary>
    /// Decisión del host por plataforma, aislada para probarla sin MAUI: Android virtual → <c>10.0.2.2</c>; Android físico →
    /// <see cref="IpPc"/>; cualquier otra → <c>localhost</c>.
    /// </summary>
    internal static string UrlPorDefecto(bool esAndroid, bool esVirtual)
    {
        string host = esAndroid ? (esVirtual ? "10.0.2.2" : IpPc) : "localhost";
        return $"http://{host}:{Puerto}/{Ruta}/";
    }

    /// <summary>Sin espacios y con una sola «/» final.</summary>
    internal static string NormalizarUrl(string url) => url.Trim().TrimEnd('/') + "/";
}
