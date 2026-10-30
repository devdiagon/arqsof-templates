namespace ec.edu.monster.api.contrato;

/// <summary>
/// Constantes del contrato REST (CONVENCION_REST.md), en un solo lugar.
/// </summary>
public static class ConvencionRest
{
    /// <summary>Prefijo común de la API (R1): la URL base es <c>http://&lt;host&gt;:&lt;puerto&gt;/api/</c>.</summary>
    public const string Prefijo = "api";

    /// <summary>Tipo de contenido de toda respuesta (R3).</summary>
    public const string JsonUtf8 = "application/json; charset=utf-8";

    /// <summary>Mensaje del DATA-001 por cuerpo ausente, mal formado o que no es un objeto JSON.</summary>
    public const string MensajeCuerpoInvalido = "Cuerpo de la petición inválido";
}
