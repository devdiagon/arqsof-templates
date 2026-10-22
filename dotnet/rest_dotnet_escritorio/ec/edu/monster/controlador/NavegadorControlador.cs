namespace ec.edu.monster.controlador;

/// <summary>
/// Pantalla que se muestra en la zona de contenido de la ventana. Cada pantalla es un controlador (ViewModel);
/// la vista que le corresponde se asocia con un DataTemplate. Los controladores no se conocen entre sí:
/// piden el cambio de pantalla a través de este navegador.
/// </summary>
public class NavegadorControlador : ControladorBase
{
    private ControladorBase? _actual;

    /// <summary>Pantalla visible; null mientras no se ha mostrado ninguna.</summary>
    public ControladorBase? Actual
    {
        get => _actual;
        private set => Asignar(ref _actual, value);
    }

    public void Mostrar(ControladorBase pantalla) => Actual = pantalla;
}
