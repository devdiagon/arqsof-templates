namespace ec.edu.monster.vista;

public partial class LoginVista : ContentPage
{
    private readonly Navegacion _navegacion;

    public LoginVista(Navegacion navegacion)
    {
        _navegacion = navegacion;
        InitializeComponent();
    }

    // Cada vez que se muestra el acceso empieza limpio (tras cerrar sesión o expirar el token), con el aviso pendiente.
    // La contraseña no se enlaza: la vista entrega al controlador cómo leerla y este no la guarda.
    protected override void OnAppearing()
    {
        base.OnAppearing();
        Contrasenia.Text = string.Empty;
        var controlador = _navegacion.CrearLogin();
        controlador.LeerContrasenia = () => Contrasenia.Text ?? string.Empty;
        BindingContext = controlador;
    }
}
