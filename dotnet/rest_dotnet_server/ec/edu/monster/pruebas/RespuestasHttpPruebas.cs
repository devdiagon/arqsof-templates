using System.Globalization;
using ec.edu.monster.api;
using ec.edu.monster.dto;
using ec.edu.monster.modelo;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>
/// EJEMPLO: forma exacta del JSON de cada respuesta (R4, R5, R9), sin servidor. Conviene una prueba así por cada
/// tipo de contrato nuevo: fija el orden de los campos, los nombres y que los nulos se omiten.
/// </summary>
public class RespuestasHttpPruebas : IDisposable
{
    private readonly CultureInfo _culturaOriginal = CultureInfo.CurrentCulture;

    public RespuestasHttpPruebas()
    {
        // Configuración regional con coma decimal: la salida no debe depender de ella.
        CultureInfo.CurrentCulture = new CultureInfo("es-EC");
    }

    public void Dispose()
    {
        CultureInfo.CurrentCulture = _culturaOriginal;
    }

    [Fact]
    public void UnElementoExitosoSaleConLaEnvolturaPrimeroYTodoComoTexto()
    {
        RespuestasHttp r = RespuestasHttp.De(
            ElementoMapeador.ARespuestaElemento(Resultado.Exito(new Elemento("0004", "Cuarto", 5m))));

        Assert.Equal(200, r.Http);
        Assert.Equal("{\"estado\":\"OK\",\"codigo\":\"200\",\"mensaje\":\"Operación exitosa\","
            + "\"codigoElemento\":\"0004\",\"nombre\":\"Cuarto\",\"valor\":\"5.00\"}", r.Json());
    }

    [Fact]
    public void UnErrorSoloLlevaLaEnvolturaYSuHttpDeCategoria()
    {
        RespuestasHttp r = RespuestasHttp.De(ElementoMapeador.ARespuestaElemento(
            Resultado.Error<Elemento>("ELE-002", "El elemento ya existe")));

        Assert.Equal(409, r.Http);
        Assert.Equal("{\"estado\":\"ERROR\",\"codigo\":\"ELE-002\",\"mensaje\":\"El elemento ya existe\"}", r.Json());
    }

    [Fact]
    public void UnErrorDeUnaConsultaConListaLlevaLaListaVaciaNuncaAusente()
    {
        RespuestasHttp r = RespuestasHttp.De(ElementoMapeador.ARespuestaElementos(
            Resultado.Error<List<Elemento>>("ERR-500", "Error interno del servidor")));

        Assert.Equal(500, r.Http);
        Assert.Equal("{\"estado\":\"ERROR\",\"codigo\":\"ERR-500\",\"mensaje\":\"Error interno del servidor\","
            + "\"elementos\":[]}", r.Json());
    }

    [Fact]
    public void LaListaSaleElementoAElementoEnOrden()
    {
        var datos = new List<Elemento> { new("0001", "Primero", 10.5m), new("0002", "Segundo", 20m) };

        string json = RespuestasHttp.De(ElementoMapeador.ARespuestaElementos(Resultado.Exito(datos))).Json();

        Assert.Contains("\"elementos\":[{\"codigoElemento\":\"0001\",\"nombre\":\"Primero\",\"valor\":\"10.50\"},"
            + "{\"codigoElemento\":\"0002\",\"nombre\":\"Segundo\",\"valor\":\"20.00\"}]", json);
    }
}
