using System.Text;
using ec.edu.monster;

// Las tildes y la ñ salen bien si la terminal también está en UTF-8 (chcp 65001 en cmd). Sin BOM.
// Si no hay consola asociada (entrada y salida redirigidas) el cambio de página de códigos puede fallar: se ignora.
var utf8 = new UTF8Encoding(encoderShouldEmitUTF8Identifier: false);
try { Console.InputEncoding = utf8; } catch (IOException) { }
try { Console.OutputEncoding = utf8; } catch (IOException) { }

try
{
    await Aplicacion.EjecutarAsync();
}
catch (EndOfStreamException)
{
    // entrada cerrada (Ctrl+Z / Ctrl+D o stdin agotado): fin del programa
    Console.WriteLine();
}
catch (Exception e)
{
    Console.Error.WriteLine("No fue posible iniciar el cliente: " + e.Message);
}
