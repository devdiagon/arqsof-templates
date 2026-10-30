using System.Text;
using ec.edu.monster.api;
using ec.edu.monster.controlador;
using ec.edu.monster.excepciones;
using ec.edu.monster.modelo;
using ec.edu.monster.servicios;
using Microsoft.AspNetCore.Http;
using Moq;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>
/// EJEMPLO: el recurso recibe, delega y devuelve. Se prueba con un controlador real sobre un servicio simulado y
/// un <c>HttpRequest</c> en memoria: sin servidor, sin red y sin base de datos. Las rutas y los verbos de
/// <c>Rutas</c> se verifican a mano contra docs/ENDPOINTS_REST.md.
/// </summary>
public class ElementoRecursoPruebas
{
    private readonly Mock<ElementoServicio> _servicio = new(null!, null!);
    private readonly ElementoRecurso _recurso;

    public ElementoRecursoPruebas()
    {
        _recurso = new ElementoRecurso(new ElementoControlador(_servicio.Object));
    }

    private static HttpRequest Peticion(string? contentType, string cuerpo)
    {
        var contexto = new DefaultHttpContext();
        contexto.Request.ContentType = contentType;
        contexto.Request.Body = new MemoryStream(Encoding.UTF8.GetBytes(cuerpo));
        return contexto.Request;
    }

    private static RespuestasHttp Http(IResult resultado) => Assert.IsType<RespuestasHttp>(resultado);

    [Fact]
    public void ConsultarDevuelve200ConElElemento()
    {
        _servicio.Setup(s => s.Consultar("0001")).Returns(new Elemento("0001", "Primero", 10.5m));

        RespuestasHttp r = Http(_recurso.ConsultarElemento("0001"));

        Assert.Equal(200, r.Http);
        Assert.Contains("\"valor\":\"10.50\"", r.Json());
    }

    [Fact]
    public void ConsultarUnCodigoDeFormaInvalidaDa400SinLlegarAlServicio()
    {
        RespuestasHttp r = Http(_recurso.ConsultarElemento("12"));

        Assert.Equal(400, r.Http);
        Assert.Contains("\"codigo\":\"DATA-001\"", r.Json());
        _servicio.Verify(s => s.Consultar(It.IsAny<string>()), Times.Never);
    }

    [Fact]
    public void ConsultarUnElementoInexistenteDa404()
    {
        _servicio.Setup(s => s.Consultar("0009")).Throws(new ExcepcionMonster(CodigoError.ELE_001));

        Assert.Equal(404, Http(_recurso.ConsultarElemento("0009")).Http);
    }

    [Fact]
    public async Task RegistrarLeeElCuerpoJsonYDevuelve200()
    {
        _servicio.Setup(s => s.Registrar("0004", "Cuarto", 5m)).Returns(new Elemento("0004", "Cuarto", 5m));

        RespuestasHttp r = Http(await _recurso.RegistrarElemento(Peticion("application/json; charset=utf-8",
            "{\"codigoElemento\":\"0004\",\"nombre\":\"Cuarto\",\"valor\":\"5.00\"}")));

        Assert.Equal(200, r.Http);
        Assert.Contains("\"codigoElemento\":\"0004\"", r.Json());
    }

    [Fact]
    public async Task RegistrarAceptaUnNumeroJsonComoValor()
    {
        _servicio.Setup(s => s.Registrar("0004", "Cuarto", 5.5m)).Returns(new Elemento("0004", "Cuarto", 5.5m));

        RespuestasHttp r = Http(await _recurso.RegistrarElemento(Peticion("application/json",
            "{\"codigoElemento\":\"0004\",\"nombre\":\"Cuarto\",\"valor\":5.5}")));

        Assert.Equal(200, r.Http);
    }

    [Fact]
    public async Task RegistrarConUnElementoQueYaExisteDa409()
    {
        _servicio.Setup(s => s.Registrar(It.IsAny<string>(), It.IsAny<string>(), It.IsAny<decimal>()))
            .Throws(new ExcepcionMonster(CodigoError.ELE_002));

        RespuestasHttp r = Http(await _recurso.RegistrarElemento(Peticion("application/json",
            "{\"codigoElemento\":\"0001\",\"nombre\":\"Repetido\",\"valor\":\"1\"}")));

        Assert.Equal(409, r.Http);
    }

    [Fact]
    public async Task RegistrarSinContentTypeJsonLanzaErr415()
    {
        ExcepcionMonster e = await Assert.ThrowsAsync<ExcepcionMonster>(() =>
            _recurso.RegistrarElemento(Peticion("text/plain", "{}")));

        Assert.Equal("ERR-415", e.Codigo);
    }

    [Fact]
    public async Task RegistrarConUnCuerpoMalFormadoLanzaData001()
    {
        ExcepcionMonster e = await Assert.ThrowsAsync<ExcepcionMonster>(() =>
            _recurso.RegistrarElemento(Peticion("application/json", "{no es json")));

        Assert.Equal("DATA-001", e.Codigo);
        Assert.Equal("Cuerpo de la petición inválido", e.Message);
    }

    [Fact]
    public void ListarDevuelve200ConLaLista()
    {
        _servicio.Setup(s => s.Listar()).Returns([new Elemento("0001", "Primero", 10.5m)]);

        RespuestasHttp r = Http(_recurso.ListarElementos());

        Assert.Equal(200, r.Http);
        Assert.Contains("\"elementos\":[{", r.Json());
    }
}
