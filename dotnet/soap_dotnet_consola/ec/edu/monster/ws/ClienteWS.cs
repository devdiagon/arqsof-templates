using System.ServiceModel;
using ec.edu.monster.excepciones;

namespace ec.edu.monster.ws;

/// <summary>
/// Piezas genéricas del transporte SOAP sobre el proxy generado (dotnet-svcutil): binding, timeouts y llamada
/// con traducción de fallos de red. Cada proyecto aporta su cliente generado (<c>XxxClient</c>) en esta carpeta.
/// Regla R10: el cliente no depende del nombre del puerto ni del binding del WSDL; solo de la URL.
/// </summary>
public static class ClienteWS
{
    /// <summary>El esquema de la URL decide el modo: https usa seguridad de transporte; http, ninguna.</summary>
    public static BasicHttpBinding CrearBinding(string url)
    {
        var https = url.StartsWith("https://", StringComparison.OrdinalIgnoreCase);
        return new BasicHttpBinding(https ? BasicHttpSecurityMode.Transport : BasicHttpSecurityMode.None)
        {
            OpenTimeout = ConfiguracionServicio.TiempoLimite,
            SendTimeout = ConfiguracionServicio.TiempoLimite,
            ReceiveTimeout = ConfiguracionServicio.TiempoLimite,
            CloseTimeout = ConfiguracionServicio.TiempoLimite
        };
    }

    /// <summary>
    /// Un canal por llamada: crea el cliente, ejecuta la operación y lo cierra. Si falla la comunicación hace
    /// <c>Abort()</c> y lanza <see cref="ExcepcionServicio"/> con código <c>RED</c>. Los errores de negocio no pasan
    /// por aquí: llegan en el cuerpo de la respuesta (HTTP 200) y los revisa el servicio.
    /// </summary>
    public static async Task<TRespuesta> LlamarAsync<TCliente, TRespuesta>(
        Func<TCliente> crearCliente, Func<TCliente, Task<TRespuesta>> operacion)
        where TCliente : ICommunicationObject
    {
        var cliente = crearCliente();
        try
        {
            var respuesta = await operacion(cliente);
            await Task.Factory.FromAsync(cliente.BeginClose, cliente.EndClose, null);
            return respuesta;
        }
        catch (Exception ex) when (ex is CommunicationException or TimeoutException)
        {
            cliente.Abort();
            throw new ExcepcionServicio(
                "No se pudo comunicar con el servidor. Verifique que esté encendido y la URL del servicio.",
                ExcepcionServicio.CodigoRed, ex);
        }
        catch
        {
            cliente.Abort();
            throw;
        }
    }
}
