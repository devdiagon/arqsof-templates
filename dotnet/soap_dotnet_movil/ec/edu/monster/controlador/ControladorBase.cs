using System.ComponentModel;
using System.Runtime.CompilerServices;
using ec.edu.monster.excepciones;
using ec.edu.monster.vista;

namespace ec.edu.monster.controlador;

/// <summary>Lo que muestra la zona de estado de una pantalla, de uno en uno.</summary>
public enum EstadoZona
{
    Ninguno,
    Ocupado,
    Error,
    Exito
}

/// <summary>
/// Molde de los controladores de pantalla (ARQUITECTURA_CLIENTE.md §4.6, familia con estado): hace de ViewModel de su
/// vista. La llamada al servicio se espera sin bloquear el hilo de la interfaz, la pantalla queda «ocupada» mientras
/// tanto, una segunda acción se ignora y los errores se muestran como mensaje (nunca el código). No conoce la pantalla
/// siguiente: la navegación llega como acciones.
/// </summary>
public abstract class ControladorBase : INotifyPropertyChanged
{
    private EstadoZona _estado = EstadoZona.Ninguno;
    private string? _mensaje;
    private string? _textoOcupado;

    public event PropertyChangedEventHandler? PropertyChanged;

    /// <summary>Pide a la vista que lleve el foco al control con ese nombre (campo vacío, error de credenciales).</summary>
    public event Action<string>? FocoSolicitado;

    // ---------------------------------------------------------------- estado que lee la vista

    public EstadoZona Estado
    {
        get => _estado;
        private set
        {
            if (Asignar(ref _estado, value))
            {
                OnPropertyChanged(nameof(Ocupado));
                OnPropertyChanged(nameof(NoOcupado));
            }
        }
    }

    /// <summary>Hay una llamada en curso: la vista deshabilita todos sus controles, incluido <c>Cancelar</c>.</summary>
    public bool Ocupado => _estado == EstadoZona.Ocupado;

    public bool NoOcupado => !Ocupado;

    public string? Mensaje
    {
        get => _mensaje;
        private set => Asignar(ref _mensaje, value);
    }

    public string? TextoOcupado
    {
        get => _textoOcupado;
        private set => Asignar(ref _textoOcupado, value);
    }

    // ---------------------------------------------------------------- ciclo de vida

    /// <summary>Se llama cada vez que se muestra la pantalla: debe dejarla limpia.</summary>
    public virtual void AlMostrar()
    {
        LimpiarMarcas();
        OcultarResultado();
        OcultarEstado();
    }

    /// <summary>Quita la marca de error de todos los campos.</summary>
    protected virtual void LimpiarMarcas()
    {
    }

    /// <summary>Lo que había de una acción anterior (resultado, tarjetas) se oculta al empezar otra.</summary>
    protected virtual void OcultarResultado()
    {
    }

    // ---------------------------------------------------------------- zona de estado

    public void MostrarError(string mensaje)
    {
        Mensaje = mensaje;
        TextoOcupado = null;
        Estado = EstadoZona.Error;
    }

    public void MostrarExito(string mensaje)
    {
        Mensaje = mensaje;
        TextoOcupado = null;
        Estado = EstadoZona.Exito;
    }

    public void OcultarEstado()
    {
        Mensaje = null;
        TextoOcupado = null;
        Estado = EstadoZona.Ninguno;
    }

    protected void PedirFoco(string campo) => FocoSolicitado?.Invoke(campo);

    // ---------------------------------------------------------------- llamadas al servicio

    protected static bool Vacio(string? valor) => string.IsNullOrWhiteSpace(valor);

    /// <summary>
    /// Espera <paramref name="tarea"/> con la pantalla ocupada. Si ya hay una llamada en curso no hace nada. El éxito y
    /// los errores se tratan de vuelta en el hilo de la interfaz, con la pantalla ya liberada.
    /// </summary>
    protected async Task EjecutarAsync<T>(string textoOcupado, Func<Task<T>> tarea, Action<T> alExito)
    {
        if (Ocupado)
        {
            return;
        }
        OcultarResultado();
        Mensaje = null;
        TextoOcupado = textoOcupado;
        Estado = EstadoZona.Ocupado;

        T resultado;
        try
        {
            resultado = await tarea();
        }
        catch (ExcepcionServicio e)
        {
            OcultarEstado();
            Manejar(e);
            return;
        }
        catch (Exception)
        {
            OcultarEstado();
            MostrarError(Textos.ErrorInesperado);
            return;
        }
        OcultarEstado();
        alExito(resultado);
    }

    /// <summary>
    /// Un error del servidor o de red se muestra con su mensaje, sin código. Un proyecto que defina una sesión puede
    /// sobrescribirlo para tratar ahí el código de sesión no válida (la plantilla no define ninguna).
    /// </summary>
    protected virtual void Manejar(ExcepcionServicio e) => MostrarError(e.Message);

    // ---------------------------------------------------------------- INotifyPropertyChanged

    protected bool Asignar<T>(ref T campo, T valor, [CallerMemberName] string? propiedad = null)
    {
        if (EqualityComparer<T>.Default.Equals(campo, valor))
        {
            return false;
        }
        campo = valor;
        OnPropertyChanged(propiedad);
        return true;
    }

    protected void OnPropertyChanged([CallerMemberName] string? propiedad = null) =>
        PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propiedad));
}
