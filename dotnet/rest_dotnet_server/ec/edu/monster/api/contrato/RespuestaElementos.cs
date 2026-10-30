using System.Text.Json.Serialization;

namespace ec.edu.monster.api.contrato;

/// <summary>
/// EJEMPLO de respuesta con lista (R5): la lista se inicializa vacía, de modo que en caso de error llega
/// <c>[]</c>, nunca ausente.
/// </summary>
public class RespuestaElementos : RespuestaBase
{
    [JsonPropertyName("elementos"), JsonPropertyOrder(1)]
    public List<ElementoDetalle> Elementos { get; private set; } = [];
}
