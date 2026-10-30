namespace ec.edu.monster.vista;

/// <summary>
/// Campo de texto de la interfaz (CLIENT_STYLES_DOTNET.md §5.3): el <c>Entry</c> de MAUI no tiene radio ni borde, así
/// que va dentro de un <c>Border</c>. <see cref="Invalido"/> marca el error con borde de 2 en <c>Error</c>.
/// </summary>
public partial class CampoTexto : ContentView
{
    public static readonly BindableProperty TextProperty = BindableProperty.Create(nameof(Text), typeof(string),
        typeof(CampoTexto), "", BindingMode.TwoWay);

    public static readonly BindableProperty PlaceholderProperty = BindableProperty.Create(nameof(Placeholder),
        typeof(string), typeof(CampoTexto), "");

    public static readonly BindableProperty EsClaveProperty = BindableProperty.Create(nameof(EsClave), typeof(bool),
        typeof(CampoTexto), false);

    public static readonly BindableProperty TecladoProperty = BindableProperty.Create(nameof(Teclado),
        typeof(Keyboard), typeof(CampoTexto), Keyboard.Default);

    public static readonly BindableProperty InvalidoProperty = BindableProperty.Create(nameof(Invalido), typeof(bool),
        typeof(CampoTexto), false, propertyChanged: (b, _, _) => ((CampoTexto)b).ActualizarBorde());

    private bool _enfocado;

    public CampoTexto()
    {
        InitializeComponent();
    }

    public string Text
    {
        get => (string)GetValue(TextProperty);
        set => SetValue(TextProperty, value);
    }

    public string Placeholder
    {
        get => (string)GetValue(PlaceholderProperty);
        set => SetValue(PlaceholderProperty, value);
    }

    public bool EsClave
    {
        get => (bool)GetValue(EsClaveProperty);
        set => SetValue(EsClaveProperty, value);
    }

    public Keyboard Teclado
    {
        get => (Keyboard)GetValue(TecladoProperty);
        set => SetValue(TecladoProperty, value);
    }

    public bool Invalido
    {
        get => (bool)GetValue(InvalidoProperty);
        set => SetValue(InvalidoProperty, value);
    }

    /// <summary>Lleva el foco al campo (campo vacío, error de credenciales).</summary>
    public void Enfocar() => Caja.Focus();

    /// <summary>Quita el foco y oculta el teclado, para que la zona de estado quede visible.</summary>
    public void Desenfocar() => Caja.Unfocus();

    private void AlEnfocar(object? sender, FocusEventArgs e)
    {
        _enfocado = true;
        ActualizarBorde();
    }

    private void AlDesenfocar(object? sender, FocusEventArgs e)
    {
        _enfocado = false;
        ActualizarBorde();
    }

    private void ActualizarBorde()
    {
        if (Invalido)
        {
            Marco.Stroke = (Color)Application.Current!.Resources["Error"];
            Marco.StrokeThickness = 2;
        }
        else if (_enfocado)
        {
            Marco.Stroke = (Color)Application.Current!.Resources["Acento"];
            Marco.StrokeThickness = 2;
        }
        else
        {
            Marco.StrokeThickness = 0;
        }
    }
}
