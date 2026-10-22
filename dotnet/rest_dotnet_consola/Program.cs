using System.Text;
using ec.edu.monster.api;
using ec.edu.monster.modelo;

// Las tildes y la ñ salen bien si la terminal también está en UTF-8 (PowerShell y Windows Terminal suelen estarlo)
Console.OutputEncoding = Encoding.UTF8;
Console.InputEncoding = Encoding.UTF8;

try
{
    // Un solo HttpClient para toda la ejecución (uno por petición agotaría los sockets) y una sola Sesion.
    using var http = ClienteApi.CrearHttpClient();
    var cliente = new ClienteApi(http);
    var sesion = new Sesion();
    // TODO: servicios (reciben cliente y sesion), vistas y controladores del dominio, y un bucle que alterne
    //       entre ellos según su enum Resultado (ver README §4).
    Console.WriteLine($"Plantilla lista. API: {http.BaseAddress} (sesión: {sesion.Token is not null}, cliente: {cliente.GetType().Name})");
}
catch (EndOfStreamException)
{
    // La entrada se cerró: fin del programa.
}
catch (Exception ex)
{
    Console.Error.WriteLine($"No fue posible iniciar el cliente: {ex.Message}");
}
