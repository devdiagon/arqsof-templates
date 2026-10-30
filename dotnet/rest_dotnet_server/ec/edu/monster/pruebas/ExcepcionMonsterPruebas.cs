using ec.edu.monster.excepciones;
using Xunit;

namespace ec.edu.monster.pruebas;

public class ExcepcionMonsterPruebas
{
    [Fact]
    public void UsaElMensajePorDefectoDelCatalogo()
    {
        var e = new ExcepcionMonster(CodigoError.ELE_001);

        Assert.Equal(CodigoError.ELE_001.Mensaje, e.Message);
    }

    [Fact]
    public void ElMensajeEspecificoReemplazaAlPorDefecto()
    {
        var e = new ExcepcionMonster(CodigoError.DATA_001, "El campo importe es obligatorio");

        Assert.Equal("El campo importe es obligatorio", e.Message);
        Assert.Equal(CodigoError.DATA_001, e.Error);
    }

    [Fact]
    public void ExponeCodigoYCategoriaDelCatalogo()
    {
        var e = new ExcepcionMonster(CodigoError.ELE_001);

        Assert.Equal("ELE-001", e.Codigo);
        Assert.Equal(CodigoError.CategoriaError.NoEncontrado, e.Categoria);
    }
}
