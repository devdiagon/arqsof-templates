using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

/// <summary>Doble escrito a mano de <see cref="IAutenticador"/>: registra las llamadas y ejecuta la conducta indicada.</summary>
public class AutenticadorFalso : IAutenticador
{
    public List<(string Usuario, string Contrasenia)> Llamadas { get; } = [];

    /// <summary>Conducta por defecto: acceso correcto. Se reemplaza por una que lance para simular errores.</summary>
    public Func<Task> Conducta { get; set; } = () => Task.CompletedTask;

    public Task IniciarSesionAsync(string usuario, string contrasenia)
    {
        Llamadas.Add((usuario, contrasenia));
        return Conducta();
    }
}
