using ec.edu.monster.controlador;
using ec.edu.monster.excepciones;
using ec.edu.monster.vista;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>El molde de los controladores: ocupado, segunda acción ignorada, mensajes sin código y error inesperado.</summary>
public class ControladorBasePruebas
{
    /// <summary>Expone lo protegido de la base para poder probarlo.</summary>
    private sealed class ControladorDePrueba : ControladorBase
    {
        public bool CampoVacio(string? valor) => Vacio(valor);

        public Task Ejecutar<T>(Func<Task<T>> tarea, Action<T> alExito) => EjecutarAsync("Procesando…", tarea, alExito);
    }

    private readonly ControladorDePrueba _controlador = new();

    [Fact]
    public void AlInicioNoHayNadaQueMostrar()
    {
        Assert.Equal(EstadoZona.Ninguno, _controlador.Estado);
        Assert.False(_controlador.Ocupado);
        Assert.True(_controlador.NoOcupado);
        Assert.Null(_controlador.Mensaje);
    }

    [Fact]
    public async Task MientrasEsperaLaPantallaEstaOcupadaConSuTexto()
    {
        var espera = new TaskCompletionSource<int>();
        Task ejecucion = _controlador.Ejecutar(() => espera.Task, _ => { });

        Assert.True(_controlador.Ocupado);
        Assert.Equal("Procesando…", _controlador.TextoOcupado);

        espera.SetResult(1);
        await ejecucion;

        Assert.False(_controlador.Ocupado);
    }

    [Fact]
    public async Task UnaSegundaAccionMientrasEstaOcupadoSeIgnora()
    {
        var espera = new TaskCompletionSource<int>();
        int llamadas = 0;
        Task primera = _controlador.Ejecutar(() => { llamadas++; return espera.Task; }, _ => { });

        await _controlador.Ejecutar(() => { llamadas++; return Task.FromResult(0); }, _ => { });
        espera.SetResult(1);
        await primera;

        Assert.Equal(1, llamadas);
    }

    [Fact]
    public async Task UnExitoEntregaElResultadoYDejaLaPantallaLibre()
    {
        int recibido = 0;

        await _controlador.Ejecutar(() => Task.FromResult(42), r => recibido = r);

        Assert.Equal(42, recibido);
        Assert.Equal(EstadoZona.Ninguno, _controlador.Estado);
    }

    [Fact]
    public async Task UnErrorDelServidorMuestraSoloElMensajeNuncaElCodigo()
    {
        await _controlador.Ejecutar<int>(() => throw new ExcepcionServicio("XYZ-001", "Mensaje del servidor"), _ => { });

        Assert.Equal(EstadoZona.Error, _controlador.Estado);
        Assert.Equal("Mensaje del servidor", _controlador.Mensaje);
        Assert.DoesNotContain("XYZ-001", _controlador.Mensaje);
        Assert.False(_controlador.Ocupado);
    }

    [Fact]
    public async Task UnErrorInesperadoMuestraElMensajeGenerico()
    {
        await _controlador.Ejecutar<int>(() => throw new InvalidOperationException("detalle técnico"), _ => { });

        Assert.Equal(EstadoZona.Error, _controlador.Estado);
        Assert.Equal(Textos.ErrorInesperado, _controlador.Mensaje);
    }

    [Fact]
    public async Task UnaNuevaAccionLimpiaElMensajeAnterior()
    {
        await _controlador.Ejecutar<int>(() => throw new ExcepcionServicio("XYZ-001", "Mensaje del servidor"), _ => { });

        await _controlador.Ejecutar(() => Task.FromResult(1), _ => { });

        Assert.Equal(EstadoZona.Ninguno, _controlador.Estado);
        Assert.Null(_controlador.Mensaje);
    }

    [Fact]
    public void AlMostrarLaPantallaQuedaLimpia()
    {
        _controlador.MostrarError("algo");

        _controlador.AlMostrar();

        Assert.Equal(EstadoZona.Ninguno, _controlador.Estado);
        Assert.Null(_controlador.Mensaje);
    }

    [Theory]
    [InlineData(null, true)]
    [InlineData("", true)]
    [InlineData("   ", true)]
    [InlineData("a", false)]
    public void UnCampoEnBlancoSeConsideraVacio(string? valor, bool esperado)
    {
        Assert.Equal(esperado, _controlador.CampoVacio(valor));
    }
}
