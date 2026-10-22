using ec.edu.monster.api;
using ec.edu.monster.excepciones;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;
using Microsoft.Extensions.Logging;

namespace ec.edu.monster
{
    /// <summary>
    /// Punto de composición: registra la Sesion (una sola para toda la ejecución, app de un solo usuario), el
    /// HttpClient (uno de larga vida), el servicio y las pantallas. Conoce todas las capas, por eso no vive en vista/.
    /// Flujo de la plantilla: login → inicio (cerrar sesión o sesión vencida vuelven al login).
    /// </summary>
    public static class MauiProgram
    {
        public static MauiApp CreateMauiApp()
        {
            var builder = MauiApp.CreateBuilder();
            builder.UseMauiApp<App>();

            builder.Services.AddSingleton<Sesion>();

            // Un único HttpClient de larga vida para toda la aplicación (uno por petición agota los sockets).
            builder.Services.AddSingleton(_ => ClienteApi.CrearHttpClient(ConfiguracionApi.ObtenerUrlBase()));
            builder.Services.AddSingleton<IClienteApi, ClienteApi>();

            // DOMINIO: registrar el servicio del dominio (ServicioXxx : ServicioBase, IAutenticador) y quitar AutenticadorPendiente:
            //   builder.Services.AddSingleton<ServicioXxx>();
            //   builder.Services.AddSingleton<IAutenticador>(sp => sp.GetRequiredService<ServicioXxx>());
            builder.Services.AddSingleton<IAutenticador, AutenticadorPendiente>();

            builder.Services.AddSingleton<Navegacion>();
            builder.Services.AddTransient<LoginVista>();
            builder.Services.AddTransient<InicioVista>();   // DOMINIO: reemplazar por las pantallas del dominio

#if DEBUG
    		builder.Logging.AddDebug();
#endif

            return builder.Build();
        }

        /// <summary>Marcador mientras la plantilla no tiene dominio: explica qué falta en lugar de fallar en silencio.</summary>
        private sealed class AutenticadorPendiente : IAutenticador
        {
            public Task IniciarSesionAsync(string usuario, string contrasenia) =>
                throw new ExcepcionServicio(
                    "Plantilla sin dominio: cree los DTO y conecte el servicio de autenticación (README, sección 4).",
                    "ERR-500");
        }
    }
}
