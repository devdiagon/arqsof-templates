using System.ComponentModel;
using ec.edu.monster.controlador;

namespace ec.edu.monster.vista;

/// <summary>
/// Zona de estado (CLIENT_STYLES_DOTNET.md §5.14). Se enlaza al controlador de la pantalla (su <c>BindingContext</c>):
/// muestra de uno en uno el indicador de ocupado, un error o un éxito, según <see cref="ControladorBase.Estado"/>.
/// <see cref="Fondo"/> es <c>Superficie</c> en el acceso y <c>PanelSobreImagen</c> en el resto (§6).
/// </summary>
public partial class ZonaEstado : ContentView
{
    public static readonly BindableProperty FondoProperty = BindableProperty.Create(nameof(Fondo), typeof(Color),
        typeof(ZonaEstado), null, propertyChanged: (b, _, _) => ((ZonaEstado)b).AplicarFondo());

    private ControladorBase? _controlador;

    public ZonaEstado()
    {
        InitializeComponent();
    }

    public Color? Fondo
    {
        get => (Color?)GetValue(FondoProperty);
        set => SetValue(FondoProperty, value);
    }

    protected override void OnBindingContextChanged()
    {
        base.OnBindingContextChanged();
        if (_controlador is not null)
        {
            _controlador.PropertyChanged -= AlCambiar;
        }
        _controlador = BindingContext as ControladorBase;
        if (_controlador is not null)
        {
            _controlador.PropertyChanged += AlCambiar;
        }
        Actualizar();
    }

    private void AlCambiar(object? sender, PropertyChangedEventArgs e)
    {
        if (e.PropertyName == nameof(ControladorBase.Estado))
        {
            Actualizar();
        }
    }

    private void Actualizar()
    {
        EstadoZona estado = _controlador?.Estado ?? EstadoZona.Ninguno;
        PanelOcupado.IsVisible = estado == EstadoZona.Ocupado;
        PanelMensaje.IsVisible = estado is EstadoZona.Error or EstadoZona.Exito;
        if (PanelMensaje.IsVisible)
        {
            Barra.Color = (Color)Application.Current!.Resources[estado == EstadoZona.Error ? "Error" : "Exito"];
        }
        IsVisible = estado != EstadoZona.Ninguno;
    }

    private void AplicarFondo()
    {
        if (Fondo is not null)
        {
            PanelOcupado.BackgroundColor = Fondo;
            PanelMensaje.BackgroundColor = Fondo;
        }
    }
}
