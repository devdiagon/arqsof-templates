using ec.edu.monster.controlador;
using ec.edu.monster.servicio;
using System.Text.Encodings.Web;
using System.Text.Unicode;
using Microsoft.AspNetCore.Mvc.Razor;
using Microsoft.Extensions.WebEncoders;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllersWithViews(opciones =>
{
    opciones.Conventions.Add(new ConvencionControlador());
    opciones.Filters.Add<PersistirSesionFiltro>();
    // Todo POST exige el token anti-falsificación (los formularios lo incluyen con <ui-formulario>)
    opciones.Filters.Add(new AutoValidateAntiforgeryTokenAttribute());
});

// Tildes y «ñ» se emiten tal cual (UTF-8) en lugar de entidades HTML; <, >, & y las comillas siguen escapándose
builder.Services.Configure<WebEncoderOptions>(opciones =>
    opciones.TextEncoderSettings = new TextEncoderSettings(UnicodeRanges.BasicLatin, UnicodeRanges.Latin1Supplement));

// Las vistas viven junto al resto del código (directorios en minúscula), no en Views/
builder.Services.Configure<RazorViewEngineOptions>(opciones =>
{
    opciones.ViewLocationFormats.Clear();
    opciones.ViewLocationFormats.Add("/ec/edu/monster/vista/{1}/{0}.cshtml");
    opciones.ViewLocationFormats.Add("/ec/edu/monster/vista/compartido/{0}.cshtml");
});

// La Sesion (token) vive en la sesión de ASP.NET Core de cada usuario, nunca en un singleton
builder.Services.AddDistributedMemoryCache();
builder.Services.AddSession(opciones =>
{
    opciones.Cookie.Name = ".Cliente.Sesion";
    opciones.Cookie.HttpOnly = true;
    opciones.Cookie.IsEssential = true;
    opciones.IdleTimeout = TimeSpan.FromMinutes(20);
});
builder.Services.AddHttpContextAccessor();
builder.Services.AddScoped(SesionWeb.Crear);

// DOMINIO: composición. Sustituir AutenticadorPendiente por el servicio del dominio (README.md §4, paso 6):
//   builder.Services.AddSingleton<IClienteXxx>(_ => new ClienteWSXxx());    // sin estado: un canal por llamada
//   builder.Services.AddScoped<ServicioXxx>();                              // usa la Sesion del usuario
//   builder.Services.AddScoped<IAutenticador>(sp => sp.GetRequiredService<ServicioXxx>());
builder.Services.AddScoped<IAutenticador, AutenticadorPendiente>();

var app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Inicio/Error");
}
app.UseRouting();

app.UseSession();
app.UseAuthorization();

app.MapStaticAssets();

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Inicio}/{action=Index}/{id?}")
    .WithStaticAssets();

app.Run();

/// <summary>Punto de entrada visible para las pruebas de integración (WebApplicationFactory).</summary>
public partial class Program;
