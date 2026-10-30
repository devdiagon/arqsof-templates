using ec.edu.monster.controlador;
using ec.edu.monster.vista;

namespace ec.edu.monster
{
    /// <summary>
    /// Punto de arranque y de composición: crea los clientes de los servicios, los servicios, los controladores y las
    /// vistas, y decide la navegación (los controladores avisan qué ocurrió; aquí se decide a dónde ir). Conoce todas las
    /// capas, por eso no vive en vista/.
    /// </summary>
    public partial class App : Application
    {
        private PaginaPrincipal _pagina = null!;
        private readonly Dictionary<Pantalla, (View Vista, ControladorBase Controlador)> _pantallas = new();

        public App()
        {
            InitializeComponent();
            // Presentación oscura fija: no hay selector de tema (docs/CLIENT_STYLES_DOTNET.md §1, §7.1).
            UserAppTheme = AppTheme.Dark;
        }

        protected override Window CreateWindow(IActivationState? activationState)
        {
            _pagina = new PaginaPrincipal();
            Componer();
            return new Window(_pagina);
        }

        private void Componer()
        {
            // TODO (README §4, paso 7): crear los clientes (XClienteApi.Crear()), los servicios, los controladores y las
            // vistas del dominio; registrarlos en _pantallas y mostrar la primera con Ir(...). Con la plantilla sin
            // tocar la página queda vacía, sobre el color de fondo.
            // Si el proyecto tiene login, la sesión (modelo) y su manejo en los controladores se definen aquí.
        }

        /// <summary>Deja limpia la pantalla pedida (su controlador) y la muestra.</summary>
        private void Ir(Pantalla pantalla)
        {
            (View vista, ControladorBase controlador) = _pantallas[pantalla];
            controlador.AlMostrar();
            _pagina.Mostrar(pantalla, vista);
        }
    }
}
