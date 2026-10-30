using System.Text;
using ec.edu.monster.api;
using ec.edu.monster.excepciones;
using Microsoft.AspNetCore.Http;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>Errores de plataforma (R6) con un HttpContext en memoria: sin servidor ni red.</summary>
public class ManejadorErroresPruebas
{
    private static async Task<(DefaultHttpContext Contexto, string Cuerpo)> Ejecutar(RequestDelegate siguiente)
    {
        var contexto = new DefaultHttpContext();
        contexto.Response.Body = new MemoryStream();
        await new ManejadorErrores(siguiente).InvokeAsync(contexto);
        contexto.Response.Body.Position = 0;
        string cuerpo = await new StreamReader(contexto.Response.Body, Encoding.UTF8).ReadToEndAsync();
        return (contexto, cuerpo);
    }

    [Fact]
    public async Task UnaRutaInexistenteDaErr404ConEnvoltura()
    {
        var (c, cuerpo) = await Ejecutar(ctx => { ctx.Response.StatusCode = 404; return Task.CompletedTask; });

        Assert.Equal(404, c.Response.StatusCode);
        Assert.StartsWith("application/json", c.Response.ContentType);
        Assert.Equal("{\"estado\":\"ERROR\",\"codigo\":\"ERR-404\",\"mensaje\":\"Ruta no encontrada\"}", cuerpo);
    }

    [Fact]
    public async Task UnVerboNoPermitidoDaErr405YConservaLaCabeceraAllow()
    {
        var (c, cuerpo) = await Ejecutar(ctx =>
        {
            ctx.Response.StatusCode = 405;
            ctx.Response.Headers.Allow = "POST";
            return Task.CompletedTask;
        });

        Assert.Equal(405, c.Response.StatusCode);
        Assert.Equal("POST", c.Response.Headers.Allow.ToString());
        Assert.Contains("\"codigo\":\"ERR-405\"", cuerpo);
    }

    [Fact]
    public async Task UnaExcepcionDeNegocioSaleConSuCodigoYElHttpDeSuCategoria()
    {
        var (c, cuerpo) = await Ejecutar(_ => throw new ExcepcionMonster(CodigoError.ERR_415));

        Assert.Equal(415, c.Response.StatusCode);
        Assert.Contains("\"codigo\":\"ERR-415\"", cuerpo);
    }

    [Fact]
    public async Task UnCuerpoInvalidoSaleComoData001Con400()
    {
        var (c, cuerpo) = await Ejecutar(_ =>
            throw new ExcepcionMonster(CodigoError.DATA_001, "Cuerpo de la petición inválido"));

        Assert.Equal(400, c.Response.StatusCode);
        Assert.Equal("{\"estado\":\"ERROR\",\"codigo\":\"DATA-001\",\"mensaje\":\"Cuerpo de la petición inválido\"}", cuerpo);
    }

    [Fact]
    public async Task UnaExcepcionNoPrevistaDaErr500SinDetallesInternos()
    {
        var (c, cuerpo) = await Ejecutar(_ => throw new InvalidOperationException("detalle secreto"));

        Assert.Equal(500, c.Response.StatusCode);
        Assert.Contains("\"codigo\":\"ERR-500\"", cuerpo);
        Assert.DoesNotContain("secreto", cuerpo);
    }

    [Fact]
    public async Task UnaRespuestaConCuerpoPropioNoSeToca()
    {
        var (c, cuerpo) = await Ejecutar(async ctx =>
        {
            ctx.Response.StatusCode = 422;
            ctx.Response.ContentType = "application/json; charset=utf-8";
            await ctx.Response.WriteAsync("{\"propio\":true}");
        });

        Assert.Equal(422, c.Response.StatusCode);
        Assert.Equal("{\"propio\":true}", cuerpo);
    }

    [Theory]
    [InlineData(404, "ERR-404")]
    [InlineData(405, "ERR-405")]
    [InlineData(415, "ERR-415")]
    [InlineData(400, "DATA-001")]
    [InlineData(500, "ERR-500")]
    public void CadaHttpDelFrameworkSeTraduceAlCatalogo(int http, string codigo)
    {
        Assert.Equal(codigo, ManejadorErrores.DeEstado(http).Codigo);
    }

    [Theory]
    [InlineData("application/json", true)]
    [InlineData("application/json; charset=utf-8", true)]
    [InlineData("APPLICATION/JSON", true)]
    [InlineData("text/plain", false)]
    [InlineData("application/xml", false)]
    [InlineData(null, false)]
    public void SoloSeAceptaApplicationJson(string? contentType, bool esperado)
    {
        Assert.Equal(esperado, CuerpoPeticion.EsJson(contentType));
    }
}
