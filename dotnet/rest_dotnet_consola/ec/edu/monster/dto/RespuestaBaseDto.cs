using System.Text.Json.Serialization;

namespace ec.edu.monster.dto;

/// <summary>
/// Forma común de toda respuesta de la familia (éxito o error). En los errores de plataforma (ruta inexistente,
/// método no permitido, JSON mal formado, error interno) es lo único que llega. Cada operación del dominio hereda
/// de esta clase y añade sus campos como <c>string?</c> o <c>List&lt;string&gt;?</c> (pueden llegar <c>null</c>).
/// </summary>
public class RespuestaBaseDto
{
    [JsonPropertyName("estado")]
    public string? Estado { get; set; }

    [JsonPropertyName("codigo")]
    public string? Codigo { get; set; }

    [JsonPropertyName("mensaje")]
    public string? Mensaje { get; set; }
}
