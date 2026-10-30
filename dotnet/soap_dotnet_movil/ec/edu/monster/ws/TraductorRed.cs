using System.ServiceModel;
using ec.edu.monster.excepciones;

namespace ec.edu.monster.ws;

/// <summary>
/// Ejecuta una llamada al proxy generado y traduce sus fallos de red y de protocolo a <see cref="ExcepcionServicio"/>
/// con código <c>RED</c>: las capas superiores nunca ven una excepción de WCF (red caída, tiempo agotado, Fault,
/// respuesta que no es SOAP). Un canal por llamada: se crea el cliente, se ejecuta la operación y se cierra.
/// Los errores de negocio no pasan por aquí: llegan como respuesta con <c>estado = ERROR</c> y los trata <c>ServicioBase</c>.
/// </summary>
public static class TraductorRed
{
    public static async Task<TRespuesta> LlamarAsync<TCliente, TRespuesta>(
        Func<TCliente> crearCliente, Func<TCliente, Task<TRespuesta>> operacion)
        where TCliente : ICommunicationObject
    {
        TCliente cliente = crearCliente();
        TRespuesta respuesta;
        try
        {
            respuesta = await operacion(cliente);
        }
        catch (Exception e) when (e is CommunicationException or TimeoutException)
        {
            cliente.Abort();
            throw new ExcepcionServicio(ExcepcionServicio.Red, ExcepcionServicio.MensajeRed, e);
        }
        catch
        {
            cliente.Abort();
            throw;
        }

        // La operación ya terminó bien: un fallo al cerrar el canal no debe presentarse como un error de la operación
        // (una operación que ya cambió datos no puede mostrarse como «servidor no disponible»).
        try
        {
            await Task.Factory.FromAsync(cliente.BeginClose, cliente.EndClose, null);
        }
        catch (Exception e) when (e is CommunicationException or TimeoutException)
        {
            cliente.Abort();
        }
        return respuesta;
    }
}
