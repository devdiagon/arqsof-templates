namespace ec.edu.monster.vista;

/// <summary>DOMINIO: pantalla de ejemplo; sirve de molde de una pantalla que se apila tras el acceso.</summary>
public partial class InicioVista : ContentPage
{
    private readonly Navegacion _navegacion;

    public InicioVista(Navegacion navegacion)
    {
        _navegacion = navegacion;
        InitializeComponent();
        Shell.SetBackButtonBehavior(this, new BackButtonBehavior { IsVisible = false });
        BindingContext = _navegacion.CrearInicio();
    }

    // Atrás no debe volver al acceso con la sesión abierta: se sale con «Cerrar sesión».
    protected override bool OnBackButtonPressed() => true;
}
