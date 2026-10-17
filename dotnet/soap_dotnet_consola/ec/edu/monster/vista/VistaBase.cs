namespace ec.edu.monster.vista;

/// <summary>Única capa que lee el teclado y escribe en la consola. Sin lógica de negocio ni acceso al servidor.</summary>
public abstract class VistaBase
{
    protected static void Titulo(string texto)
    {
        Console.WriteLine();
        Console.WriteLine(new string('=', 40));
        Console.WriteLine(texto);
        Console.WriteLine(new string('=', 40));
    }

    /// <summary>Pide un texto y lo devuelve recortado. Lanza <see cref="EndOfStreamException"/> si la entrada se cerró.</summary>
    protected static string Pedir(string etiqueta)
    {
        Console.Write($"{etiqueta}: ");
        // ReadLine devuelve null al cerrarse la entrada (Ctrl+Z / redirección): se corta en vez de repetir.
        return (Console.ReadLine() ?? throw new EndOfStreamException()).Trim();
    }

    /// <summary>Lista numerada; devuelve el índice (base 0) o -1 si se elige «0. Cancelar».</summary>
    protected static int Elegir(string titulo, IReadOnlyList<string> opciones)
    {
        Console.WriteLine();
        Console.WriteLine(titulo);
        for (var i = 0; i < opciones.Count; i++)
            Console.WriteLine($"  {i + 1}. {opciones[i]}");
        Console.WriteLine("  0. Cancelar");

        while (true)
        {
            if (int.TryParse(Pedir("Opción"), out var n) && n >= 0 && n <= opciones.Count)
                return n - 1;
            MostrarError("Opción inválida");
        }
    }

    public static void Mostrar(string mensaje) => Console.WriteLine(mensaje);

    public static void MostrarError(string mensaje) => Console.WriteLine($"[Error] {mensaje}");
}
