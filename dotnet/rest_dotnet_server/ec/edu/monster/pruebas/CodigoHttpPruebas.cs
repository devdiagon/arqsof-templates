using ec.edu.monster.api;
using ec.edu.monster.excepciones;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>El HTTP sale de la categoría de la entrada del catálogo (R7), nunca del código individual.</summary>
public class CodigoHttpPruebas
{
    [Theory]
    [InlineData("DATA-001", 400)]
    [InlineData("ELE-001", 404)]
    [InlineData("ELE-002", 409)]
    [InlineData("ERR-404", 404)]
    [InlineData("ERR-405", 405)]
    [InlineData("ERR-415", 415)]
    [InlineData("ERR-500", 500)]
    public void CadaCodigoDelCatalogoSaleConElHttpDeSuCategoria(string codigo, int esperado)
    {
        Assert.Equal(esperado, CodigoHttp.DeRespuesta("ERROR", codigo));
    }

    [Fact]
    public void ElCatalogoCompletoTieneUnHttpDefinido()
    {
        foreach (CodigoError error in CodigoError.Todos)
        {
            Assert.Equal(CodigoHttp.DeCategoria(error.Categoria), CodigoHttp.DeRespuesta("ERROR", error.Codigo));
        }
    }

    [Theory]
    [InlineData(CodigoError.CategoriaError.Validacion, 400)]
    [InlineData(CodigoError.CategoriaError.NoAutorizado, 401)]
    [InlineData(CodigoError.CategoriaError.NoEncontrado, 404)]
    [InlineData(CodigoError.CategoriaError.MetodoNoPermitido, 405)]
    [InlineData(CodigoError.CategoriaError.Conflicto, 409)]
    [InlineData(CodigoError.CategoriaError.TipoNoSoportado, 415)]
    [InlineData(CodigoError.CategoriaError.OperacionInvalida, 422)]
    [InlineData(CodigoError.CategoriaError.Tecnico, 500)]
    public void CadaCategoriaTieneSuHttp(CodigoError.CategoriaError categoria, int esperado)
    {
        Assert.Equal(esperado, CodigoHttp.DeCategoria(categoria));
    }

    [Fact]
    public void TodoExitoEs200()
    {
        Assert.Equal(200, CodigoHttp.DeRespuesta("OK", "200"));
    }

    [Fact]
    public void UnCodigoDesconocidoSaleComoErrorTecnico()
    {
        Assert.Equal(500, CodigoHttp.DeRespuesta("ERROR", "XXX-999"));
    }
}
