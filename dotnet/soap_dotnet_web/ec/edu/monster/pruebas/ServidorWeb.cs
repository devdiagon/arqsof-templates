using Microsoft.AspNetCore.Hosting.Server;
using Microsoft.AspNetCore.Hosting.Server.Features;

namespace ec.edu.monster.pruebas;

/// <summary>
/// La aplicación completa (rutas, vistas Razor, tag helpers y recursos estáticos) arrancada dentro de la prueba, en un puerto
/// libre de la máquina: no hay servidor SOAP ni red externa. En un proyecto, <c>Aplicacion.Construir</c> recibe un segundo
/// argumento para sustituir los clientes SOAP por simulados (<c>servicios.RemoveAll&lt;IXCliente&gt;()</c> y
/// <c>AddSingleton(simulado)</c>); aquí no hay ninguno.
/// </summary>
internal sealed class ServidorWeb : IAsyncDisposable
{
    private readonly WebApplication _aplicacion;

    private ServidorWeb(WebApplication aplicacion, Uri direccion)
    {
        _aplicacion = aplicacion;
        Direccion = direccion;
    }

    public Uri Direccion { get; }

    public WebApplication Aplicacion => _aplicacion;

    public static async Task<ServidorWeb> IniciarAsync(Action<IServiceCollection>? ajustarServicios = null)
    {
        WebApplication aplicacion = ec.edu.monster.Aplicacion.Construir(
            ["--urls", "http://127.0.0.1:0", "--environment", "Development"], ajustarServicios);
        await aplicacion.StartAsync();
        string direccion = aplicacion.Services.GetRequiredService<IServer>().Features.Get<IServerAddressesFeature>()!.Addresses.First();
        return new ServidorWeb(aplicacion, new Uri(direccion));
    }

    /// <summary>Un navegador: un cliente HTTP con sus cookies, que no sigue redirecciones (se comprueban una por una).</summary>
    public HttpClient NuevoNavegador() => new(new HttpClientHandler { AllowAutoRedirect = false, UseCookies = true })
    {
        BaseAddress = Direccion
    };

    public async ValueTask DisposeAsync()
    {
        await _aplicacion.StopAsync();
        await _aplicacion.DisposeAsync();
    }
}
