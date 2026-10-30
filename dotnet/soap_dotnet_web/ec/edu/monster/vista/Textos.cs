namespace ec.edu.monster.vista;

/// <summary>
/// Textos de pantalla. Los mismos valores en todos los clientes del proyecto (los define su docs/PROJECT.md);
/// aquí solo van los genéricos. Los del dominio (títulos, etiquetas, mensajes de validación) se agregan en cada proyecto.
/// El usuario ve siempre mensajes, nunca códigos.
/// </summary>
public static class Textos
{
    /// <summary>DOMINIO: nombre de la aplicación; título de la ventana.</summary>
    public const string NombreAplicacion = "Cliente SOAP";

    public const string Inicio = "Inicio";
    public const string Cancelar = "Cancelar";
    public const string Procesando = "Procesando…";
    public const string Consultando = "Consultando…";
    public const string ErrorInesperado = "Ocurrió un error inesperado, intente nuevamente";

    /// <summary><c>El campo {campo} es obligatorio</c>, con la etiqueta de pantalla en minúscula.</summary>
    public static string CampoObligatorio(string etiqueta) =>
        $"El campo {etiqueta.ToLowerInvariant()} es obligatorio";
}
