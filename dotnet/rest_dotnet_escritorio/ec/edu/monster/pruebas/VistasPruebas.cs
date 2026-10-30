using System.Windows;
using System.Windows.Controls;
using System.Windows.Threading;
using ec.edu.monster.controlador;
using ec.edu.monster.vista;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>
/// Humo de la interfaz: carga los recursos de <c>estilos/</c>, la ventana en sus dos modos, la pantalla inicial y los
/// componentes de la plantilla, sin red. Los errores de XAML (un recurso de otro tipo, una clave que no existe) solo
/// aparecen al cargar y el compilador no los ve; esta prueba los detecta. No comprueba el aspecto: eso se mira a mano.
/// </summary>
public class VistasPruebas
{
    private sealed class ControladorDePrueba : ControladorBase
    {
    }

    private static void Vaciar() =>
        Dispatcher.CurrentDispatcher.Invoke(() => { }, DispatcherPriority.ContextIdle);

    [Fact]
    public void LaVentanaLosEstilosYLosComponentesSeConstruyenSinErroresDeXaml()
    {
        Exception? error = null;
        var hilo = new Thread(() =>
        {
            try
            {
                var app = new App();
                app.InitializeComponent();   // carga estilos/tema.xaml y estilos/controles.xaml

                var ventana = new VentanaPrincipal();
                ventana.Mostrar(Pantalla.Login, new InicioVista());       // modo acceso (dividido)
                ventana.Mostrar(Pantalla.Inicio, new InicioVista());      // modo aplicación (ancho completo)
                ventana.Measure(new Size(1100, 720));
                ventana.Arrange(new Rect(0, 0, 1100, 720));
                ventana.UpdateLayout();
                Assert.Equal(Textos.NombreAplicacion, ventana.Title);

                // Los campos y los botones miden lo que fija la guía (36): el relleno no se repite en la plantilla.
                var campo = new TextBox { Style = (Style)app.FindResource("CajaTexto") };
                var clave = new PasswordBox { Style = (Style)app.FindResource("CajaContrasenia") };
                var primario = new Button { Content = "Aceptar", Style = (Style)app.FindResource("BotonPrimario") };
                var secundario = new Button { Content = "Cancelar", Style = (Style)app.FindResource("BotonSecundario") };
                var cuadricula = new Button { Content = "Operación", Style = (Style)app.FindResource("BotonCuadricula") };
                foreach (FrameworkElement control in new FrameworkElement[] { campo, clave, primario, secundario })
                {
                    control.Measure(new Size(300, 1000));
                    Assert.Equal(36, Math.Round(control.DesiredSize.Height));
                }
                cuadricula.Measure(new Size(300, 1000));
                Assert.Equal(96 + 16, Math.Round(cuadricula.DesiredSize.Height));   // 96 de alto + Margin 8 arriba y abajo

                // Zona de estado: oculta sin nada que decir, visible con un mensaje.
                var controlador = new ControladorDePrueba();
                var zona = new ZonaEstado { DataContext = controlador };
                zona.Measure(new Size(400, 1000));
                Assert.Equal(0, zona.DesiredSize.Height);
                controlador.MostrarError("Un mensaje");
                Vaciar();   // los cambios de estado llegan a los enlaces por la cola del despachador
                zona.Measure(new Size(400, 1000));
                Assert.True(zona.DesiredSize.Height > 0);

                // Tarjeta de resultado: 260 de ancho (más el margen de 8 a cada lado).
                var tarjeta = new TarjetaResultado { DataContext = new DatosTarjeta("Título", true, "+$1.00", "$1.00 → $2.00", "Pie") };
                tarjeta.Measure(new Size(1000, 1000));
                Assert.Equal(260 + 16, Math.Round(tarjeta.DesiredSize.Width));
            }
            catch (Exception e)
            {
                error = e;
            }
        });
        hilo.SetApartmentState(ApartmentState.STA);
        hilo.Start();
        hilo.Join();

        Assert.True(error is null, error?.ToString());
    }
}
