using ec.edu.monster.dto;
using ec.edu.monster.excepciones;
using ec.edu.monster.servicios;
using Xunit;

namespace ec.edu.monster.pruebas;

public class ServicioBasePruebas
{
    /// <summary>Expone la verificación protegida de la base para poder probarla.</summary>
    private sealed class ServicioDePrueba : ServicioBase
    {
        public R Comprobar<R>(R? respuesta) where R : RespuestaBaseDto => Verificar(respuesta);
    }

    private readonly ServicioDePrueba _servicio = new();

    private static RespuestaBaseDto Respuesta(string? estado, string? codigo, string? mensaje) =>
        new() { Estado = estado, Codigo = codigo, Mensaje = mensaje };

    [Fact]
    public void UnaRespuestaOkSeDevuelveSinCambios()
    {
        RespuestaBaseDto ok = Respuesta("OK", "200", "Operación exitosa");

        Assert.Same(ok, _servicio.Comprobar(ok));
    }

    [Fact]
    public void UnaRespuestaDeErrorLanzaExcepcionConElCodigoYElMensajeDelServidor()
    {
        ExcepcionServicio e = Assert.Throws<ExcepcionServicio>(
            () => _servicio.Comprobar(Respuesta("ERROR", "ABC-001", "Regla de negocio incumplida")));

        Assert.Equal("ABC-001", e.Codigo);
        Assert.Equal("Regla de negocio incumplida", e.Message);
        Assert.False(e.ServidorNoDisponible);
    }

    [Fact]
    public void UnErrorTecnicoDelServidorConservaSuCodigoYSuMensajeGenerico()
    {
        ExcepcionServicio e = Assert.Throws<ExcepcionServicio>(
            () => _servicio.Comprobar(Respuesta("ERROR", "ERR-500", "Error interno del servidor")));

        Assert.Equal("ERR-500", e.Codigo);
        Assert.Equal("Error interno del servidor", e.Message);
    }

    [Fact]
    public void UnaRespuestaNulaSeTrataComoRespuestaIlegible()
    {
        ExcepcionServicio e = Assert.Throws<ExcepcionServicio>(() => _servicio.Comprobar<RespuestaBaseDto>(null));

        Assert.Equal(ExcepcionServicio.Srv000, e.Codigo);
        Assert.Equal(ExcepcionServicio.MensajeSrv000, e.Message);
        Assert.True(e.RespuestaIlegible);
    }

    [Fact]
    public void UnEstadoDistintoDeOkSeTrataComoError()
    {
        Assert.Throws<ExcepcionServicio>(() => _servicio.Comprobar(Respuesta(null, null, "sin estado")));
        Assert.Throws<ExcepcionServicio>(() => _servicio.Comprobar(Respuesta("ok", "200", "minúsculas")));
    }
}
