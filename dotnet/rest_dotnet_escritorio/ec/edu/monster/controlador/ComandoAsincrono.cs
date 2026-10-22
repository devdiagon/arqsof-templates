using System.Windows.Input;

namespace ec.edu.monster.controlador;

/// <summary>Comando que ejecuta una tarea asíncrona y no se reentra mientras está en curso (evita doble envío).</summary>
public class ComandoAsincrono : ICommand
{
    private readonly Func<Task> _ejecutar;
    private readonly Func<bool>? _puedeEjecutar;
    private bool _enCurso;

    /// <param name="origen">
    /// Controlador cuyo estado condiciona <paramref name="puedeEjecutar"/>. WPF solo vuelve a consultar CanExecute
    /// cuando el comando avisa; sin esto, un botón enlazado mientras la pantalla cargaba (Ocupado) quedaría
    /// deshabilitado para siempre.
    /// </param>
    public ComandoAsincrono(Func<Task> ejecutar, Func<bool>? puedeEjecutar = null, ControladorBase? origen = null)
    {
        _ejecutar = ejecutar;
        _puedeEjecutar = puedeEjecutar;
        if (origen is not null)
            origen.PropertyChanged += (_, e) =>
            {
                if (e.PropertyName == nameof(ControladorBase.Ocupado))
                    Notificar();
            };
    }

    public event EventHandler? CanExecuteChanged;

    public bool CanExecute(object? parametro) => !_enCurso && (_puedeEjecutar?.Invoke() ?? true);

    public async void Execute(object? parametro) => await EjecutarAsync();

    /// <summary>Lo que hace <see cref="Execute"/>, pero esperable (pruebas).</summary>
    public async Task EjecutarAsync()
    {
        if (!CanExecute(null))
            return;
        _enCurso = true;
        Notificar();
        try
        {
            await _ejecutar();
        }
        finally
        {
            _enCurso = false;
            Notificar();
        }
    }

    public void Notificar() => CanExecuteChanged?.Invoke(this, EventArgs.Empty);
}
