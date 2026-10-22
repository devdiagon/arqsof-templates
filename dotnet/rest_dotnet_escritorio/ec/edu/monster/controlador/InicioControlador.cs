namespace ec.edu.monster.controlador;

/// <summary>
/// DOMINIO: pantalla de ejemplo que se muestra tras el acceso. Se reemplaza por las pantallas del dominio;
/// sirve de molde mínimo (un controlador por pantalla, la navegación llega como delegado).
/// </summary>
public class InicioControlador : ControladorBase
{
    public InicioControlador(Action alCerrarSesion)
    {
        CerrarSesion = new Comando(alCerrarSesion);
    }

    public Comando CerrarSesion { get; }
}
