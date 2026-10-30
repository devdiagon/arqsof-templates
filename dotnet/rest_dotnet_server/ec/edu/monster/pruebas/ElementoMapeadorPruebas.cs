using ec.edu.monster.api;
using ec.edu.monster.api.contrato;
using ec.edu.monster.dto;
using ec.edu.monster.modelo;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>EJEMPLO: el mapeador pasa Resultado a tipo de contrato, con los tipos reales convertidos a texto.</summary>
public class ElementoMapeadorPruebas
{
    [Fact]
    public void UnElementoExitosoSeConvierteATextoConLaEnvoltura()
    {
        RespuestaElemento respuesta = ElementoMapeador.ARespuestaElemento(
            Resultado.Exito(new Elemento("0001", "Primer elemento", 10.5m)));

        Assert.Equal("OK", respuesta.Estado);
        Assert.Equal("200", respuesta.Codigo);
        Assert.Equal(Resultado.MensajeExito, respuesta.Mensaje);
        Assert.Equal("0001", respuesta.CodigoElemento);
        Assert.Equal("Primer elemento", respuesta.Nombre);
        Assert.Equal("10.50", respuesta.Valor);
    }

    [Fact]
    public void UnErrorNoCargaDatosPeroConservaCodigoYMensaje()
    {
        RespuestaElemento respuesta = ElementoMapeador.ARespuestaElemento(
            Resultado.Error<Elemento>("ELE-001", "El elemento no existe"));

        Assert.Equal("ERROR", respuesta.Estado);
        Assert.Equal("ELE-001", respuesta.Codigo);
        Assert.Equal("El elemento no existe", respuesta.Mensaje);
        Assert.Null(respuesta.CodigoElemento);
        Assert.Null(respuesta.Valor);
    }

    [Fact]
    public void LaListaSeConvierteElementoAElemento()
    {
        var datos = new List<Elemento>
        {
            new("0001", "Primero", 10.5m),
            new("0002", "Segundo", 20m)
        };

        RespuestaElementos respuesta = ElementoMapeador.ARespuestaElementos(Resultado.Exito(datos));

        Assert.Equal("OK", respuesta.Estado);
        Assert.Equal(2, respuesta.Elementos.Count);
        Assert.Equal("0001", respuesta.Elementos[0].CodigoElemento);
        Assert.Equal("10.50", respuesta.Elementos[0].Valor);
        Assert.Equal("20.00", respuesta.Elementos[1].Valor);
    }

    [Fact]
    public void UnErrorDevuelveLaListaVaciaPeroNuncaNula()
    {
        RespuestaElementos respuesta = ElementoMapeador.ARespuestaElementos(
            Resultado.Error<List<Elemento>>("ERR-500", "Error interno del servidor"));

        Assert.Equal("ERROR", respuesta.Estado);
        Assert.NotNull(respuesta.Elementos);
        Assert.Empty(respuesta.Elementos);
    }
}
