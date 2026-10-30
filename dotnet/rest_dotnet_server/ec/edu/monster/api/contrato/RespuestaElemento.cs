using System.Text.Json.Serialization;

namespace ec.edu.monster.api.contrato;

/// <summary>
/// EJEMPLO de respuesta con campos propios. Un campo propio no puede llamarse estado, codigo ni mensaje
/// (chocaría con la envoltura): por eso el código se publica como <c>codigoElemento</c>.
/// Los campos solo existen si hubo éxito: el serializador omite los nulos (<see cref="RespuestasHttp"/>).
/// </summary>
public class RespuestaElemento : RespuestaBase
{
    [JsonPropertyName("codigoElemento"), JsonPropertyOrder(1)] public string? CodigoElemento { get; private set; }
    [JsonPropertyName("nombre"), JsonPropertyOrder(2)] public string? Nombre { get; private set; }
    [JsonPropertyName("valor"), JsonPropertyOrder(3)] public string? Valor { get; private set; }

    public void CargarDatos(string codigoElemento, string nombre, string valor)
    {
        CodigoElemento = codigoElemento;
        Nombre = nombre;
        Valor = valor;
    }
}
