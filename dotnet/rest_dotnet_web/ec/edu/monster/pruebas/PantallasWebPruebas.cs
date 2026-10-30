using System.Net;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Options;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>
/// La aplicación de la plantilla de punta a punta (rutas, layout en sus dos modos, vistas Razor, tag helpers, recursos
/// estáticos y página de error). Un proyecto amplía esta clase con las pantallas del dominio. No comprueba el aspecto: eso se
/// mira en un navegador.
/// </summary>
public class PantallasWebPruebas : IAsyncLifetime
{
    private ServidorWeb _servidor = null!;

    public async Task InitializeAsync() => _servidor = await ServidorWeb.IniciarAsync();

    public async Task DisposeAsync() => await _servidor.DisposeAsync();

    [Fact]
    public async Task La_pantalla_inicial_se_muestra_en_modo_aplicacion()
    {
        HttpResponseMessage r = await _servidor.NuevoNavegador().GetAsync("/");
        string html = await r.Content.ReadAsStringAsync();

        Assert.Equal(HttpStatusCode.OK, r.StatusCode);
        Assert.Equal("text/html; charset=utf-8", r.Content.Headers.ContentType?.ToString());
        Assert.Contains("<title>Inicio · Cliente REST</title>", html);
        Assert.Contains("<h1 class=\"encabezado\">Inicio</h1>", html);
        Assert.Contains("pagina pagina--aplicacion", html);
        Assert.Contains("contenido--formulario", html);
        Assert.DoesNotContain("zona-imagen", html);
        Assert.DoesNotContain("<ui-", html);                  // ningún tag helper quedó sin resolver
        Assert.DoesNotContain("bootstrap", html, StringComparison.OrdinalIgnoreCase);
        Assert.Matches(@"<link rel=""stylesheet"" href=""/css/estilos[^""]*\.css""", html);
        Assert.Matches(@"<script src=""/js/app[^""]*\.js""", html);
    }

    [Fact]
    public async Task La_pagina_de_error_usa_la_disposicion_dividida_y_el_mensaje_generico()
    {
        string html = await _servidor.NuevoNavegador().GetStringAsync("/error");

        Assert.Contains("<title>Cliente REST · Cliente REST</title>", html);
        Assert.Contains("<h1 class=\"encabezado encabezado--aplicacion\">Cliente REST</h1>", html);
        Assert.Contains("Ocurrió un error inesperado, intente nuevamente", html);
        Assert.Contains("zona-imagen", html);                 // disposición dividida
        Assert.DoesNotContain("pagina--aplicacion", html);
        Assert.Contains("class=\"accion-principal\"", html);
        Assert.Contains("href=\"/\"", html);
        Assert.DoesNotContain("<ui-", html);
    }

    [Fact]
    public async Task Una_ruta_inexistente_responde_404()
    {
        HttpResponseMessage r = await _servidor.NuevoNavegador().GetAsync("/no-existe");

        Assert.Equal(HttpStatusCode.NotFound, r.StatusCode);
    }

    [Fact]
    public async Task Los_recursos_estaticos_se_sirven_con_su_tipo()
    {
        HttpClient navegador = _servidor.NuevoNavegador();

        HttpResponseMessage css = await navegador.GetAsync("/css/estilos.css");
        HttpResponseMessage js = await navegador.GetAsync("/js/app.js");
        HttpResponseMessage imagen = await navegador.GetAsync("/img/fondo_app.png");

        Assert.Equal(HttpStatusCode.OK, css.StatusCode);
        Assert.Equal("text/css", css.Content.Headers.ContentType?.MediaType);
        Assert.Equal(HttpStatusCode.OK, js.StatusCode);
        Assert.Equal(HttpStatusCode.OK, imagen.StatusCode);
        Assert.Equal("image/png", imagen.Content.Headers.ContentType?.MediaType);
    }

    [Fact]
    public void Todo_POST_exige_el_token_anti_falsificacion()
    {
        var opciones = _servidor.Aplicacion.Services.GetRequiredService<IOptions<MvcOptions>>().Value;

        Assert.Contains(opciones.Filters, f => f is AutoValidateAntiforgeryTokenAttribute);
    }
}
