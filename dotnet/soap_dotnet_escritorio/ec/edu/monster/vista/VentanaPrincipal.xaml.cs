using System.Windows;
using System.Windows.Controls;

namespace ec.edu.monster.vista;

/// <summary>
/// Ventana única de la aplicación con dos modos: acceso (dividido) y aplicación (ancho completo sobre la imagen de
/// fondo). Solo coloca la pantalla que le entregan; no conoce controladores ni servicios.
/// </summary>
public partial class VentanaPrincipal : Window
{
    public VentanaPrincipal()
    {
        InitializeComponent();
        Title = Textos.NombreAplicacion;
    }

    /// <summary>Muestra <paramref name="contenido"/> en el modo que corresponde a <paramref name="pantalla"/>.</summary>
    public void Mostrar(Pantalla pantalla, UIElement contenido)
    {
        bool acceso = pantalla.EsAcceso();
        Acceso.Visibility = acceso ? Visibility.Visible : Visibility.Collapsed;
        Aplicacion.Visibility = acceso ? Visibility.Collapsed : Visibility.Visible;

        if (acceso)
        {
            ContenidoAcceso.Content = contenido;
            DesplazamientoAcceso.ScrollToTop();
        }
        else
        {
            ContenidoAplicacion.MaxWidth = pantalla.AnchoContenido();
            // Arriba y a todo el alto, sin desplazamiento externo: la pantalla reparte el alto y solo su tabla se desplaza.
            // Las demás se centran en vertical y solo se desplazan si no caben.
            bool arriba = pantalla.VaArriba();
            ContenidoAplicacion.VerticalAlignment = arriba ? VerticalAlignment.Stretch : VerticalAlignment.Center;
            DesplazamientoAplicacion.VerticalScrollBarVisibility =
                arriba ? ScrollBarVisibility.Disabled : ScrollBarVisibility.Auto;
            ContenidoAplicacion.Content = contenido;
            DesplazamientoAplicacion.ScrollToTop();
        }
    }
}
