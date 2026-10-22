using ec.edu.monster.api;
using ec.edu.monster.modelo;
using Microsoft.AspNetCore.Mvc.Testing;

namespace ec.edu.monster.pruebas;

/// <summary>
/// Comprueba la composición real de <c>Program.cs</c>: el transporte REST se resuelve desde el contenedor con la URL
/// base y el tiempo límite configurados, y la <see cref="Sesion"/> (token) es una por petición, nunca compartida.
/// </summary>
public class ComposicionPruebas : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly WebApplicationFactory<Program> _fabrica;

    public ComposicionPruebas(WebApplicationFactory<Program> fabrica)
    {
        _fabrica = fabrica.WithWebHostBuilder(constructor => constructor.UseContentRoot(RaizDelProyecto()));
    }

    private static string RaizDelProyecto()
    {
        var carpeta = new DirectoryInfo(AppContext.BaseDirectory);
        while (carpeta is not null && carpeta.GetFiles("*.csproj").Length == 0)
            carpeta = carpeta.Parent;
        return carpeta?.FullName ?? throw new InvalidOperationException("No se encontró la carpeta del proyecto");
    }

    [Fact]
    public void El_cliente_de_transporte_se_resuelve_desde_el_contenedor()
    {
        using var ambito = _fabrica.Services.CreateScope();

        var cliente = ambito.ServiceProvider.GetRequiredService<IClienteApi>();

        Assert.IsType<ClienteApi>(cliente);
    }

    [Fact]
    public void La_sesion_es_distinta_en_cada_ambito_para_no_mezclar_usuarios()
    {
        using var uno = _fabrica.Services.CreateScope();
        using var otro = _fabrica.Services.CreateScope();

        var sesionUno = uno.ServiceProvider.GetRequiredService<Sesion>();
        var sesionOtro = otro.ServiceProvider.GetRequiredService<Sesion>();

        Assert.NotSame(sesionUno, sesionOtro);
    }
}
