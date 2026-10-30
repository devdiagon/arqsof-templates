using System.ServiceModel;
using ec.edu.monster.ws;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>El origen de las variables de entorno se inyecta: las pruebas no tocan el entorno real.</summary>
public class ConfiguracionServicioPruebas
{
    private static Func<string, string?> SinVariables => _ => null;

    private static Func<string, string?> Variables(params (string nombre, string valor)[] definidas) =>
        nombre => definidas.Where(d => d.nombre == nombre).Select(d => d.valor).FirstOrDefault();

    [Fact]
    public void QuitaBarrasFinalesYEspacios()
    {
        Assert.Equal("http://h:8080/ctx/Servicio", ConfiguracionServicio.NormalizarUrl(" http://h:8080/ctx/Servicio// "));
    }

    [Fact]
    public void LaVariableWsUrlTienePrioridadYSeNormaliza()
    {
        string url = ConfiguracionServicio.UrlConfigurada(Variables(("WS_URL", " http://otro:5267/RutaLibre/ ")));

        Assert.Equal("http://otro:5267/RutaLibre", url);
    }

    [Fact]
    public void SinConfiguracionSeUsaLaUrlPorDefectoCompleta()
    {
        Assert.Equal(ConfiguracionServicio.UrlPorDefecto, ConfiguracionServicio.UrlConfigurada(SinVariables));
    }

    [Fact]
    public void UnaVariableEnBlancoSeIgnora()
    {
        Assert.Equal(ConfiguracionServicio.UrlPorDefecto, ConfiguracionServicio.UrlConfigurada(Variables(("WS_URL", "   "))));
    }

    // ------------------------------------------------------------------ varios servicios (una URL completa por servicio)

    [Fact]
    public void LaVariableDelServicioTienePrioridadYSeNormaliza()
    {
        string url = ConfiguracionServicio.UrlConfigurada("consulta", "http://h:8080/ctx/Consulta",
            Variables(("WS_URL_CONSULTA", " http://otro:5267/RutaLibre/ ")));

        Assert.Equal("http://otro:5267/RutaLibre", url);
    }

    [Fact]
    public void ElNombreDeLaVariableEsWsUrlYElServicioEnMayusculas()
    {
        string? pedida = null;

        ConfiguracionServicio.UrlConfigurada("operacion", "http://h:1/D", nombre => { pedida = nombre; return null; });

        Assert.Equal("WS_URL_OPERACION", pedida);
    }

    [Fact]
    public void SinConfiguracionDelServicioSeUsaSuUrlPorDefectoNormalizada()
    {
        Assert.Equal("http://h:8080/ctx/Consulta",
            ConfiguracionServicio.UrlConfigurada("consulta", " http://h:8080/ctx/Consulta/ ", SinVariables));
    }

    [Fact]
    public void CadaServicioSeConfiguraPorSeparadoSinAfectarALosDemas()
    {
        var variables = Variables(("WS_URL_CONSULTA", "http://uno:1/A"));

        Assert.Equal("http://uno:1/A", ConfiguracionServicio.UrlConfigurada("consulta", "http://x/Consulta", variables));
        Assert.Equal("http://x/Operacion", ConfiguracionServicio.UrlConfigurada("operacion", "http://x/Operacion", variables));
    }

    // ------------------------------------------------------------------ binding

    [Fact]
    public void ElBindingParaHttpNoUsaSeguridadYFijaLosTiemposLimite()
    {
        BasicHttpBinding binding = ConfiguracionServicio.CrearBinding("http://localhost:5267/Servicio");

        Assert.Equal(BasicHttpSecurityMode.None, binding.Security.Mode);
        Assert.Equal(TimeSpan.FromSeconds(10), binding.OpenTimeout);
        Assert.Equal(TimeSpan.FromSeconds(10), binding.SendTimeout);
        Assert.Equal(TimeSpan.FromSeconds(10), binding.ReceiveTimeout);
        Assert.Equal(TimeSpan.FromSeconds(10), binding.CloseTimeout);
    }

    [Fact]
    public void ElBindingParaHttpsUsaSeguridadDeTransporte()
    {
        BasicHttpBinding binding = ConfiguracionServicio.CrearBinding("HTTPS://servidor/Servicio");

        Assert.Equal(BasicHttpSecurityMode.Transport, binding.Security.Mode);
    }
}
