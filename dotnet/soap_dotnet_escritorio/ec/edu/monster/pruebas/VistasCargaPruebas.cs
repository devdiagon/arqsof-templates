using System.Windows;
using System.Windows.Controls;
using ec.edu.monster.controlador;
using ec.edu.monster.vista;

namespace ec.edu.monster.pruebas;

/// <summary>
/// Humo de la capa de vista: instancia cada pantalla con los estilos reales. Detecta recursos faltantes o repetidos,
/// plantillas inválidas y enlaces con tipos equivocados, que solo fallan al cargar el XAML. No prueba el aspecto.
/// Cada pantalla nueva del dominio agrega aquí su prueba.
/// </summary>
public class VistasCargaPruebas
{
    private static void EnHiloSta(Action accion)
    {
        Exception? error = null;
        var hilo = new Thread(() =>
        {
            try { accion(); }
            catch (Exception ex) { error = ex; }
        });
        hilo.SetApartmentState(ApartmentState.STA);
        hilo.Start();
        hilo.Join();
        if (error is not null)
            throw new Xunit.Sdk.XunitException($"Fallo al cargar la vista: {error}");
    }

    private static void Cargar(Func<(ControladorBase Controlador, UserControl Vista)> crear)
    {
        EnHiloSta(() =>
        {
            var app = Application.Current ?? new App();
            if (app.Resources.MergedDictionaries.Count == 0)
                ((App)app).InitializeComponent();

            var (controlador, vista) = crear();
            vista.DataContext = controlador;

            var ventana = new Window { Content = vista, Width = 600, Height = 700, ShowInTaskbar = false };
            ventana.Style = (Style)app.FindResource("VentanaBase");
            ventana.Show();
            vista.UpdateLayout();
            ventana.Close();
        });
    }

    [Fact]
    public void LoginVista_carga() =>
        Cargar(() => (new LoginControlador(new AutenticadorFalso(), () => { }, "Mensaje"), new LoginVista()));

    [Fact]
    public void InicioVista_carga() =>
        Cargar(() => (new InicioControlador(() => { }), new InicioVista()));
}
