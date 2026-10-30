using ec.edu.monster.api;
using ec.edu.monster.dto;
using ec.edu.monster.excepciones;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>
/// Armado de peticiones y lectura de respuestas, sin red. Usa un DTO de prueba (un campo propio y una lista de solo
/// lectura): el mapeo JSON ↔ DTO no anota los DTO (ARQUITECTURA_CLIENTE.md 4.5).
/// </summary>
public class TraductorApiPruebas
{
    private sealed class RespuestaPruebaDto : RespuestaBaseDto
    {
        public string? CodigoItem { get; set; }
        public List<string> Items { get; } = [];
    }

    private const string Ok = "{\"estado\":\"OK\",\"codigo\":\"200\",\"mensaje\":\"Operación exitosa\"";

    // ------------------------------------------------------------------ armado

    [Theory]
    [InlineData("0001", "0001")]
    [InlineData("a b", "a%20b")]
    [InlineData("a/b", "a%2Fb")]
    [InlineData("ñ", "%C3%B1")]
    public void LasVariablesDeRutaSeCodifican(string segmento, string esperado)
    {
        Assert.Equal(esperado, TraductorApi.Codificar(segmento));
    }

    [Fact]
    public void ElCuerpoEsUnObjetoJsonDeTexto()
    {
        Assert.Equal("{\"codigoItem\":\"0002\",\"valor\":\"200.00\"}",
            TraductorApi.Cuerpo("codigoItem", "0002", "valor", "200.00"));
    }

    [Fact]
    public void ElCuerpoEscapaLasComillasYConservaLasTildes()
    {
        Assert.Equal("{\"texto\":\"a\\\"b é\"}", TraductorApi.Cuerpo("texto", "a\"b é"));
    }

    // ------------------------------------------------------------------ lectura

    [Fact]
    public void UnaRespuestaExitosaSeLeeConSusNombresCamelCase()
    {
        var dto = TraductorApi.Leer<RespuestaPruebaDto>(new RespuestaHttp(200, Ok + ",\"codigoItem\":\"0001\"}"));

        Assert.True(dto.EsOk);
        Assert.Equal("200", dto.Codigo);
        Assert.Equal("Operación exitosa", dto.Mensaje);
        Assert.Equal("0001", dto.CodigoItem);
    }

    [Fact]
    public void LosNombresSeLeenSinDistinguirMayusculas()
    {
        var dto = TraductorApi.Leer<RespuestaPruebaDto>(new RespuestaHttp(200,
            "{\"Estado\":\"OK\",\"CODIGO\":\"200\",\"mensaje\":\"m\",\"CodigoItem\":\"0001\"}"));

        Assert.True(dto.EsOk);
        Assert.Equal("0001", dto.CodigoItem);
    }

    [Fact]
    public void LosCamposDesconocidosSeIgnoran()
    {
        var dto = TraductorApi.Leer<RespuestaPruebaDto>(new RespuestaHttp(200, Ok + ",\"campoNuevo\":\"x\"}"));

        Assert.True(dto.EsOk);
    }

    [Fact]
    public void UnaListaDeSoloLecturaSeRellenaGraciasAPopulate()
    {
        // Sin Populate, System.Text.Json deja vacía una colección sin setter.
        var dto = TraductorApi.Leer<RespuestaPruebaDto>(new RespuestaHttp(200, Ok + ",\"items\":[\"a\",\"b\"]}"));

        Assert.Equal(["a", "b"], dto.Items);
    }

    [Fact]
    public void UnaListaVaciaOAusenteQuedaVacia()
    {
        var vacia = TraductorApi.Leer<RespuestaPruebaDto>(new RespuestaHttp(404,
            "{\"estado\":\"ERROR\",\"codigo\":\"X-001\",\"mensaje\":\"m\",\"items\":[]}"));
        var ausente = TraductorApi.Leer<RespuestaPruebaDto>(new RespuestaHttp(404,
            "{\"estado\":\"ERROR\",\"codigo\":\"X-001\",\"mensaje\":\"m\"}"));

        Assert.Empty(vacia.Items);
        Assert.Empty(ausente.Items);
    }

    [Theory]
    [InlineData(400, "DATA-001", "El campo valor es obligatorio")]
    [InlineData(401, "AUTH-001", "Usuario o clave incorrectos")]
    [InlineData(404, "ELE-001", "El elemento no existe")]
    [InlineData(409, "ELE-002", "El elemento ya existe")]
    [InlineData(422, "XXX-004", "Operación inválida")]
    [InlineData(500, "ERR-500", "Error interno del servidor")]
    [InlineData(404, "ERR-404", "Ruta no encontrada")]
    [InlineData(405, "ERR-405", "Método no permitido")]
    [InlineData(415, "ERR-415", "Tipo de contenido no soportado")]
    public void LaEnvolturaSeLeeTambienConUnHttpDeError(int http, string codigo, string mensaje)
    {
        string json = "{\"estado\":\"ERROR\",\"codigo\":\"" + codigo + "\",\"mensaje\":\"" + mensaje + "\"}";

        var dto = TraductorApi.Leer<RespuestaPruebaDto>(new RespuestaHttp(http, json));

        Assert.False(dto.EsOk);
        Assert.Equal(codigo, dto.Codigo);
        Assert.Equal(mensaje, dto.Mensaje);
    }

    [Theory]
    [InlineData(null)]
    [InlineData("")]
    [InlineData("   ")]
    [InlineData("<html><body>502 Bad Gateway</body></html>")]
    [InlineData("{no es json")]
    [InlineData("[1, 2]")]
    [InlineData("null")]
    [InlineData("{\"codigo\":\"200\",\"mensaje\":\"sin estado\"}")]
    public void UnCuerpoIlegibleDaSrv000ConElCodigoHttp(string? cuerpo)
    {
        ExcepcionServicio e = Assert.Throws<ExcepcionServicio>(() =>
            TraductorApi.Leer<RespuestaPruebaDto>(new RespuestaHttp(502, cuerpo)));

        Assert.Equal("SRV-000", e.Codigo);
        Assert.True(e.RespuestaIlegible);
        Assert.Equal("No se pudo interpretar la respuesta del servidor (HTTP 502)", e.Message);
    }

    // ------------------------------------------------------------------ red

    [Fact]
    public async Task UnaLlamadaCorrectaDevuelveElDtoYPasaLosDatosAlTransporte()
    {
        var transporte = new TransporteFalso(200, Ok + ",\"codigoItem\":\"0001\"}");

        var dto = await TraductorApi.LlamarAsync<RespuestaPruebaDto>(transporte, "POST", "items",
            "{\"valor\":\"10\"}", "Esquema credencial");

        Assert.Equal("0001", dto.CodigoItem);
        Assert.Equal("POST", transporte.Metodo);
        Assert.Equal("items", transporte.Ruta);
        Assert.Equal("{\"valor\":\"10\"}", transporte.CuerpoJson);
        Assert.Equal("Esquema credencial", transporte.Autorizacion);
    }

    public static TheoryData<Exception> FallosDeRed => new()
    {
        new HttpRequestException("sin conexión"),
        new TaskCanceledException("tiempo agotado"),
        new TimeoutException("tiempo agotado")
    };

    [Theory]
    [MemberData(nameof(FallosDeRed))]
    public async Task SinRespuestaDelServidorDaRed(Exception fallo)
    {
        var transporte = new TransporteFalso(fallo);

        ExcepcionServicio e = await Assert.ThrowsAsync<ExcepcionServicio>(() =>
            TraductorApi.LlamarAsync<RespuestaPruebaDto>(transporte, "GET", "x", null, null));

        Assert.Equal("RED", e.Codigo);
        Assert.True(e.ServidorNoDisponible);
        Assert.Equal("Servidor no disponible, intente más tarde", e.Message);
        Assert.Same(fallo, e.InnerException);
    }

    [Fact]
    public async Task UnaExcepcionDeProgramacionNoSeDisfrazaDeRed()
    {
        var transporte = new TransporteFalso(new InvalidOperationException("defecto"));

        await Assert.ThrowsAsync<InvalidOperationException>(() =>
            TraductorApi.LlamarAsync<RespuestaPruebaDto>(transporte, "GET", "x", null, null));
    }
}
