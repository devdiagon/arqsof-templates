using System.Windows.Input;

namespace ec.edu.monster.controlador;

/// <summary>Comando síncrono con parámetro opcional (p. ej. la categoría elegida en un botón de la lista).</summary>
public class Comando : ICommand
{
    private readonly Action<object?> _ejecutar;

    public Comando(Action<object?> ejecutar)
    {
        _ejecutar = ejecutar;
    }

    public Comando(Action ejecutar) : this(_ => ejecutar())
    {
    }

    public event EventHandler? CanExecuteChanged
    {
        add { }
        remove { }
    }

    public bool CanExecute(object? parametro) => true;

    public void Execute(object? parametro) => _ejecutar(parametro);
}
