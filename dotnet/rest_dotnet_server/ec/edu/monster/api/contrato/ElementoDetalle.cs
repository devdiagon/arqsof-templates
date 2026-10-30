using System.Text.Json.Serialization;

namespace ec.edu.monster.api.contrato;

/// <summary>
/// EJEMPLO de elemento anidado en una lista: todos los campos son texto (R9).
/// </summary>
public class ElementoDetalle
{
    [JsonPropertyName("codigoElemento"), JsonPropertyOrder(1)] public string? CodigoElemento { get; private set; }
    [JsonPropertyName("nombre"), JsonPropertyOrder(2)] public string? Nombre { get; private set; }
    [JsonPropertyName("valor"), JsonPropertyOrder(3)] public string? Valor { get; private set; }

    public ElementoDetalle(string codigoElemento, string nombre, string valor)
    {
        CodigoElemento = codigoElemento;
        Nombre = nombre;
        Valor = valor;
    }
}
