using System.ServiceModel;
using ec.edu.monster.ws;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>
/// El origen de las variables de entorno y la plataforma se inyectan: las pruebas no tocan el entorno real ni dependen de
/// <c>DeviceInfo</c> (que solo existe con la aplicación en ejecución).
/// </summary>
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
    public void EnWindowsLaUrlPorDefectoEsCompletaYApuntaALocalhost()
    {
        Assert.Equal($"http://localhost:{ConfiguracionServicio.Puerto}/Ruta",
            ConfiguracionServicio.UrlPorDefecto("Ruta", false, false));
    }

    [Fact]
    public void EnElEmuladorDeAndroidElHostEsElAliasDelPc()
    {
        Assert.Equal($"http://10.0.2.2:{ConfiguracionServicio.Puerto}/Ruta",
            ConfiguracionServicio.UrlPorDefecto("Ruta", true, true));
    }

    [Fact]
    public void EnUnTelefonoFisicoElHostEsLaIpDelPc()
    {
        Assert.Equal($"http://{ConfiguracionServicio.IpPc}:{ConfiguracionServicio.Puerto}/Ruta",
            ConfiguracionServicio.UrlPorDefecto("Ruta", true, false));
    }

    [Fact]
    public void LaIpDelPcDeLaPlantillaEsUnMarcadorDePosicion()
    {
        Assert.Equal("127.0.0.1", ConfiguracionServicio.IpPc);
    }

    [Fact]
    public void LaVariableWsUrlTienePrioridadYSeNormaliza()
    {
        string url = ConfiguracionServicio.UrlConfigurada(Variables(("WS_URL", " http://otro:5267/RutaLibre/ ")));

        Assert.Equal("http://otro:5267/RutaLibre", url);
    }

    [Fact]
    public void LaVariableDelServicioTienePrioridadYSeNormaliza()
    {
        string url = ConfiguracionServicio.UrlConfigurada("consulta", "http://h:1/Defecto",
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
    public void SinConfiguracionSeUsaLaUrlPorDefectoNormalizada()
    {
        Assert.Equal("http://h:8080/ctx/Consulta",
            ConfiguracionServicio.UrlConfigurada("consulta", " http://h:8080/ctx/Consulta/ ", SinVariables));
    }

    [Fact]
    public void UnaVariableEnBlancoSeIgnora()
    {
        string url = ConfiguracionServicio.UrlConfigurada("consulta", "http://h:1/Defecto",
            Variables(("WS_URL_CONSULTA", "   ")));

        Assert.Equal("http://h:1/Defecto", url);
    }

    [Fact]
    public void CadaServicioSeConfiguraPorSeparadoSinAfectarALosDemas()
    {
        var variables = Variables(("WS_URL_CONSULTA", "http://uno:1/A"));

        Assert.Equal("http://uno:1/A", ConfiguracionServicio.UrlConfigurada("consulta", "http://x/Consulta", variables));
        Assert.Equal("http://x/Operacion", ConfiguracionServicio.UrlConfigurada("operacion", "http://x/Operacion", variables));
    }

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
