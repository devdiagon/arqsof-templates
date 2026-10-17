using ec.edu.monster.excepciones;

namespace ec.edu.monster.servicio;

/// <summary>
/// DOMINIO: marcador hasta conectar el servicio del dominio (<c>ServicioXxx : ServicioBase, IAutenticador</c>).
/// El acceso responde que falta conectar el dominio; se elimina al registrar el servicio real en <c>Program.cs</c>.
/// </summary>
public sealed class AutenticadorPendiente : IAutenticador
{
    public Task IniciarSesionAsync(string usuario, string contrasenia) =>
        throw new ExcepcionServicio("Falta conectar el servicio del dominio.", "ERR-500");
}
