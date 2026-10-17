using System.Text;
using ec.edu.monster.modelo;
using ec.edu.monster.ws;

// Las tildes y la ñ salen bien si la terminal también está en UTF-8 (PowerShell y Windows Terminal suelen estarlo)
Console.OutputEncoding = Encoding.UTF8;
Console.InputEncoding = Encoding.UTF8;

try
{
    var sesion = new Sesion();
    // TODO: cliente = new ClienteXxx() (fábrica sobre el proxy generado, ver README §4), servicio(cliente, sesion),
    //       vistas y controladores, y un bucle que alterne entre ellos según su enum Resultado.
    Console.WriteLine($"Plantilla lista. WS: {ConfiguracionServicio.ObtenerUrl()} (sesión: {sesion.Token is not null})");
}
catch (EndOfStreamException)
{
    // La entrada se cerró: fin del programa.
}
catch (Exception ex)
{
    Console.Error.WriteLine($"No fue posible iniciar el cliente: {ex.Message}");
}
