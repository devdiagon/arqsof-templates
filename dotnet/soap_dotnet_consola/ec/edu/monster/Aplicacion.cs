using ec.edu.monster.ws;

namespace ec.edu.monster;

/// <summary>
/// Punto de composición: crea el cliente del servicio, los servicios, las vistas y los controladores, y ejecuta el bucle de
/// navegación. Es el equivalente del <c>Main</c> de la plantilla Java.
/// </summary>
public static class Aplicacion
{
    public static Task EjecutarAsync()
    {
        // TODO: crear el cliente SOAP (README §4):
        //         IXCliente cliente = XClienteWS.Crear();
        //       los servicios (reciben el cliente y la sesión, si el proyecto tiene login), las vistas y los controladores, y
        //       alternar entre pantallas según el enum Resultado de cada controlador. Captura EndOfStreamException para dejarla
        //       subir a Program.cs (entrada cerrada = fin del programa) y cualquier otra Exception inesperada para mostrar
        //       Textos.ErrorInesperado por Console.Error y volver a la pantalla inicial.
        Console.WriteLine("Plantilla lista. WS: " + ConfiguracionServicio.UrlConfigurada());
        return Task.CompletedTask;
    }
}
