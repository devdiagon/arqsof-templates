using ec.edu.monster.dto;
using Xunit;

namespace ec.edu.monster.pruebas;

public class ResultadoPruebas
{
    [Fact]
    public void ElExitoLlevaEstadoOkCodigo200MensajeFijoYDatos()
    {
        Resultado<string> resultado = Resultado.Exito("datos");

        Assert.Equal(EstadoResultado.OK, resultado.Estado);
        Assert.Equal("200", resultado.Codigo);
        Assert.Equal(Resultado.MensajeExito, resultado.Mensaje);
        Assert.Equal("datos", resultado.Datos);
    }

    [Fact]
    public void ElErrorLlevaCodigoMensajeYNingunDato()
    {
        Resultado<string> resultado = Resultado.Error<string>("ELE-001", "El elemento no existe");

        Assert.Equal(EstadoResultado.ERROR, resultado.Estado);
        Assert.Equal("ELE-001", resultado.Codigo);
        Assert.Equal("El elemento no existe", resultado.Mensaje);
        Assert.Null(resultado.Datos);
    }

    [Fact]
    public void ElMensajeDeExitoEsElDelContrato()
    {
        Assert.Equal("200", Resultado.CodigoExito);
        Assert.Equal("Operación exitosa", Resultado.MensajeExito);
    }
}
