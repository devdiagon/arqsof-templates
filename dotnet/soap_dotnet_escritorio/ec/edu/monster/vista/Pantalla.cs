namespace ec.edu.monster.vista;

/// <summary>
/// Pantallas que la ventana principal muestra. El acceso usa la disposición dividida (la pantalla a la izquierda y la
/// imagen a la derecha); las demás, el ancho completo sobre la imagen de fondo (docs/CLIENT_STYLES_DOTNET.md §7.0).
///
/// DOMINIO: agregar aquí una constante por pantalla nueva y, si no es un formulario de 480, su ancho y su alineación en
/// <see cref="PantallaExtensiones"/>.
/// </summary>
public enum Pantalla
{
    /// <summary>Pantalla de acceso, para los proyectos que definen autenticación (la plantilla no trae vista).</summary>
    Login,

    /// <summary>Pantalla inicial de la plantilla (punto de enganche: el proyecto la reemplaza por su login o su menú).</summary>
    Inicio
}

public static class PantallaExtensiones
{
    /// <summary>Solo el acceso usa la disposición dividida.</summary>
    public static bool EsAcceso(this Pantalla pantalla) => pantalla == Pantalla.Login;

    /// <summary>
    /// Una pantalla con una tabla va arriba de la ventana, ocupa su alto y no se desplaza: solo la tabla lo hace, por
    /// dentro (§5.13, §7.3). Las demás se centran en vertical. DOMINIO: agregar aquí las pantallas con tabla.
    /// </summary>
    public static bool VaArriba(this Pantalla pantalla) => false;

    /// <summary>
    /// Ancho máximo del contenido centrado: 420 en el acceso, 480 en formularios, 640 en el menú o en formularios con dos
    /// campos en fila y infinito (todo el ancho disponible) en una tabla (§7.0). DOMINIO: completar por pantalla.
    /// </summary>
    public static double AnchoContenido(this Pantalla pantalla) => pantalla switch
    {
        Pantalla.Login => 420,
        _ => 480
    };
}
