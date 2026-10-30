using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>
/// La consola es global del proceso: todas las pruebas que la reemplazan van en esta colección, que no se ejecuta en
/// paralelo. Las demás pruebas no usan la consola (las vistas se sustituyen con Moq).
/// </summary>
[CollectionDefinition(Nombre, DisableParallelization = true)]
public sealed class ColeccionConsola
{
    public const string Nombre = "Consola";
}

/// <summary>
/// Reemplaza la entrada, la salida y el error estándar mientras vive y los restaura al terminar. La entrada son líneas
/// separadas por salto de línea; al agotarse, la lectura devuelve <c>null</c> (como un <c>stdin</c> cerrado).
/// </summary>
internal sealed class ConsolaFalsa : IDisposable
{
    private readonly TextReader _entradaOriginal = Console.In;
    private readonly TextWriter _salidaOriginal = Console.Out;
    private readonly TextWriter _errorOriginal = Console.Error;
    private readonly StringWriter _salida = new();
    private readonly StringWriter _error = new();

    public ConsolaFalsa(params string[] lineasDeEntrada)
    {
        Console.SetIn(new StringReader(lineasDeEntrada.Length == 0 ? "" : string.Join("\n", lineasDeEntrada) + "\n"));
        Console.SetOut(_salida);
        Console.SetError(_error);
    }

    /// <summary>Lo escrito en la salida, con los fines de línea normalizados a LF.</summary>
    public string Salida => _salida.ToString().Replace("\r\n", "\n");

    public string Error => _error.ToString().Replace("\r\n", "\n");

    public void Dispose()
    {
        Console.SetIn(_entradaOriginal);
        Console.SetOut(_salidaOriginal);
        Console.SetError(_errorOriginal);
    }
}
