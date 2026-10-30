namespace ec.edu.monster.vista;

/// <summary>
/// Página que aloja la pantalla actual. No tiene lógica de negocio: solo coloca la vista pedida, muestra u oculta la
/// imagen de fondo y la centra en vertical (la lista de movimientos va arriba, CLIENT_STYLES_DOTNET.md §7.0).
/// </summary>
public partial class PaginaPrincipal : ContentPage
{
    public PaginaPrincipal()
    {
        InitializeComponent();
        // Una pantalla corta se centra en vertical: la fila ocupa al menos el alto visible.
        Desplazamiento.SizeChanged += (_, _) => AjustarAlto();
    }

    public void Mostrar(Pantalla pantalla, View vista)
    {
        CapaImagen.IsVisible = !pantalla.EsAcceso();
        Contenedor.VerticalOptions = pantalla.VaArriba() ? LayoutOptions.Start : LayoutOptions.Center;
        Contenedor.Content = vista;
        Desplazamiento.ScrollToAsync(0, 0, false);
    }

    private void AjustarAlto()
    {
        if (Desplazamiento.Height > 0)
        {
            Marco.MinimumHeightRequest = Desplazamiento.Height;
        }
    }
}
