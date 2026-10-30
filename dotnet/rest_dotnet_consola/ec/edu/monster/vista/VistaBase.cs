namespace ec.edu.monster.vista;

/// <summary>
/// Única capa que lee el teclado y escribe en la consola. Los métodos públicos son <c>virtual</c> para que las pruebas
/// de los controladores los sustituyan con Moq.
/// </summary>
public abstract class VistaBase
{
    /// <summary>
    /// Pide un dato y lo devuelve recortado. Si la entrada se cierra (Ctrl+Z / Ctrl+D o <c>stdin</c> agotado) lanza
    /// <see cref="EndOfStreamException"/>: el bucle principal la trata como fin del programa en lugar de girar
    /// indefinidamente en un menú.
    /// </summary>
    public virtual string Pedir(string etiqueta)
    {
        Console.Write(etiqueta + ": ");
        return LeerLinea().Trim();
    }

    /// <summary>Muestra una lista numerada y devuelve el índice elegido (base 0), o -1 si cancela.</summary>
    protected int Elegir(string titulo, IReadOnlyList<string> opciones) =>
        Elegir(titulo, opciones, Textos.Cancelar);

    /// <summary>
    /// Igual que la anterior, pero la opción «0» lleva el texto indicado (p. ej. «Salir» o «Cerrar sesión»).
    /// Devuelve el índice elegido (base 0) o -1 si elige «0». Un título vacío no se imprime.
    /// </summary>
    protected int Elegir(string titulo, IReadOnlyList<string> opciones, string textoCero)
    {
        if (titulo.Length > 0)
        {
            Console.WriteLine(titulo);
        }
        for (int i = 0; i < opciones.Count; i++)
        {
            Console.WriteLine($"  {i + 1}. {opciones[i]}");
        }
        Console.WriteLine($"  0. {textoCero}");

        while (true)
        {
            string entrada = Pedir(Textos.Opcion);
            if (int.TryParse(entrada, System.Globalization.NumberStyles.Integer,
                    System.Globalization.CultureInfo.InvariantCulture, out int n))
            {
                if (n == 0)
                {
                    return -1;
                }
                if (n >= 1 && n <= opciones.Count)
                {
                    return n - 1;
                }
            }
            MostrarError(Textos.OpcionInvalida);
        }
    }

    /// <summary>Detiene la pantalla hasta que el usuario pulse Enter.</summary>
    public virtual void Pausar()
    {
        Console.Write(Textos.PulseEnter);
        LeerLinea();
    }

    /// <summary>Línea en blanco, el título y su subrayado.</summary>
    public virtual void MostrarTitulo(string titulo)
    {
        Console.WriteLine();
        Console.WriteLine(titulo);
        Console.WriteLine(new string('-', titulo.Length));
    }

    /// <summary>Imprime <see cref="Textos.Portada"/> si tiene contenido.</summary>
    public virtual void MostrarPortada()
    {
        if (!string.IsNullOrWhiteSpace(Textos.Portada))
        {
            Mostrar(Textos.Portada.TrimEnd());
        }
    }

    public virtual void Mostrar(string mensaje) => Console.WriteLine(mensaje);

    public virtual void MostrarError(string mensaje) => Console.WriteLine("[!] " + mensaje);

    private static string LeerLinea() =>
        Console.ReadLine() ?? throw new EndOfStreamException("La entrada de la consola se cerró");
}
