using System.Text.RegularExpressions;
using ec.edu.monster.excepciones;
using Xunit;

namespace ec.edu.monster.pruebas;

public class CodigoErrorPruebas
{
    [Fact]
    public void CadaEntradaTieneCodigoMensajeYCategoria()
    {
        foreach (CodigoError error in CodigoError.Todos)
        {
            Assert.True(Regex.IsMatch(error.Codigo, @"^[A-Z]{2,4}-\d{3}$"),
                "Código con formato inválido: " + error.Codigo);
            Assert.False(string.IsNullOrWhiteSpace(error.Mensaje), "Sin mensaje: " + error);
            Assert.True(Enum.IsDefined(error.Categoria), "Sin categoría: " + error);
        }
    }

    [Fact]
    public void LosCodigosNoSeRepiten()
    {
        var codigos = new HashSet<string>();
        foreach (CodigoError error in CodigoError.Todos)
        {
            Assert.True(codigos.Add(error.Codigo), "Código repetido: " + error.Codigo);
        }
    }

    [Fact]
    public void Err500EsTecnicoConMensajeGenerico()
    {
        Assert.Equal("ERR-500", CodigoError.ERR_500.Codigo);
        Assert.Equal("Error interno del servidor", CodigoError.ERR_500.Mensaje);
        Assert.Equal(CodigoError.CategoriaError.Tecnico, CodigoError.ERR_500.Categoria);
    }

    [Fact]
    public void Data001EsDeValidacion()
    {
        Assert.Equal("DATA-001", CodigoError.DATA_001.Codigo);
        Assert.Equal(CodigoError.CategoriaError.Validacion, CodigoError.DATA_001.Categoria);
    }

    [Fact]
    public void ElEjemploDeElementoUsaNoEncontradoYConflicto()
    {
        Assert.Equal(CodigoError.CategoriaError.NoEncontrado, CodigoError.ELE_001.Categoria);
        Assert.Equal(CodigoError.CategoriaError.Conflicto, CodigoError.ELE_002.Categoria);
    }
}
