using System.Text.Encodings.Web;
using System.Text.Unicode;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Razor;
using Microsoft.Extensions.WebEncoders;

namespace ec.edu.monster;

/// <summary>
/// Punto de composición: registra los clientes SOAP, los servicios y las convenciones de MVC (vistas, codificación, token
/// anti-falsificación). Es el equivalente del <c>Main</c> de la plantilla Java. <c>Program.cs</c> solo lo arranca; las pruebas
/// lo construyen con otros clientes.
/// </summary>
public static class Aplicacion
{
    /// <param name="ajustarServicios">Cambia lo registrado (las pruebas sustituyen los clientes SOAP por simulados).</param>
    public static WebApplication Construir(string[] args, Action<IServiceCollection>? ajustarServicios = null)
    {
        var builder = WebApplication.CreateBuilder(new WebApplicationOptions
        {
            Args = args,
            // Fijo, sea cual sea el proceso que arranca la aplicación (el servidor de pruebas): MVC descubre así los
            // controladores y las vistas compiladas, y los recursos estáticos buscan su manifiesto
            ApplicationName = typeof(Aplicacion).Assembly.GetName().Name
        });

        builder.Services.AddControllersWithViews(opciones =>
        {
            // Todo POST exige el token anti-falsificación (los formularios lo incluyen con <ui-formulario>)
            opciones.Filters.Add(new AutoValidateAntiforgeryTokenAttribute());
        });

        // Tildes, «ñ», «…», «→» y «·» se emiten tal cual (UTF-8) en lugar de entidades HTML; <, >, & y las comillas siguen escapándose
        builder.Services.Configure<WebEncoderOptions>(opciones =>
            opciones.TextEncoderSettings = new TextEncoderSettings(UnicodeRanges.BasicLatin, UnicodeRanges.Latin1Supplement,
                UnicodeRanges.GeneralPunctuation, UnicodeRanges.Arrows));

        // Las vistas viven junto al resto del código (directorios en minúscula), no en Views/: una carpeta plana con las
        // pantallas, el layout y los imports
        builder.Services.Configure<RazorViewEngineOptions>(opciones =>
        {
            opciones.ViewLocationFormats.Clear();
            opciones.ViewLocationFormats.Add("/ec/edu/monster/vista/{0}.cshtml");
        });

        // TODO (DOMINIO, README §4): registrar el cliente SOAP de cada servicio y los servicios que lo usan
        //         builder.Services.AddSingleton(_ => XClienteWS.Crear());     // sin estado: un canal por llamada
        //         builder.Services.AddSingleton<XServicio>();
        //       Crear un cliente no abre ninguna conexión: la aplicación arranca aunque el servidor SOAP esté apagado.
        ajustarServicios?.Invoke(builder.Services);

        WebApplication app = builder.Build();

        if (!app.Environment.IsDevelopment())
        {
            app.UseExceptionHandler("/error");
        }
        app.UseRouting();
        app.UseAuthorization();

        app.MapStaticAssets();

        // Las rutas de las pantallas se declaran con atributos en cada controlador
        app.MapControllers().WithStaticAssets();

        return app;
    }
}
