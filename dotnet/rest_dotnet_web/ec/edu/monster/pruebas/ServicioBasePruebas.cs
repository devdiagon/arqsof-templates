using System.Text.Json.Serialization;
using ec.edu.monster.api;
using ec.edu.monster.dto;
using ec.edu.monster.excepciones;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

public class ServicioBasePruebas
{
    private class RespuestaEjemploDto : RespuestaBaseDto
    {
        [JsonPropertyName("dato")]
        public string? Dato { get; set; }
    }

    // Servicio mínimo que expone los miembros protegidos de ServicioBase.
    private class ServicioEjemplo : ServicioBase
    {
        public ServicioEjemplo(IClienteApi cliente, Sesion sesion) : base(cliente, sesion) { }
        public string Token() => ExigirToken();

        public Task<RespuestaEjemploDto> Consultar() =>
            EnviarAsync<RespuestaEjemploDto>(HttpMethod.Get, "recurso");

        public Task<RespuestaEjemploDto> Publico() =>
            EnviarAsync<RespuestaEjemploDto>(HttpMethod.Post, "publico", new { x = 1 }, requiereToken: false);
    }

    private readonly ClienteApiFalso _cliente = new();
    private readonly Sesion _sesion = new() { Token = "ABC" };
    private readonly ServicioEjemplo _servicio;

    public ServicioBasePruebas()
    {
        _servicio = new ServicioEjemplo(_cliente, _sesion);
    }

    [Fact]
    public void Sin_token_se_exige_login_sin_llamar_al_servidor()
    {
        _sesion.Token = null;

        var ex = Assert.Throws<ExcepcionServicio>(_servicio.Token);

        Assert.Equal("AUTH-002", ex.Codigo);
        Assert.True(ex.ExigeLogin);
        Assert.Empty(_cliente.Llamadas);
    }

    [Fact]
    public async Task Operacion_protegida_sin_sesion_no_llama_a_la_api()
    {
        _sesion.Token = null;

        await Assert.ThrowsAsync<ExcepcionServicio>(_servicio.Consultar);

        Assert.Empty(_cliente.Llamadas);
    }

    [Fact]
    public async Task Operacion_protegida_envia_el_token_de_la_sesion()
    {
        _cliente.Siempre(ClienteApiFalso.Ok(new RespuestaEjemploDto { Dato = "x" }));

        var respuesta = await _servicio.Consultar();

        Assert.Equal("x", respuesta.Dato);
        Assert.Equal("ABC", Assert.Single(_cliente.Llamadas).Token);
    }

    [Fact]
    public async Task Operacion_publica_no_exige_sesion_ni_envia_token()
    {
        _sesion.Token = null;

        await _servicio.Publico();

        var llamada = Assert.Single(_cliente.Llamadas);
        Assert.Null(llamada.Token);
        Assert.Equal(HttpMethod.Post, llamada.Metodo);
    }

    [Theory]
    [InlineData(400, "DATA-001", "El valor debe ser numérico")]
    [InlineData(404, "CAT-001", "Categoría no soportada")]
    [InlineData(405, "ERR-405", "Método no permitido")]
    [InlineData(500, "ERR-500", "Error interno")]
    public async Task Error_con_JSON_lanza_con_codigo_mensaje_y_estado_del_servidor_y_conserva_la_sesion(
        int http, string codigo, string mensaje)
    {
        _cliente.Siempre(ClienteApiFalso.Error(http, codigo, mensaje));

        var ex = await Assert.ThrowsAsync<ExcepcionServicio>(_servicio.Consultar);

        Assert.Equal(codigo, ex.Codigo);
        Assert.Equal(mensaje, ex.Message);
        Assert.Equal(http, ex.EstadoHttp);
        Assert.False(ex.ServidorNoDisponible);
        Assert.True(_servicio.HaySesion);
    }

    [Theory]
    [InlineData("AUTH-002")]
    [InlineData("AUTH-003")]
    public async Task Token_invalido_o_vencido_descarta_la_sesion(string codigo)
    {
        _cliente.Siempre(ClienteApiFalso.Error(401, codigo, "Sesión expirada"));

        var ex = await Assert.ThrowsAsync<ExcepcionServicio>(_servicio.Consultar);

        Assert.True(ex.ExigeLogin);
        Assert.False(_servicio.HaySesion);
    }

    [Fact]
    public async Task Un_401_de_credenciales_en_operacion_publica_no_exige_login()
    {
        _sesion.Token = null;
        _cliente.Siempre(ClienteApiFalso.Error(401, "AUTH-001", "Usuario o contraseña incorrectos"));

        var ex = await Assert.ThrowsAsync<ExcepcionServicio>(_servicio.Publico);

        Assert.False(ex.ExigeLogin);
        Assert.Equal("Usuario o contraseña incorrectos", ex.Message);
    }

    [Fact]
    public async Task Un_401_sin_JSON_en_operacion_protegida_exige_login()
    {
        _cliente.Siempre(new RespuestaHttp(401, "Unauthorized"));

        var ex = await Assert.ThrowsAsync<ExcepcionServicio>(_servicio.Consultar);

        Assert.True(ex.ExigeLogin);
        Assert.False(_servicio.HaySesion);
    }

    [Theory]
    [InlineData(502, "<html>Bad Gateway</html>")]
    [InlineData(500, "")]
    public async Task Error_sin_JSON_da_mensaje_generico_con_el_codigo_http_y_sin_JsonException(int http, string cuerpo)
    {
        _cliente.Siempre(new RespuestaHttp(http, cuerpo));

        var ex = await Assert.ThrowsAsync<ExcepcionServicio>(_servicio.Consultar);

        Assert.Contains(http.ToString(), ex.Message);
        Assert.Equal(http, ex.EstadoHttp);
        Assert.True(_servicio.HaySesion);
    }

    [Theory]
    [InlineData("no es json")]
    [InlineData("")]
    public async Task Un_200_con_cuerpo_ilegible_es_error_interno(string cuerpo)
    {
        _cliente.Siempre(new RespuestaHttp(200, cuerpo));

        var ex = await Assert.ThrowsAsync<ExcepcionServicio>(_servicio.Consultar);

        Assert.Equal("ERR-500", ex.Codigo);
    }

    [Fact]
    public async Task Campos_ausentes_o_null_en_un_200_llegan_como_null()
    {
        _cliente.Siempre(new RespuestaHttp(200, """{"estado":"OK","codigo":"200","mensaje":"m","dato":null}"""));

        var respuesta = await _servicio.Consultar();

        Assert.Null(respuesta.Dato);
    }

    [Fact]
    public async Task Servidor_no_disponible_se_propaga_y_conserva_la_sesion()
    {
        _cliente.Responder = _ => throw new ExcepcionServicio(
            ClienteApi.MensajeServidorNoDisponible, ExcepcionServicio.CodigoRed, servidorNoDisponible: true);

        var ex = await Assert.ThrowsAsync<ExcepcionServicio>(_servicio.Consultar);

        Assert.True(ex.ServidorNoDisponible);
        Assert.True(_servicio.HaySesion);
    }

    [Theory]
    [InlineData("1,5", "1.5")]
    [InlineData(" 2.25 ", "2.25")]
    [InlineData("-3,0", "-3.0")]
    public void El_valor_decimal_se_envia_con_punto(string escrito, string enviado)
    {
        Assert.Equal(enviado, ServicioBase.NormalizarValor(escrito));
    }

    [Fact]
    public void Cerrar_sesion_descarta_el_token()
    {
        _servicio.CerrarSesion();

        Assert.False(_servicio.HaySesion);
    }
}
