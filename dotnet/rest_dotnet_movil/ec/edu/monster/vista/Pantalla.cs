namespace ec.edu.monster.vista;

/// <summary>
/// Pantallas que la página principal muestra, una a la vez. CAMBIAR: un miembro por pantalla del dominio (la plantilla no
/// trae ninguna).
/// </summary>
public enum Pantalla
{
    Inicio
}

public static class PantallaExtensiones
{
    /// <summary>
    /// Solo la pantalla de acceso (si el proyecto la tiene) lleva la imagen como cabecera de su formulario; en las demás la
    /// imagen es el fondo fijo de la página (docs/CLIENT_STYLES_DOTNET.md §6). CAMBIAR: devolver verdadero para ella.
    /// </summary>
    public static bool EsAcceso(this Pantalla pantalla) => false;

    /// <summary>Una pantalla con lista de registros se ubica arriba; el resto se centran en vertical (§7.0). CAMBIAR si hace falta.</summary>
    public static bool VaArriba(this Pantalla pantalla) => false;
}
