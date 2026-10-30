using System.Net;
using System.Text;
using ec.edu.monster.api;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>
/// El transporte real contra un <c>HttpMessageHandler</c> simulado: se comprueba lo que sale por el cable (URL, cabeceras,
/// cuerpo) y lo que se devuelve, sin abrir ninguna conexión.
/// </summary>
public class TransporteHttpPruebas
{
    private sealed class ManejadorFalso : HttpMessageHandler
    {
        private readonly HttpStatusCode _estado;
        private readonly string _cuerpo;

        public ManejadorFalso(HttpStatusCode estado, string cuerpo)
        {
            _estado = estado;
            _cuerpo = cuerpo;
        }

        public HttpRequestMessage? Peticion { get; private set; }
        public string? CuerpoEnviado { get; private set; }

        protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage peticion, CancellationToken token)
        {
            Peticion = peticion;
            CuerpoEnviado = peticion.Content is null ? null : await peticion.Content.ReadAsStringAsync(token);
            return new HttpResponseMessage(_estado)
            {
                Content = new StringContent(_cuerpo, Encoding.UTF8, "application/json")
            };
        }
    }

    private static (TransporteHttp Transporte, ManejadorFalso Manejador, HttpClient Cliente) Nuevo(
        HttpStatusCode estado = HttpStatusCode.OK, string cuerpo = "{}")
    {
        var manejador = new ManejadorFalso(estado, cuerpo);
        var cliente = new HttpClient(manejador);
        return (new TransporteHttp("http://servidor:1/api", cliente), manejador, cliente);
    }

    [Fact]
    public async Task LaRutaSeAnadeALaUrlBaseConBarraFinal()
    {
        var (transporte, manejador, _) = Nuevo();

        await transporte.EnviarAsync("GET", "items/0001", null, null);

        Assert.Equal("http://servidor:1/api/items/0001", manejador.Peticion!.RequestUri!.ToString());
        Assert.Equal(HttpMethod.Get, manejador.Peticion.Method);
    }

    [Fact]
    public async Task LaRutaConSegmentosCodificadosLlegaTalCual()
    {
        var (transporte, manejador, _) = Nuevo();

        await transporte.EnviarAsync("GET", "items/a%20b", null, null);

        Assert.Equal("/api/items/a%20b", manejador.Peticion!.RequestUri!.AbsolutePath);
    }

    [Fact]
    public async Task ElCuerpoSaleComoJsonUtf8()
    {
        var (transporte, manejador, _) = Nuevo();

        await transporte.EnviarAsync("POST", "items", "{\"texto\":\"é\"}", null);

        Assert.Equal("application/json; charset=utf-8", manejador.Peticion!.Content!.Headers.ContentType!.ToString());
        Assert.Equal("{\"texto\":\"é\"}", manejador.CuerpoEnviado);
        Assert.Contains("application/json", manejador.Peticion.Headers.Accept.ToString());
    }

    [Fact]
    public async Task LaAutorizacionSePoneSoloEnLaPeticionYNuncaEnLasCabecerasPorDefecto()
    {
        var (transporte, manejador, cliente) = Nuevo();

        await transporte.EnviarAsync("GET", "x", null, "Esquema credencial");

        Assert.Equal("Esquema credencial", manejador.Peticion!.Headers.GetValues("Authorization").Single());
        Assert.False(cliente.DefaultRequestHeaders.Contains("Authorization"));
    }

    [Fact]
    public async Task SinAutorizacionNoSeEnviaLaCabecera()
    {
        var (transporte, manejador, _) = Nuevo();

        await transporte.EnviarAsync("POST", "items", "{}", null);

        Assert.False(manejador.Peticion!.Headers.Contains("Authorization"));
    }

    [Fact]
    public async Task UnGetNoLlevaCuerpo()
    {
        var (transporte, manejador, _) = Nuevo();

        await transporte.EnviarAsync("GET", "x", null, null);

        Assert.Null(manejador.Peticion!.Content);
    }

    [Theory]
    [InlineData(HttpStatusCode.Unauthorized)]
    [InlineData(HttpStatusCode.NotFound)]
    [InlineData((HttpStatusCode)422)]
    [InlineData(HttpStatusCode.InternalServerError)]
    public async Task UnHttpDeErrorDevuelveElCuerpoSinLanzarExcepcion(HttpStatusCode estado)
    {
        const string cuerpo = "{\"estado\":\"ERROR\",\"codigo\":\"X\",\"mensaje\":\"m\"}";
        var (transporte, _, _) = Nuevo(estado, cuerpo);

        RespuestaHttp respuesta = await transporte.EnviarAsync("GET", "x", null, null);

        Assert.Equal((int)estado, respuesta.Codigo);
        Assert.Equal(cuerpo, respuesta.Cuerpo);
    }
}
