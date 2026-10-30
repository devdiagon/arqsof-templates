using ec.edu.monster.excepciones;
using Xunit;

namespace ec.edu.monster.pruebas;

public class ExcepcionServicioPruebas
{
    [Fact]
    public void LlevaElCodigoYElMensajeQueSeMostraraAlUsuario()
    {
        var e = new ExcepcionServicio("ABC-001", "El recurso no existe");

        Assert.Equal("ABC-001", e.Codigo);
        Assert.Equal("El recurso no existe", e.Message);
        Assert.False(e.ServidorNoDisponible);
        Assert.False(e.RespuestaIlegible);
    }

    [Fact]
    public void LosCodigosPropiosDelClienteSeReconocen()
    {
        Assert.True(new ExcepcionServicio(ExcepcionServicio.Red, ExcepcionServicio.MensajeRed).ServidorNoDisponible);
        Assert.True(new ExcepcionServicio(ExcepcionServicio.Srv000, ExcepcionServicio.MensajeSrv000).RespuestaIlegible);
    }

    [Fact]
    public void ConservaLaCausaCuandoSeLaDan()
    {
        var causa = new TimeoutException("agotado");

        var e = new ExcepcionServicio(ExcepcionServicio.Red, ExcepcionServicio.MensajeRed, causa);

        Assert.Same(causa, e.InnerException);
    }

    [Fact]
    public void LosMensajesPropiosSonLosDeLaEspecificacion()
    {
        Assert.Equal("Servidor no disponible, intente más tarde", ExcepcionServicio.MensajeRed);
        Assert.Equal("No se pudo interpretar la respuesta del servidor", ExcepcionServicio.MensajeSrv000);
        Assert.Equal("RED", ExcepcionServicio.Red);
        Assert.Equal("SRV-000", ExcepcionServicio.Srv000);
    }
}
