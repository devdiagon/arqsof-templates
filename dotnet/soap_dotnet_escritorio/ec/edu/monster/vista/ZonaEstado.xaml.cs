using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

namespace ec.edu.monster.vista;

/// <summary>
/// Zona de estado de una pantalla (indicador de ocupado, error o éxito). El fondo de su panel de mensaje lo fija la
/// pantalla: <c>PanelSobreImagen</c> sobre la imagen de fondo o <c>Superficie</c> en el acceso.
/// </summary>
public partial class ZonaEstado : UserControl
{
    public static readonly DependencyProperty FondoProperty = DependencyProperty.Register(
        nameof(Fondo), typeof(Brush), typeof(ZonaEstado), new PropertyMetadata(null));

    public ZonaEstado()
    {
        InitializeComponent();
    }

    public Brush? Fondo
    {
        get => (Brush?)GetValue(FondoProperty);
        set => SetValue(FondoProperty, value);
    }
}
