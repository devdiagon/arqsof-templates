using ec.edu.monster.ws;

namespace ec.edu.monster.pruebas;

// No se prueba la URL por defecto: depende de DeviceInfo (plataforma y emulador), que solo existe con la app en ejecución.
public class ConfiguracionServicioPruebas
{
    [Fact]
    public void La_variable_sobrescribe_la_url_por_defecto()
    {
        Assert.Equal("http://192.168.1.10:57592/Ruta", ConfiguracionServicio.Resolver("http://192.168.1.10:57592/Ruta"));
    }

    [Theory]
    [InlineData("http://localhost:8080/Contexto/Servicio/", "http://localhost:8080/Contexto/Servicio")]
    [InlineData("  http://localhost:57592/Ruta  ", "http://localhost:57592/Ruta")]
    public void La_url_se_normaliza_sin_espacios_ni_barra_final(string entrada, string esperada)
    {
        Assert.Equal(esperada, ConfiguracionServicio.Resolver(entrada));
    }
}
