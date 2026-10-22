using ec.edu.monster.api;

namespace ec.edu.monster.pruebas;

public class ConfiguracionApiPruebas
{
    [Theory]
    [InlineData(null)]
    [InlineData("")]
    [InlineData("   ")]
    public void Sin_variable_se_usa_la_url_por_defecto(string? variable)
    {
        Assert.Equal(ConfiguracionApi.Normalizar(ConfiguracionApi.UrlBasePorDefecto), ConfiguracionApi.Resolver(variable));
    }

    [Fact]
    public void La_variable_sobrescribe_la_url_por_defecto()
    {
        Assert.Equal("http://192.168.1.10:57592/api/", ConfiguracionApi.Resolver("http://192.168.1.10:57592/api/"));
    }

    [Theory]
    [InlineData("http://localhost:5267/api", "http://localhost:5267/api/")]
    [InlineData("http://localhost:5267/api/", "http://localhost:5267/api/")]
    [InlineData("  http://localhost:8080/Contexto/api  ", "http://localhost:8080/Contexto/api/")]
    public void La_url_siempre_termina_en_una_sola_barra(string entrada, string esperada)
    {
        Assert.Equal(esperada, ConfiguracionApi.Normalizar(entrada));
    }

    [Fact]
    public void La_url_por_defecto_es_una_url_absoluta_valida()
    {
        Assert.True(Uri.TryCreate(ConfiguracionApi.Resolver(null), UriKind.Absolute, out var uri));
        Assert.EndsWith("/", uri!.AbsoluteUri);
    }
}
