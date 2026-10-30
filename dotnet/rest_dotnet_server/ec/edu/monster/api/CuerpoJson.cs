using System.Text.Json;
using ec.edu.monster.api.contrato;
using ec.edu.monster.excepciones;

namespace ec.edu.monster.api;

/// <summary>
/// Cuerpo JSON de una petición, leído como texto. Los nombres de campo se buscan sin distinguir
/// mayúsculas y los desconocidos se ignoran (R4). Un campo ausente o nulo se entrega como <c>null</c>,
/// para que el validador del controlador diga «es obligatorio».
/// Se lee con <see cref="JsonDocument"/> y no con un POCO: así un número JSON llega como su texto (R9) en vez de
/// fallar el enlace de modelo, y un cuerpo mal formado se convierte en DATA-001 con la envoltura.
/// </summary>
public sealed class CuerpoJson
{
    private readonly Dictionary<string, JsonElement> _campos;

    private CuerpoJson(Dictionary<string, JsonElement> campos)
    {
        _campos = campos;
    }

    /// <exception cref="ExcepcionMonster">DATA-001 si el cuerpo está vacío, no es JSON válido o no es un objeto.</exception>
    public static CuerpoJson Leer(string? cuerpo)
    {
        if (string.IsNullOrWhiteSpace(cuerpo))
        {
            throw CuerpoInvalido();
        }
        try
        {
            using JsonDocument documento = JsonDocument.Parse(cuerpo);
            if (documento.RootElement.ValueKind != JsonValueKind.Object)
            {
                throw CuerpoInvalido();
            }
            var campos = new Dictionary<string, JsonElement>(StringComparer.OrdinalIgnoreCase);
            foreach (JsonProperty propiedad in documento.RootElement.EnumerateObject())
            {
                campos[propiedad.Name] = propiedad.Value.Clone();
            }
            return new CuerpoJson(campos);
        }
        catch (JsonException)
        {
            throw CuerpoInvalido();
        }
    }

    /// <summary>
    /// Valor del campo como texto. Un texto JSON se entrega tal cual; un número o booleano, con su
    /// representación escrita (el validador decide); un objeto, una lista o un nulo, como <c>null</c>.
    /// </summary>
    public string? Texto(string campo)
    {
        if (!_campos.TryGetValue(campo, out JsonElement valor))
        {
            return null;
        }
        return valor.ValueKind switch
        {
            JsonValueKind.String => valor.GetString(),
            JsonValueKind.Number or JsonValueKind.True or JsonValueKind.False => valor.GetRawText(),
            _ => null
        };
    }

    private static ExcepcionMonster CuerpoInvalido() =>
        new(CodigoError.DATA_001, ConvencionRest.MensajeCuerpoInvalido);
}
