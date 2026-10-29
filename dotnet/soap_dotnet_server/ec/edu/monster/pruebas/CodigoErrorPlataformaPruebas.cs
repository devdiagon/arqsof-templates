using ec.edu.monster.excepciones;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>Errores de plataforma REST dados de alta en el catálogo (CONVENCION_REST.md 2.1).</summary>
public class CodigoErrorPlataformaPruebas
{
    [Fact]
    public void Err404EsDeCategoriaNoEncontrado()
    {
        Assert.Equal("ERR-404", CodigoError.ERR_404.Codigo);
        Assert.Equal("Ruta no encontrada", CodigoError.ERR_404.Mensaje);
        Assert.Equal(CodigoError.CategoriaError.NoEncontrado, CodigoError.ERR_404.Categoria);
    }

    [Fact]
    public void Err405TieneSuPropiaCategoria()
    {
        Assert.Equal("ERR-405", CodigoError.ERR_405.Codigo);
        Assert.Equal("Método no permitido", CodigoError.ERR_405.Mensaje);
        Assert.Equal(CodigoError.CategoriaError.MetodoNoPermitido, CodigoError.ERR_405.Categoria);
    }

    [Fact]
    public void Err415TieneSuPropiaCategoria()
    {
        Assert.Equal("ERR-415", CodigoError.ERR_415.Codigo);
        Assert.Equal("Tipo de contenido no soportado", CodigoError.ERR_415.Mensaje);
        Assert.Equal(CodigoError.CategoriaError.TipoNoSoportado, CodigoError.ERR_415.Categoria);
    }
}
