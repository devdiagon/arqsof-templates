using System.Net;
using System.Text;
using System.Text.Json;
using ec.edu.monster.api;
using ec.edu.monster.excepciones;

namespace ec.edu.monster.pruebas;

public class ClienteApiPruebas
{
    /// <summary>Handler falso: guarda la petición recibida y responde lo que se le configure.</summary>
    private class ManejadorFalso : HttpMessageHandler
    {
        public HttpRequestMessage? Peticion { get; private set; }
        public string? CuerpoEnviado { get; private set; }
        public Func<HttpResponseMessage> Respuesta { get; set; } = () => Json(HttpStatusCode.OK, "{}");

        protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage peticion, CancellationToken ct)
        {
            Peticion = peticion;
            if (peticion.Content is not null)
                CuerpoEnviado = await peticion.Content.ReadAsStringAsync(ct);
            return Respuesta();
        }

        public static HttpResponseMessage Json(HttpStatusCode codigo, string cuerpo) =>
            new(codigo) { Content = new StringContent(cuerpo, Encoding.UTF8, "application/json") };
    }

    private readonly ManejadorFalso _manejador = new();
    private readonly HttpClient _http;
    private readonly ClienteApi _cliente;

    public ClienteApiPruebas()
    {
        _http = new HttpClient(_manejador) { BaseAddress = new Uri("http://localhost:57592/api/") };
        _cliente = new ClienteApi(_http);
    }

    [Fact]
    public async Task La_ruta_relativa_se_concatena_con_la_url_base()
    {
        await _cliente.EnviarAsync(HttpMethod.Get, "recurso/uno", null, "T");

        Assert.Equal("http://localhost:57592/api/recurso/uno", _manejador.Peticion!.RequestUri!.ToString());
    }

    [Fact]
    public async Task El_token_va_como_Bearer_en_la_peticion_y_no_en_las_cabeceras_por_defecto()
    {
        await _cliente.EnviarAsync(HttpMethod.Get, "recurso", null, "ABC123");

        var autorizacion = _manejador.Peticion!.Headers.Authorization!;
        Assert.Equal("Bearer", autorizacion.Scheme);
        Assert.Equal("ABC123", autorizacion.Parameter);
        Assert.Null(_http.DefaultRequestHeaders.Authorization);
    }

    [Fact]
    public async Task Sin_token_no_se_envia_Authorization()
    {
        await _cliente.EnviarAsync(HttpMethod.Post, "auth/login", new { usuario = "u" }, null);

        Assert.Null(_manejador.Peticion!.Headers.Authorization);
    }

    [Fact]
    public async Task El_cuerpo_se_envia_como_JSON_UTF8_con_los_nombres_tal_cual()
    {
        await _cliente.EnviarAsync(HttpMethod.Post, "recurso", new { nombre = "canción", valor = "1.5" }, "T");

        Assert.Equal("application/json", _manejador.Peticion!.Content!.Headers.ContentType!.MediaType);
        Assert.Equal("utf-8", _manejador.Peticion.Content.Headers.ContentType!.CharSet);
        using var json = JsonDocument.Parse(_manejador.CuerpoEnviado!);
        Assert.Equal("canción", json.RootElement.GetProperty("nombre").GetString());
        Assert.Equal("1.5", json.RootElement.GetProperty("valor").GetString());
    }

    [Fact]
    public async Task Una_consulta_GET_no_lleva_cuerpo()
    {
        await _cliente.EnviarAsync(HttpMethod.Get, "recurso", null, "T");

        Assert.Null(_manejador.Peticion!.Content);
    }

    [Theory]
    [InlineData(HttpStatusCode.OK)]
    [InlineData(HttpStatusCode.BadRequest)]
    [InlineData(HttpStatusCode.Unauthorized)]
    [InlineData(HttpStatusCode.NotFound)]
    [InlineData(HttpStatusCode.UnsupportedMediaType)]
    [InlineData(HttpStatusCode.InternalServerError)]
    public async Task Devuelve_codigo_y_cuerpo_sin_lanzar_excepcion_en_errores_HTTP(HttpStatusCode codigo)
    {
        const string cuerpo = """{"estado":"ERROR","codigo":"X","mensaje":"m"}""";
        _manejador.Respuesta = () => ManejadorFalso.Json(codigo, cuerpo);

        var respuesta = await _cliente.EnviarAsync(HttpMethod.Get, "recurso", null, "T");

        Assert.Equal((int)codigo, respuesta.Codigo);
        Assert.Equal(cuerpo, respuesta.Cuerpo);
    }

    [Fact]
    public async Task Un_cuerpo_que_no_es_JSON_se_devuelve_tal_cual()
    {
        _manejador.Respuesta = () => new HttpResponseMessage(HttpStatusCode.BadGateway)
        {
            Content = new StringContent("<html>Bad Gateway</html>", Encoding.UTF8, "text/html")
        };

        var respuesta = await _cliente.EnviarAsync(HttpMethod.Get, "recurso", null, "T");

        Assert.Equal(502, respuesta.Codigo);
        Assert.Equal("<html>Bad Gateway</html>", respuesta.Cuerpo);
    }

    [Fact]
    public async Task Los_acentos_de_la_respuesta_se_leen_en_UTF8()
    {
        _manejador.Respuesta = () => ManejadorFalso.Json(HttpStatusCode.OK, """{"elementos":["canción"]}""");

        var respuesta = await _cliente.EnviarAsync(HttpMethod.Get, "recurso", null, "T");

        Assert.Contains("canción", respuesta.Cuerpo);
    }

    [Fact]
    public async Task Sin_conexion_se_traduce_a_servidor_no_disponible()
    {
        _manejador.Respuesta = () => throw new HttpRequestException("Connection refused");

        var ex = await Assert.ThrowsAsync<ExcepcionServicio>(
            () => _cliente.EnviarAsync(HttpMethod.Get, "recurso", null, "T"));

        Assert.True(ex.ServidorNoDisponible);
        Assert.Equal(ExcepcionServicio.CodigoRed, ex.Codigo);
        Assert.Null(ex.EstadoHttp);
        Assert.False(ex.ExigeLogin);
        Assert.Equal(ClienteApi.MensajeServidorNoDisponible, ex.Message);
        Assert.IsType<HttpRequestException>(ex.InnerException);
    }

    [Fact]
    public async Task El_tiempo_agotado_se_traduce_a_servidor_no_disponible()
    {
        _manejador.Respuesta = () => throw new TaskCanceledException("timeout");

        var ex = await Assert.ThrowsAsync<ExcepcionServicio>(
            () => _cliente.EnviarAsync(HttpMethod.Get, "recurso", null, "T"));

        Assert.True(ex.ServidorNoDisponible);
    }

    [Fact]
    public async Task Si_quien_llama_cancela_no_se_trata_como_servidor_caido()
    {
        using var origen = new CancellationTokenSource();
        await origen.CancelAsync();
        _manejador.Respuesta = () => throw new TaskCanceledException("cancelado");

        await Assert.ThrowsAsync<TaskCanceledException>(
            () => _cliente.EnviarAsync(HttpMethod.Get, "recurso", null, "T", origen.Token));
    }

    [Fact]
    public async Task Servidor_caido_real_se_traduce_a_ExcepcionServicio_RED()
    {
        using var http = ClienteApi.CrearHttpClient("http://localhost:1/api/");

        var ex = await Assert.ThrowsAsync<ExcepcionServicio>(
            () => new ClienteApi(http).EnviarAsync(HttpMethod.Get, "recurso", null, null));

        Assert.True(ex.ServidorNoDisponible);
    }

    [Fact]
    public void El_HttpClient_de_fabrica_trae_base_tiempo_limite_y_Accept()
    {
        using var http = ClienteApi.CrearHttpClient("http://localhost:5267/api/");

        Assert.Equal("http://localhost:5267/api/", http.BaseAddress!.ToString());
        Assert.Equal(ConfiguracionApi.TiempoLimite, http.Timeout);
        Assert.Contains(http.DefaultRequestHeaders.Accept, a => a.MediaType == "application/json");
    }
}
