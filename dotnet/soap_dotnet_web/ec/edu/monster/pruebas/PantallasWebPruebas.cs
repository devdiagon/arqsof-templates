using System.Net;
using System.Text.RegularExpressions;
using ec.edu.monster.excepciones;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection.Extensions;

namespace ec.edu.monster.pruebas;

/// <summary>
/// Recorrido real de la aplicación en memoria (rutas, convención de controladores, vistas Razor, tag helpers,
/// sesión, token anti-falsificación y cookies), sin servidor SOAP: el acceso usa un autenticador falso.
/// Es el equivalente web de <c>VistasCargaPruebas</c>: una vista inválida compila pero falla aquí.
/// </summary>
public class PantallasWebPruebas : IClassFixture<WebApplicationFactory<Program>>
{
    private const string Usuario = "usuario-de-prueba";
    private const string Contrasenia = "clave-de-prueba";

    /// <summary>Acepta solo las credenciales de prueba y guarda el token en la <see cref="Sesion"/> de la petición.</summary>
    private sealed class AutenticadorDePrueba(Sesion sesion) : IAutenticador
    {
        public Task IniciarSesionAsync(string usuario, string contrasenia)
        {
            if (usuario != Usuario || contrasenia != Contrasenia)
                throw new ExcepcionServicio("Usuario o contraseña incorrectos", "AUTH-001");
            sesion.Token = "TOKEN-DE-PRUEBA";
            return Task.CompletedTask;
        }
    }

    private readonly WebApplicationFactory<Program> _fabrica;

    public PantallasWebPruebas(WebApplicationFactory<Program> fabrica)
    {
        _fabrica = fabrica.WithWebHostBuilder(constructor =>
        {
            // El proyecto está en la raíz de la solución: la fábrica buscaría una subcarpeta con su nombre que no existe
            constructor.UseContentRoot(RaizDelProyecto());
            constructor.ConfigureServices(servicios =>
            {
                servicios.RemoveAll<IAutenticador>();
                servicios.AddScoped<IAutenticador>(sp => new AutenticadorDePrueba(sp.GetRequiredService<Sesion>()));
            });
        });
    }

    /// <summary>Sube desde <c>bin/…</c> hasta la carpeta del <c>.csproj</c>.</summary>
    private static string RaizDelProyecto()
    {
        var carpeta = new DirectoryInfo(AppContext.BaseDirectory);
        while (carpeta is not null && carpeta.GetFiles("*.csproj").Length == 0)
            carpeta = carpeta.Parent;
        return carpeta?.FullName ?? throw new InvalidOperationException("No se encontró la carpeta del proyecto");
    }

    private HttpClient CrearCliente() =>
        _fabrica.CreateClient(new WebApplicationFactoryClientOptions { AllowAutoRedirect = false, HandleCookies = true });

    private static async Task<string> ExtraerTokenAsync(HttpClient cliente)
    {
        var html = await cliente.GetStringAsync("/Acceso/Acceder");
        var coincidencia = Regex.Match(html, "name=\"__RequestVerificationToken\"[^>]*value=\"([^\"]+)\"");
        Assert.True(coincidencia.Success, "El formulario debe incluir el token anti-falsificación");
        return coincidencia.Groups[1].Value;
    }

    private static Task<HttpResponseMessage> EnviarAccesoAsync(HttpClient cliente, string token, string usuario, string contrasenia) =>
        cliente.PostAsync("/Acceso/Acceder", new FormUrlEncodedContent(new Dictionary<string, string>
        {
            ["Usuario"] = usuario,
            ["Contrasenia"] = contrasenia,
            ["__RequestVerificationToken"] = token
        }));

    [Fact]
    public async Task Sin_sesion_la_pantalla_inicial_redirige_al_acceso()
    {
        var respuesta = await CrearCliente().GetAsync("/");

        Assert.Equal(HttpStatusCode.Redirect, respuesta.StatusCode);
        Assert.Equal("/Acceso/Acceder", respuesta.Headers.Location?.OriginalString);
    }

    [Fact]
    public async Task La_pantalla_de_acceso_carga_con_su_layout_y_textos()
    {
        var respuesta = await CrearCliente().GetAsync("/Acceso/Acceder");
        var html = await respuesta.Content.ReadAsStringAsync();

        Assert.Equal(HttpStatusCode.OK, respuesta.StatusCode);
        Assert.Contains(Textos.NombreAplicacion, html);
        Assert.Contains("Contraseña", html);
        Assert.Contains("Iniciar sesión", html);
        Assert.Contains("css/estilos.css?v=", html);   // asp-append-version: si falta el tag helper de MVC sale el atributo literal
        Assert.DoesNotContain("asp-", html);
        Assert.DoesNotContain("<ui-", html);           // todos los componentes se resolvieron
        Assert.Contains("zona-imagen", html);
        Assert.DoesNotContain("bootstrap", html, StringComparison.OrdinalIgnoreCase);
    }

    [Fact]
    public async Task Un_POST_sin_token_anti_falsificacion_se_rechaza()
    {
        var respuesta = await CrearCliente().PostAsync("/Acceso/Acceder", new FormUrlEncodedContent(
            new Dictionary<string, string> { ["Usuario"] = Usuario, ["Contrasenia"] = Contrasenia }));

        Assert.Equal(HttpStatusCode.BadRequest, respuesta.StatusCode);
    }

    [Fact]
    public async Task Credenciales_malas_muestran_el_mensaje_conservan_el_usuario_y_no_devuelven_la_contrasenia()
    {
        var cliente = CrearCliente();
        var token = await ExtraerTokenAsync(cliente);

        var respuesta = await EnviarAccesoAsync(cliente, token, Usuario, "ClaveSecreta123");
        var html = await respuesta.Content.ReadAsStringAsync();

        Assert.Equal(HttpStatusCode.OK, respuesta.StatusCode);
        Assert.Contains("Usuario o contraseña incorrectos", html);
        Assert.Contains($"value=\"{Usuario}\"", html);
        Assert.DoesNotContain("ClaveSecreta123", html);
        Assert.DoesNotContain("AUTH-001", html);
    }

    [Fact]
    public async Task Recorrido_completo_acceso_inicio_y_cierre_de_sesion_mantiene_el_token_en_la_sesion()
    {
        var cliente = CrearCliente();
        var token = await ExtraerTokenAsync(cliente);

        var acceso = await EnviarAccesoAsync(cliente, token, Usuario, Contrasenia);
        Assert.Equal(HttpStatusCode.Redirect, acceso.StatusCode);
        Assert.Equal("/", acceso.Headers.Location?.OriginalString);   // Inicio/Index es la ruta por defecto

        // La sesión persiste entre peticiones: la pantalla protegida ya abre
        var inicio = await cliente.GetAsync("/");
        var htmlInicio = await inicio.Content.ReadAsStringAsync();
        Assert.Equal(HttpStatusCode.OK, inicio.StatusCode);
        Assert.Contains("Cerrar sesión", htmlInicio);
        Assert.DoesNotContain("TOKEN-DE-PRUEBA", htmlInicio);

        var tokenSalida = Regex.Match(htmlInicio, "name=\"__RequestVerificationToken\"[^>]*value=\"([^\"]+)\"").Groups[1].Value;
        var salida = await cliente.PostAsync("/Acceso/CerrarSesion", new FormUrlEncodedContent(
            new Dictionary<string, string> { ["__RequestVerificationToken"] = tokenSalida }));
        Assert.Equal(HttpStatusCode.Redirect, salida.StatusCode);

        var tras = await cliente.GetAsync("/");
        Assert.Equal(HttpStatusCode.Redirect, tras.StatusCode);
        Assert.Equal("/Acceso/Acceder", tras.Headers.Location?.OriginalString);
    }

    [Fact]
    public async Task Dos_navegadores_tienen_sesiones_independientes()
    {
        var uno = CrearCliente();
        var otro = CrearCliente();
        await EnviarAccesoAsync(uno, await ExtraerTokenAsync(uno), Usuario, Contrasenia);

        Assert.Equal(HttpStatusCode.OK, (await uno.GetAsync("/")).StatusCode);
        Assert.Equal(HttpStatusCode.Redirect, (await otro.GetAsync("/")).StatusCode);
    }

    [Fact]
    public async Task La_cookie_de_sesion_es_HttpOnly_y_no_lleva_el_token()
    {
        var cliente = CrearCliente();
        var token = await ExtraerTokenAsync(cliente);

        var acceso = await EnviarAccesoAsync(cliente, token, Usuario, Contrasenia);

        var cookies = acceso.Headers.GetValues("Set-Cookie").ToList();
        var sesion = Assert.Single(cookies, c => c.StartsWith(".Cliente.Sesion=", StringComparison.Ordinal));
        Assert.Contains("httponly", sesion, StringComparison.OrdinalIgnoreCase);
        Assert.DoesNotContain("TOKEN-DE-PRUEBA", string.Concat(cookies));
    }
}
