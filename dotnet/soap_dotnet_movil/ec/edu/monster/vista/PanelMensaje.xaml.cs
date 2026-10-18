namespace ec.edu.monster.vista;

public partial class PanelMensaje : ContentView
{
    public static readonly BindableProperty TextoProperty =
        BindableProperty.Create(nameof(Texto), typeof(string), typeof(PanelMensaje), default(string));

    public PanelMensaje()
    {
        InitializeComponent();
    }

    public string? Texto
    {
        get => (string?)GetValue(TextoProperty);
        set => SetValue(TextoProperty, value);
    }
}
