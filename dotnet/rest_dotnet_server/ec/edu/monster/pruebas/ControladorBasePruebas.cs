using ec.edu.monster.controlador;
using ec.edu.monster.dto;
using ec.edu.monster.excepciones;
using Xunit;

namespace ec.edu.monster.pruebas;

public class ControladorBasePruebas
{
    /// <summary>Expone la operación protegida de la base para poder probarla.</summary>
    private sealed class ControladorDePrueba : ControladorBase
    {
        public Resultado<T> Correr<T>(Func<T> operacion) => Ejecutar(operacion);
    }

    private readonly ControladorDePrueba _controlador = new();

    [Fact]
    public void UnaOperacionExitosaDevuelveResultadoOkConSusDatos()
    {
        Resultado<string> resultado = _controlador.Correr(() => "listo");

        Assert.Equal(EstadoResultado.OK, resultado.Estado);
        Assert.Equal("200", resultado.Codigo);
        Assert.Equal("listo", resultado.Datos);
    }

    [Fact]
    public void UnaExcepcionDeNegocioDevuelveSuCodigoYSuMensaje()
    {
        Resultado<string> resultado = _controlador.Correr<string>(() => throw new ExcepcionMonster(CodigoError.ELE_001));

        Assert.Equal(EstadoResultado.ERROR, resultado.Estado);
        Assert.Equal("ELE-001", resultado.Codigo);
        Assert.Equal(CodigoError.ELE_001.Mensaje, resultado.Mensaje);
        Assert.Null(resultado.Datos);
    }

    [Fact]
    public void UnaExcepcionDeNegocioConMensajeEspecificoLoConserva()
    {
        Resultado<string> resultado = _controlador.Correr<string>(() =>
            throw new ExcepcionMonster(CodigoError.DATA_001, "El campo importe es obligatorio"));

        Assert.Equal("DATA-001", resultado.Codigo);
        Assert.Equal("El campo importe es obligatorio", resultado.Mensaje);
    }

    [Fact]
    public void UnaExcepcionInesperadaDevuelveErr500SinFiltrarDetallesInternos()
    {
        const string detalleInterno = "Invalid object name 'elemento' (usuario ejemplo, base ejemplo)";

        Resultado<string> resultado = _controlador.Correr<string>(() => throw new InvalidOperationException(detalleInterno));

        Assert.Equal(EstadoResultado.ERROR, resultado.Estado);
        Assert.Equal("ERR-500", resultado.Codigo);
        Assert.Equal(CodigoError.ERR_500.Mensaje, resultado.Mensaje);
        Assert.DoesNotContain("ejemplo", resultado.Mensaje);
    }

    [Fact]
    public void UnaExcepcionNoControladaDeTiempoDeEjecucionTambienSeTraduceAErr500()
    {
        Resultado<string> resultado = _controlador.Correr<string>(() => throw new NullReferenceException("detalle interno"));

        Assert.Equal("ERR-500", resultado.Codigo);
        Assert.Equal(CodigoError.ERR_500.Mensaje, resultado.Mensaje);
    }
}
