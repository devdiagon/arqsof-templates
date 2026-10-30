using ec.edu.monster.api;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>
/// El origen de las variables de entorno y la plataforma se inyectan: las pruebas no tocan el entorno real ni dependen de
/// <c>DeviceInfo</c> (que solo existe con la aplicación en ejecución). Ninguna fija el valor de <c>Puerto</c> ni de
/// <c>IpPc</c>: son constantes del proyecto y se cambian sin tocar estas pruebas.
/// </summary>
public class ConfiguracionApiPruebas
{
    private static readonly string PorDefecto = ConfiguracionApi.UrlPorDefecto(false, false);

    [Theory]
    [InlineData("http://h:1/api", "http://h:1/api/")]
    [InlineData("http://h:1/api/", "http://h:1/api/")]
    [InlineData("  http://h:1/api///  ", "http://h:1/api/")]
    public void LaUrlSeNormalizaConUnaSolaBarraFinal(string entrada, string esperado)
    {
        Assert.Equal(esperado, ConfiguracionApi.NormalizarUrl(entrada));
    }

    [Fact]
    public void EnUnaPlataformaQueNoEsAndroidElHostEsLocalhost()
    {
        Assert.Equal($"http://localhost:{ConfiguracionApi.Puerto}/{ConfiguracionApi.Ruta}/",
            ConfiguracionApi.UrlPorDefecto(false, false));
        Assert.Equal(ConfiguracionApi.UrlPorDefecto(false, false), ConfiguracionApi.UrlPorDefecto(false, true));
    }

    [Fact]
    public void EnElEmuladorDeAndroidElHostEsElAliasDelPc()
    {
        Assert.Equal($"http://10.0.2.2:{ConfiguracionApi.Puerto}/{ConfiguracionApi.Ruta}/",
            ConfiguracionApi.UrlPorDefecto(true, true));
    }

    [Fact]
    public void EnUnTelefonoFisicoElHostEsLaIpDelPc()
    {
        Assert.Equal($"http://{ConfiguracionApi.IpPc}:{ConfiguracionApi.Puerto}/{ConfiguracionApi.Ruta}/",
            ConfiguracionApi.UrlPorDefecto(true, false));
        Assert.DoesNotContain("10.0.2.2", ConfiguracionApi.UrlPorDefecto(true, false));
    }

    [Fact]
    public void LaUrlPorDefectoTerminaEnBarraYLlevaElPrefijoDeLaApi()
    {
        Assert.EndsWith($"/{ConfiguracionApi.Ruta}/", PorDefecto);
        Assert.False(ConfiguracionApi.Ruta.StartsWith('/') || ConfiguracionApi.Ruta.EndsWith('/'));
    }

    [Fact]
    public void SinVariableSeUsaLaUrlPorDefectoNormalizada()
    {
        Assert.Equal(PorDefecto, ConfiguracionApi.UrlBase(PorDefecto, _ => null));
    }

    [Fact]
    public void LaVariableApiBaseUrlSobrescribeLaUrlSinRecompilar()
    {
        string? pedida = null;

        string url = ConfiguracionApi.UrlBase(PorDefecto, nombre =>
        {
            pedida = nombre;
            return " http://otro:9/api ";
        });

        Assert.Equal("API_BASE_URL", pedida);
        Assert.Equal("http://otro:9/api/", url);
    }

    [Fact]
    public void UnaVariableEnBlancoSeIgnora()
    {
        Assert.Equal(PorDefecto, ConfiguracionApi.UrlBase(PorDefecto, _ => "   "));
    }

    [Fact]
    public void ElTiempoLimiteEsDeDiezSegundos()
    {
        Assert.Equal(TimeSpan.FromSeconds(10), ConfiguracionApi.TiempoLimite);
    }
}
