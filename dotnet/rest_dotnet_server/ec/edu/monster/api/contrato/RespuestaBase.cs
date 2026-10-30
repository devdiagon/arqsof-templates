using System.Text.Json.Serialization;

namespace ec.edu.monster.api.contrato;

/// <summary>
/// Envoltura común de toda respuesta, de éxito o de error (R5). Va primero en el JSON: <c>System.Text.Json</c>
/// ordena por <c>JsonPropertyOrder</c>, así que la envoltura usa órdenes negativos y los tipos de cada operación
/// numeran sus campos propios desde 1. Los nombres se fijan con <c>JsonPropertyName</c> (R4), no con una política.
/// </summary>
public class RespuestaBase
{
    [JsonPropertyName("estado"), JsonPropertyOrder(-3)] public string? Estado { get; private set; }
    [JsonPropertyName("codigo"), JsonPropertyOrder(-2)] public string? Codigo { get; private set; }
    [JsonPropertyName("mensaje"), JsonPropertyOrder(-1)] public string? Mensaje { get; private set; }

    public void AplicarEnvoltura(string estado, string codigo, string mensaje)
    {
        Estado = estado;
        Codigo = codigo;
        Mensaje = mensaje;
    }
}
