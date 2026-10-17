using System.ServiceModel;
using ec.edu.monster.excepciones;
using ec.edu.monster.ws;

namespace ec.edu.monster.pruebas;

public class ClienteWSPruebas
{
    // Contrato de ejemplo mínimo (equivale al que genera dotnet-svcutil): sirve de modelo para probar el transporte.
    [ServiceContract(Namespace = "http://ws.ejemplo.edu.ec/")]
    public interface IContratoEjemplo
    {
        [OperationContract(Action = "http://ws.ejemplo.edu.ec/ServicioEjemplo/eco")]
        Task<string> ecoAsync(string texto);
    }

    public class ClienteEjemplo : ClientBase<IContratoEjemplo>, IContratoEjemplo
    {
        public ClienteEjemplo(string url)
            : base(ClienteWS.CrearBinding(url), new EndpointAddress(url)) { }

        public Task<string> ecoAsync(string texto) => Channel.ecoAsync(texto);
    }

    [Theory]
    [InlineData("http://localhost:5267/Ruta", BasicHttpSecurityMode.None)]
    [InlineData("http://localhost:57592/Ruta", BasicHttpSecurityMode.None)]
    [InlineData("https://localhost:44380/Ruta", BasicHttpSecurityMode.Transport)]
    [InlineData("HTTPS://localhost:44380/Ruta", BasicHttpSecurityMode.Transport)]
    public void El_esquema_de_la_url_decide_el_modo_de_seguridad(string url, BasicHttpSecurityMode modo)
    {
        Assert.Equal(modo, ClienteWS.CrearBinding(url).Security.Mode);
    }

    [Fact]
    public void El_binding_fija_los_tiempos_limite()
    {
        var binding = ClienteWS.CrearBinding("http://localhost/Ruta");

        Assert.Equal(ConfiguracionServicio.TiempoLimite, binding.SendTimeout);
        Assert.Equal(ConfiguracionServicio.TiempoLimite, binding.ReceiveTimeout);
    }

    [Fact]
    public async Task Servidor_caido_se_traduce_a_ExcepcionServicio_RED()
    {
        const string url = "http://localhost:1/Ruta";

        var ex = await Assert.ThrowsAsync<ExcepcionServicio>(
            () => ClienteWS.LlamarAsync(() => new ClienteEjemplo(url), c => c.ecoAsync("hola")));

        Assert.True(ex.ServidorNoDisponible);
        Assert.False(ex.ExigeLogin);
    }
}
