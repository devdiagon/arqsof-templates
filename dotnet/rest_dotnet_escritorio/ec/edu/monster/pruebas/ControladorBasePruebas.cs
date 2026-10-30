using ec.edu.monster.controlador;
using ec.edu.monster.excepciones;
using Xunit;

namespace ec.edu.monster.pruebas;

public class ControladorBasePruebas
{
    /// <summary>Controlador mínimo que expone el molde para probarlo sin pantalla ni servicio.</summary>
    private sealed class ControladorDePrueba : ControladorBase
    {
        public int ResultadosOcultados { get; private set; }
        public Func<ExcepcionServicio, bool>? TratarError { get; set; }

        public Task EjecutarAsync<T>(Func<Task<T>> tarea, Action<T> alExito) =>
            base.EjecutarAsync("Procesando…", tarea, alExito);

        protected override void OcultarResultado() => ResultadosOcultados++;

        protected override void Manejar(ExcepcionServicio e)
        {
            if (TratarError?.Invoke(e) != true)
            {
                base.Manejar(e);
            }
        }
    }

    private readonly ControladorDePrueba _controlador = new();

    [Fact]
    public async Task ElExitoEntregaElResultadoYDejaLaPantallaLibre()
    {
        int? recibido = null;

        await _controlador.EjecutarAsync(() => Task.FromResult(7), r => recibido = r);

        Assert.Equal(7, recibido);
        Assert.Equal(EstadoZona.Ninguno, _controlador.Estado);
        Assert.False(_controlador.Ocupado);
        Assert.True(_controlador.NoOcupado);
        Assert.Null(_controlador.Mensaje);
    }

    [Fact]
    public async Task UnErrorDelServicioMuestraSuMensajeNuncaElCodigo()
    {
        bool exito = false;

        await _controlador.EjecutarAsync<int>(() => throw new ExcepcionServicio("ERR-001", "El elemento no existe"),
            _ => exito = true);

        Assert.False(exito);
        Assert.Equal(EstadoZona.Error, _controlador.Estado);
        Assert.Equal("El elemento no existe", _controlador.Mensaje);
        Assert.False(_controlador.Ocupado);
    }

    [Fact]
    public async Task UnFalloDeRedMuestraElMensajeDelCliente()
    {
        await _controlador.EjecutarAsync<int>(
            () => throw new ExcepcionServicio(ExcepcionServicio.Red, ExcepcionServicio.MensajeRed), _ => { });

        Assert.Equal("Servidor no disponible, intente más tarde", _controlador.Mensaje);
    }

    [Fact]
    public async Task UnaExcepcionInesperadaMuestraElMensajeGenericoSinDetalle()
    {
        await _controlador.EjecutarAsync<int>(() => throw new InvalidOperationException("detalle interno"), _ => { });

        Assert.Equal(EstadoZona.Error, _controlador.Estado);
        Assert.Equal("Ocurrió un error inesperado, intente nuevamente", _controlador.Mensaje);
    }

    [Fact]
    public async Task MientrasEstaOcupadoSeMuestraElIndicadorYUnaSegundaAccionSeIgnora()
    {
        var pendiente = new TaskCompletionSource<int>();
        int llamadas = 0;

        Task primera = _controlador.EjecutarAsync(() => { llamadas++; return pendiente.Task; }, _ => { });
        Assert.True(_controlador.Ocupado);
        Assert.False(_controlador.NoOcupado);
        Assert.Equal(EstadoZona.Ocupado, _controlador.Estado);
        Assert.Equal("Procesando…", _controlador.TextoOcupado);

        await _controlador.EjecutarAsync(() => { llamadas++; return Task.FromResult(0); }, _ => { });
        pendiente.SetResult(1);
        await primera;

        Assert.Equal(1, llamadas);
        Assert.False(_controlador.Ocupado);
    }

    [Fact]
    public async Task AlEmpezarUnaAccionSeOcultaElResultadoAnterior()
    {
        await _controlador.EjecutarAsync(() => Task.FromResult(1), _ => { });

        Assert.Equal(1, _controlador.ResultadosOcultados);
    }

    [Fact]
    public async Task UnProyectoPuedeTratarUnCodigoPropioSobrescribiendoManejar()
    {
        string? codigo = null;
        _controlador.TratarError = e => { codigo = e.Codigo; return e.Codigo == "COD-001"; };

        await _controlador.EjecutarAsync<int>(() => throw new ExcepcionServicio("COD-001", "x"), _ => { });

        Assert.Equal("COD-001", codigo);
        Assert.Null(_controlador.Mensaje);
    }

    [Fact]
    public void AlMostrarLaPantallaQuedaLimpia()
    {
        _controlador.MostrarError("antes");

        _controlador.AlMostrar();

        Assert.Equal(EstadoZona.Ninguno, _controlador.Estado);
        Assert.Null(_controlador.Mensaje);
        Assert.Equal(1, _controlador.ResultadosOcultados);
    }

    [Fact]
    public void LaVistaSeEnteraDeLosCambios()
    {
        var cambios = new List<string?>();
        _controlador.PropertyChanged += (_, e) => cambios.Add(e.PropertyName);

        _controlador.MostrarExito("listo");

        Assert.Contains(nameof(ControladorBase.Mensaje), cambios);
        Assert.Contains(nameof(ControladorBase.Estado), cambios);
        Assert.Contains(nameof(ControladorBase.Ocupado), cambios);
    }
}
