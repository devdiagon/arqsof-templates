using System.Windows;
using System.Windows.Controls;
using ec.edu.monster.controlador;
using ec.edu.monster.vista;

namespace ec.edu.monster;

/// <summary>
/// Punto de arranque y de composición: crea los clientes de los servicios, los servicios, los controladores y las vistas,
/// y decide la navegación entre pantallas (los controladores avisan qué ocurrió; aquí se decide a dónde ir). Es el
/// equivalente del <c>Main</c> de la plantilla Java.
/// </summary>
public partial class App : Application
{
    private VentanaPrincipal _ventana = null!;
    private readonly Dictionary<Pantalla, (UserControl Vista, ControladorBase? Controlador)> _pantallas = new();

    protected override void OnStartup(StartupEventArgs e)
    {
        base.OnStartup(e);
        try
        {
            Componer();
            _ventana.Show();
        }
        catch (Exception)
        {
            MessageBox.Show(Textos.ErrorInesperado, Textos.NombreAplicacion, MessageBoxButton.OK, MessageBoxImage.Error);
            Shutdown(1);
        }
    }

    private void Componer()
    {
        _ventana = new VentanaPrincipal();

        // TODO (DOMINIO, README §4): crear el cliente REST de cada recurso
        //         IXCliente cliente = XClienteApi.Crear();
        //       los servicios (reciben el cliente), los controladores (reciben el servicio y la navegación como acciones)
        //       y las vistas (reciben su controlador). Registrar cada pantalla:
        //         _pantallas[Pantalla.X] = (new XVista(controlador), controlador);
        //       y navegar con Ir(Pantalla.X) desde las acciones que los controladores reciben.
        _pantallas[Pantalla.Inicio] = (new InicioVista(), null);

        Ir(Pantalla.Inicio);
    }

    /// <summary>Deja limpia la pantalla pedida (su controlador, si tiene) y la muestra en el modo que le corresponde.</summary>
    private void Ir(Pantalla pantalla)
    {
        (UserControl vista, ControladorBase? controlador) = _pantallas[pantalla];
        controlador?.AlMostrar();
        _ventana.Mostrar(pantalla, vista);
    }
}
