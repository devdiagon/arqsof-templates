using ec.edu.monster.db;
using ec.edu.monster.excepciones;
using ec.edu.monster.modelo;
using ec.edu.monster.servicios;
using Moq;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>
/// EJEMPLO: orquestación del servicio con la base de datos simulada. Se comprueba qué se pide al repositorio y cómo
/// termina la transacción.
/// </summary>
public class ElementoServicioPruebas
{
    private static readonly Elemento Primero = new("0001", "Primer elemento", 10.50m);

    private readonly Mock<AccesoDB> _accesoDB = new();
    private readonly Mock<IElementoRepositorio> _repositorio = new();
    private readonly ConexionFalsa _conexion = new();
    private readonly ElementoServicio _servicio;

    public ElementoServicioPruebas()
    {
        _servicio = new ElementoServicio(_accesoDB.Object, _repositorio.Object);
        _accesoDB.Setup(a => a.ObtenerConexion()).Returns(_conexion);
    }

    [Fact]
    public void ConsultarDevuelveElElementoConfirmaYCierraLaConexion()
    {
        _repositorio.Setup(r => r.BuscarPorCodigo(_conexion.Transaccion, "0001")).Returns(Primero);

        Elemento elemento = _servicio.Consultar("0001");

        Assert.Equal(Primero, elemento);
        Assert.Equal(1, _conexion.Transaccion.Confirmaciones);
        Assert.True(_conexion.Cerrada);
    }

    [Fact]
    public void ConsultarUnElementoInexistenteLanzaEle001RevierteYCierraLaConexion()
    {
        ExcepcionMonster e = Assert.Throws<ExcepcionMonster>(() => _servicio.Consultar("9999"));

        Assert.Equal(CodigoError.ELE_001, e.Error);
        Assert.Equal(1, _conexion.Transaccion.Reversiones);
        Assert.True(_conexion.Cerrada);
    }

    [Fact]
    public void ListarDevuelveLosElementosDelRepositorio()
    {
        var lista = new List<Elemento> { Primero };
        _repositorio.Setup(r => r.Listar(_conexion.Transaccion)).Returns(lista);

        Assert.Same(lista, _servicio.Listar());
    }

    [Fact]
    public void RegistrarInsertaConfirmaYCierra()
    {
        Elemento elemento = _servicio.Registrar("0004", "Cuarto", 5m);

        Assert.Equal(new Elemento("0004", "Cuarto", 5.00m), elemento);
        _repositorio.Verify(r => r.Insertar(_conexion.Transaccion, elemento), Times.Once);
        Assert.Equal(1, _conexion.Transaccion.Confirmaciones);
        Assert.Equal(0, _conexion.Transaccion.Reversiones);
        Assert.True(_conexion.Cerrada);
    }

    [Fact]
    public void RegistrarUnCodigoExistenteRevierteYLanzaEle002()
    {
        _repositorio.Setup(r => r.BuscarPorCodigo(_conexion.Transaccion, "0001")).Returns(Primero);

        ExcepcionMonster e = Assert.Throws<ExcepcionMonster>(() => _servicio.Registrar("0001", "Otro", 1m));

        Assert.Equal(CodigoError.ELE_002, e.Error);
        _repositorio.Verify(r => r.Insertar(It.IsAny<System.Data.Common.DbTransaction>(), It.IsAny<Elemento>()), Times.Never);
        Assert.Equal(1, _conexion.Transaccion.Reversiones);
        Assert.Equal(0, _conexion.Transaccion.Confirmaciones);
        Assert.True(_conexion.Cerrada);
    }

    [Fact]
    public void UnFalloDeLaBaseDeDatosRevierteYContinuaHaciaElControlador()
    {
        var fallo = new InvalidOperationException("fallo simulado");
        _repositorio.Setup(r => r.Insertar(_conexion.Transaccion, It.IsAny<Elemento>())).Throws(fallo);

        InvalidOperationException lanzada = Assert.Throws<InvalidOperationException>(
            () => _servicio.Registrar("0004", "Cuarto", 5m));

        Assert.Same(fallo, lanzada);
        Assert.Equal(1, _conexion.Transaccion.Reversiones);
        Assert.True(_conexion.Cerrada);
    }

    [Fact]
    public void ElValorSeRedondeaADosDecimales()
    {
        Assert.Equal(10.51m, ElementoServicio.RedondearValor(10.505m));
        Assert.Equal(10.50m, ElementoServicio.RedondearValor(10.504m));
    }
}
