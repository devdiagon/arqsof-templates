namespace ec.edu.monster.servicio;

/// <summary>
/// Lo único que la pantalla de acceso necesita del servicio del dominio. El servicio del dominio
/// (<c>ServicioXxx : ServicioBase, IAutenticador</c>) la implementa llamando a <c>login</c> del proxy generado,
/// guardando el token en la <c>Sesion</c> y lanzando <c>ExcepcionServicio</c> si el servidor responde ERROR.
/// Así el login es común a todo dominio y se prueba sin servidor.
/// </summary>
public interface IAutenticador
{
    Task IniciarSesionAsync(string usuario, string contrasenia);
}
