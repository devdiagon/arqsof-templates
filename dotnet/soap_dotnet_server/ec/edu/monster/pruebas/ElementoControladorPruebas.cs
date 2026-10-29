using ec.edu.monster.controlador;
using ec.edu.monster.dto;
using ec.edu.monster.excepciones;
using ec.edu.monster.modelo;
using ec.edu.monster.servicios;
using Moq;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>
/// EJEMPLO: el controlador valida la forma, convierte a tipos reales, delega y devuelve siempre un Resultado.
/// El servicio se sustituye con Moq (sus operaciones son <c>virtual</c>).
/// </summary>
public class ElementoControladorPruebas
{
    private static readonly Elemento Primero = new("0001", "Primer elemento", 10.50m);

    private readonly Mock<ElementoServicio> _servicio = new(null!, null!);
    private readonly ElementoControlador _controlador;

    public ElementoControladorPruebas()
    {
        _controlador = new ElementoControlador(_servicio.Object);
    }

    [Fact]
    public void ConsultarConvierteLaEntradaYDevuelveElResultadoOk()
    {
        _servicio.Setup(s => s.Consultar("0001")).Returns(Primero);

        Resultado<Elemento> resultado = _controlador.Consultar(" 0001 ");

        Assert.Equal(EstadoResultado.OK, resultado.Estado);
        Assert.Equal(Primero, resultado.Datos);
    }

    [Theory]
    [InlineData("")]
    [InlineData("12")]
    [InlineData("12AB")]
    [InlineData("12345")]
    public void UnaEntradaMalFormadaDevuelveData001SinLlamarAlServicio(string codigo)
    {
        Resultado<Elemento> resultado = _controlador.Consultar(codigo);

        Assert.Equal("DATA-001", resultado.Codigo);
        _servicio.VerifyNoOtherCalls();
    }

    [Fact]
    public void RegistrarValidaElValorAntesDeLlamarAlServicio()
    {
        Resultado<Elemento> resultado = _controlador.Registrar("0004", "Cuarto", "-5");

        Assert.Equal("DATA-001", resultado.Codigo);
        Assert.Equal("El campo valor debe ser un número con punto decimal, sin signo", resultado.Mensaje);
        _servicio.VerifyNoOtherCalls();
    }

    [Fact]
    public void RegistrarEntregaAlServicioLosTiposReales()
    {
        _servicio.Setup(s => s.Registrar("0004", "Cuarto", 5.5m)).Returns(new Elemento("0004", "Cuarto", 5.50m));

        Resultado<Elemento> resultado = _controlador.Registrar(" 0004 ", " Cuarto ", " 5.5 ");

        Assert.Equal(EstadoResultado.OK, resultado.Estado);
        _servicio.Verify(s => s.Registrar("0004", "Cuarto", 5.5m), Times.Once);
    }

    [Fact]
    public void ListarDelegaEnElServicio()
    {
        var lista = new List<Elemento> { Primero };
        _servicio.Setup(s => s.Listar()).Returns(lista);

        Resultado<List<Elemento>> resultado = _controlador.Listar();

        Assert.Same(lista, resultado.Datos);
    }

    [Fact]
    public void UnErrorDeNegocioDelServicioSeConvierteEnResultadoDeError()
    {
        _servicio.Setup(s => s.Consultar("9999")).Throws(new ExcepcionMonster(CodigoError.ELE_001));

        Resultado<Elemento> resultado = _controlador.Consultar("9999");

        Assert.Equal(EstadoResultado.ERROR, resultado.Estado);
        Assert.Equal("ELE-001", resultado.Codigo);
        Assert.Equal(CodigoError.ELE_001.Mensaje, resultado.Mensaje);
        Assert.Null(resultado.Datos);
    }

    [Fact]
    public void UnErrorTecnicoDelServicioSeConvierteEnErr500SinDetalles()
    {
        _servicio.Setup(s => s.Listar()).Throws(new InvalidOperationException("detalle interno"));

        Resultado<List<Elemento>> resultado = _controlador.Listar();

        Assert.Equal("ERR-500", resultado.Codigo);
        Assert.Equal(CodigoError.ERR_500.Mensaje, resultado.Mensaje);
    }
}
