namespace ec.edu.monster.vista;

/// <summary>
/// Textos de pantalla. Los mismos valores en todos los clientes del proyecto (los define su docs/PROJECT.md);
/// aquí solo van los genéricos. Los del dominio (títulos, etiquetas, mensajes de validación) se agregan en cada proyecto.
/// El usuario ve siempre mensajes, nunca códigos.
/// </summary>
public static class Textos
{
    /// <summary>
    /// Arte ASCII que se muestra una sola vez al iniciar la aplicación. Vacío por defecto: no se muestra nada.
    /// Si se define con un literal de texto sin formato (<c>"""</c>), normalizar los saltos de línea con
    /// <c>.ReplaceLineEndings("\n")</c> para que la salida no dependa del fin de línea del archivo fuente.
    /// </summary>
    public static readonly string Portada = "";

    public const string Opcion = "Opción";
    public const string OpcionInvalida = "Opción inválida";
    public const string Cancelar = "Cancelar";
    public const string PulseEnter = "Presione Enter para continuar";
    public const string Procesando = "Procesando…";
    public const string Consultando = "Consultando…";
    public const string ErrorInesperado = "Ocurrió un error inesperado, intente nuevamente";

    /// <summary><c>El campo {campo} es obligatorio</c>, con la etiqueta de pantalla en minúscula.</summary>
    public static string CampoObligatorio(string etiqueta) =>
        $"El campo {etiqueta.ToLowerInvariant()} es obligatorio";
}
