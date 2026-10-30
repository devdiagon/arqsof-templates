using ec.edu.monster.api;
using Xunit;

namespace ec.edu.monster.pruebas;

public class ConfiguracionApiPruebas
{
    [Fact]
    public void SinVariableSeUsaLaUrlPorDefectoConBarraFinal()
    {
        Assert.Equal(ConfiguracionApi.UrlBasePorDefecto, ConfiguracionApi.UrlBase(_ => null));
        Assert.EndsWith("/", ConfiguracionApi.UrlBasePorDefecto);
    }

    [Fact]
    public void LaVariableApiBaseUrlSobrescribeLaUrlSinRecompilar()
    {
        string url = ConfiguracionApi.UrlBase(nombre => nombre == "API_BASE_URL" ? "http://10.0.2.2:5267/api" : null);

        Assert.Equal("http://10.0.2.2:5267/api/", url);
    }

    [Fact]
    public void UnaVariableEnBlancoSeIgnora()
    {
        Assert.Equal(ConfiguracionApi.UrlBasePorDefecto, ConfiguracionApi.UrlBase(_ => "   "));
    }

    [Theory]
    [InlineData("http://h:1/api", "http://h:1/api/")]
    [InlineData("http://h:1/api/", "http://h:1/api/")]
    [InlineData("  http://h:1/api///  ", "http://h:1/api/")]
    public void LaUrlSeNormalizaConUnaSolaBarraFinal(string entrada, string esperado)
    {
        Assert.Equal(esperado, ConfiguracionApi.NormalizarUrl(entrada));
    }

    [Fact]
    public void ElTiempoLimiteEsDeDiezSegundos()
    {
        Assert.Equal(TimeSpan.FromSeconds(10), ConfiguracionApi.TiempoLimite);
    }
}
