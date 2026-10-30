using ec.edu.monster.api;
using ec.edu.monster.excepciones;
using Xunit;

namespace ec.edu.monster.pruebas;

public class CuerpoJsonPruebas
{
    [Fact]
    public void UnTextoJsonSeEntregaTalCual()
    {
        var json = CuerpoJson.Leer("{\"importe\": \"100.50\"}");

        Assert.Equal("100.50", json.Texto("importe"));
    }

    [Fact]
    public void UnNumeroJsonSeEntregaComoSuTextoEscrito()
    {
        var json = CuerpoJson.Leer("{\"importe\": 100.50, \"otro\": 7}");

        Assert.Equal("100.50", json.Texto("importe"));
        Assert.Equal("7", json.Texto("otro"));
    }

    [Fact]
    public void UnNumeroNegativoOConExponenteLlegaAlValidadorSinConvertir()
    {
        var json = CuerpoJson.Leer("{\"a\": -5, \"b\": 1e2}");

        Assert.Equal("-5", json.Texto("a"));
        Assert.Equal("1e2", json.Texto("b"));
    }

    [Fact]
    public void LosNombresNoDistinguenMayusculas()
    {
        var json = CuerpoJson.Leer("{\"CodigoElemento\": \"0002\"}");

        Assert.Equal("0002", json.Texto("codigoElemento"));
    }

    [Fact]
    public void UnCampoAusenteONuloOEstructuradoEsNulo()
    {
        var json = CuerpoJson.Leer("{\"a\": null, \"b\": {\"x\": 1}, \"c\": [1]}");

        Assert.Null(json.Texto("a"));
        Assert.Null(json.Texto("b"));
        Assert.Null(json.Texto("c"));
        Assert.Null(json.Texto("ausente"));
    }

    [Fact]
    public void LosCamposDesconocidosSeIgnoran()
    {
        var json = CuerpoJson.Leer("{\"usuario\": \"u\", \"extra\": \"x\"}");

        Assert.Equal("u", json.Texto("usuario"));
    }

    [Theory]
    [InlineData(null)]
    [InlineData("")]
    [InlineData("   ")]
    [InlineData("{no es json")]
    [InlineData("[1, 2]")]
    [InlineData("\"texto\"")]
    [InlineData("42")]
    public void UnCuerpoVacioMalFormadoOQueNoEsObjetoDaData001(string? cuerpo)
    {
        ExcepcionMonster e = Assert.Throws<ExcepcionMonster>(() => CuerpoJson.Leer(cuerpo));

        Assert.Equal("DATA-001", e.Codigo);
        Assert.Equal("Cuerpo de la petición inválido", e.Message);
    }
}
