using ec.edu.monster.api;

namespace ec.edu.monster.pruebas;

// No se prueba la URL por defecto: depende de DeviceInfo (plataforma y emulador), que solo existe con la app en ejecución.
public class ConfiguracionApiPruebas
{
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
        Assert.Equal(esperada, ConfiguracionApi.Resolver(entrada));
    }

    [Fact]
    public void Una_ruta_relativa_sin_barra_inicial_se_concatena_a_la_base()
    {
        using var http = ClienteApi.CrearHttpClient("http://localhost:5267/api/");

        Assert.Equal("http://localhost:5267/api/auth/login", new Uri(http.BaseAddress!, "auth/login").ToString());
    }
}
