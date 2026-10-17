using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace ec.edu.monster.controlador;

/// <summary>
/// Base de los controladores (ViewModels en MVVM): notificación de cambios y estado común de ocupado/mensaje.
/// No conoce <c>ws</c> ni los tipos generados: solo habla con los servicios.
/// </summary>
public abstract class ControladorBase : INotifyPropertyChanged
{
    private bool _ocupado;
    private string? _mensaje;

    public event PropertyChangedEventHandler? PropertyChanged;

    /// <summary>Mientras es true, la vista deshabilita los controles y muestra el indicador de progreso.</summary>
    public bool Ocupado
    {
        get => _ocupado;
        protected set => Asignar(ref _ocupado, value);
    }

    /// <summary>Mensaje de error o validación para la vista: solo el mensaje, nunca el código.</summary>
    public string? Mensaje
    {
        get => _mensaje;
        protected set
        {
            if (Asignar(ref _mensaje, value))
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(HayMensaje)));
        }
    }

    /// <summary>Para mostrar u ocultar el panel de mensaje (no se dibuja vacío).</summary>
    public bool HayMensaje => !string.IsNullOrEmpty(_mensaje);

    protected bool Asignar<T>(ref T campo, T valor, [CallerMemberName] string? propiedad = null)
    {
        if (EqualityComparer<T>.Default.Equals(campo, valor))
            return false;
        campo = valor;
        OnPropertyChanged(propiedad);
        return true;
    }

    protected void OnPropertyChanged([CallerMemberName] string? propiedad = null) =>
        PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propiedad));
}
