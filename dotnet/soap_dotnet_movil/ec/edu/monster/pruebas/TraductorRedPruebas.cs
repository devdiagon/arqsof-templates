using System.ServiceModel;
using ec.edu.monster.excepciones;
using ec.edu.monster.ws;
using Moq;
using Xunit;

namespace ec.edu.monster.pruebas;

public class TraductorRedPruebas
{
    /// <summary>
    /// Canal simulado cuyo cierre termina bien: <c>BeginClose</c> devuelve una tarea ya completada e invoca el callback,
    /// como haría WCF al terminar el cierre asíncrono (sin el callback, <c>Task.Factory.FromAsync</c> espera para siempre).
    /// </summary>
    private static Mock<ICommunicationObject> NuevoCanal()
    {
        var canal = new Mock<ICommunicationObject>();
        canal.Setup(c => c.BeginClose(It.IsAny<AsyncCallback>(), It.IsAny<object>()))
            .Returns<AsyncCallback, object>((callback, estado) =>
            {
                Task cierre = Task.CompletedTask;
                callback(cierre);
                return cierre;
            });
        return canal;
    }

    [Fact]
    public async Task UnaLlamadaCorrectaDevuelveSuResultadoYCierraElCanal()
    {
        Mock<ICommunicationObject> canal = NuevoCanal();

        string resultado = await TraductorRed.LlamarAsync(() => canal.Object, _ => Task.FromResult("listo"));

        Assert.Equal("listo", resultado);
        canal.Verify(c => c.BeginClose(It.IsAny<AsyncCallback>(), It.IsAny<object>()), Times.Once);
        canal.Verify(c => c.Abort(), Times.Never);
    }

    [Fact]
    public async Task UnFalloDeComunicacionSeTraduceACodigoRedConElMensajeGenericoYConservaLaCausa()
    {
        Mock<ICommunicationObject> canal = NuevoCanal();
        var causa = new EndpointNotFoundException("Connection refused");

        ExcepcionServicio e = await Assert.ThrowsAsync<ExcepcionServicio>(
            () => TraductorRed.LlamarAsync<ICommunicationObject, string>(() => canal.Object, _ => throw causa));

        Assert.Equal(ExcepcionServicio.Red, e.Codigo);
        Assert.Equal(ExcepcionServicio.MensajeRed, e.Message);
        Assert.Same(causa, e.InnerException);
        Assert.True(e.ServidorNoDisponible);
        canal.Verify(c => c.Abort(), Times.Once);
    }

    [Fact]
    public async Task UnTiempoAgotadoTambienSeTraduceACodigoRed()
    {
        Mock<ICommunicationObject> canal = NuevoCanal();

        ExcepcionServicio e = await Assert.ThrowsAsync<ExcepcionServicio>(
            () => TraductorRed.LlamarAsync<ICommunicationObject, string>(() => canal.Object, _ => throw new TimeoutException("agotado")));

        Assert.Equal(ExcepcionServicio.Red, e.Codigo);
        canal.Verify(c => c.Abort(), Times.Once);
    }

    [Fact]
    public async Task UnFaultDeProtocoloTambienSeTraduceACodigoRed()
    {
        Mock<ICommunicationObject> canal = NuevoCanal();

        ExcepcionServicio e = await Assert.ThrowsAsync<ExcepcionServicio>(
            () => TraductorRed.LlamarAsync<ICommunicationObject, string>(() => canal.Object, _ => throw new FaultException("Fault de la plataforma")));

        Assert.Equal(ExcepcionServicio.Red, e.Codigo);
    }

    [Fact]
    public async Task ElMensajeDeRedNoExponeDetallesTecnicos()
    {
        Mock<ICommunicationObject> canal = NuevoCanal();

        ExcepcionServicio e = await Assert.ThrowsAsync<ExcepcionServicio>(
            () => TraductorRed.LlamarAsync<ICommunicationObject, string>(
                () => canal.Object, _ => throw new EndpointNotFoundException("No hay conexión con http://localhost:5267/Servicio")));

        Assert.DoesNotContain("localhost", e.Message);
    }

    [Fact]
    public async Task UnErrorDeProgramacionQueNoEsDeRedNoSeDisfrazaDeRedPeroSeAbortaElCanal()
    {
        Mock<ICommunicationObject> canal = NuevoCanal();

        await Assert.ThrowsAsync<InvalidOperationException>(
            () => TraductorRed.LlamarAsync<ICommunicationObject, string>(() => canal.Object, _ => throw new InvalidOperationException("error de programación")));

        canal.Verify(c => c.Abort(), Times.Once);
    }

    [Fact]
    public async Task UnFalloAlCerrarElCanalDespuesDeUnaOperacionExitosaNoLaConvierteEnError()
    {
        // Una operación que ya cambió datos en el servidor no puede presentarse como «servidor no disponible» por un fallo al cerrar.
        var canal = new Mock<ICommunicationObject>();
        canal.Setup(c => c.BeginClose(It.IsAny<AsyncCallback>(), It.IsAny<object>()))
            .Throws(new CommunicationException("cierre fallido"));

        string resultado = await TraductorRed.LlamarAsync(() => canal.Object, _ => Task.FromResult("realizado"));

        Assert.Equal("realizado", resultado);
        canal.Verify(c => c.Abort(), Times.Once);
    }
}
