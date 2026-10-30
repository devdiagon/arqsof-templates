using System.Windows.Input;

namespace ec.edu.monster.controlador;

/// <summary>
/// Comando mínimo para enlazar botones al controlador. Siempre puede ejecutarse: mientras hay una llamada en curso la
/// pantalla entera está deshabilitada y el controlador ignora cualquier segunda acción.
/// </summary>
public sealed class Comando : ICommand
{
    private readonly Action<object?> _accion;

    public Comando(Action accion) : this(_ => accion())
    {
    }

    public Comando(Action<object?> accion)
    {
        _accion = accion;
    }

    // Nunca cambia: el evento existe solo por el contrato de ICommand.
    public event EventHandler? CanExecuteChanged
    {
        add { }
        remove { }
    }

    public bool CanExecute(object? parameter) => true;

    public void Execute(object? parameter) => _accion(parameter);
}
