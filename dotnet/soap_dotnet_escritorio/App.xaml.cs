using System.Windows;
using ec.edu.monster.controlador;
using ec.edu.monster.excepciones;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;

namespace ec.edu.monster;

/// <summary>
/// Punto de composición: crea la Sesion (una sola para toda la ejecución), el servicio y los controladores
/// (ViewModels), y decide el orden de las pantallas. Conoce todas las capas, por eso no vive en vista/.
/// Flujo de la plantilla: login → inicio (cerrar sesión o sesión vencida vuelven al login).
/// </summary>
public partial class App : Application
{
    private readonly Sesion _sesion = new();
    private NavegadorControlador _navegador = null!;
    private IAutenticador _autenticador = null!;

    protected override void OnStartup(StartupEventArgs e)
    {
        base.OnStartup(e);

        // DOMINIO: crear el cliente generado y el servicio del dominio, p. ej.
        //   var servicio = new ServicioXxx(new ClienteWSXxx(), _sesion);   // ServicioXxx : ServicioBase, IAutenticador
        //   _autenticador = servicio;
        _autenticador = new AutenticadorPendiente();

        _navegador = new NavegadorControlador();
        MostrarLogin();
        new VentanaPrincipal { DataContext = _navegador }.Show();
    }

    private void MostrarLogin(string? mensaje = null) =>
        _navegador.Mostrar(new LoginControlador(_autenticador, MostrarInicio, mensaje));

    // DOMINIO: reemplazar por la pantalla inicial del dominio. Una pantalla que carga datos al abrirse hace
    //   _navegador.Mostrar(pantalla); _ = pantalla.CargarAsync();   (CargarAsync no lanza: deja el fallo en Mensaje)
    // y avisa la sesión vencida (ExigeLogin) con MostrarLogin(mensaje).
    private void MostrarInicio() =>
        _navegador.Mostrar(new InicioControlador(alCerrarSesion: () =>
        {
            _sesion.Token = null;
            MostrarLogin();
        }));

    /// <summary>Marcador mientras la plantilla no tiene dominio: explica qué falta en lugar de fallar en silencio.</summary>
    private sealed class AutenticadorPendiente : IAutenticador
    {
        public Task IniciarSesionAsync(string usuario, string contrasenia) =>
            throw new ExcepcionServicio(
                "Plantilla sin dominio: genere el proxy y conecte el servicio de autenticación (README, sección 4).",
                "ERR-500");
    }
}
