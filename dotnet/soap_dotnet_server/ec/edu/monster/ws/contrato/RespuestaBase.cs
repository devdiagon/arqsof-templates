using System.Runtime.Serialization;

namespace ec.edu.monster.ws.contrato;

/// <summary>
/// Envoltura común de toda respuesta, de éxito o de error (R6). Los tipos de cada operación la
/// extienden y agregan sus campos propios después (en un DataContract la base se escribe primero).
/// </summary>
[DataContract(Name = "RespuestaBase", Namespace = ConvencionSoap.Tns)]
public class RespuestaBase
{
    [DataMember(Name = "estado", Order = 0)] public string? Estado { get; private set; }
    [DataMember(Name = "codigo", Order = 1)] public string? Codigo { get; private set; }
    [DataMember(Name = "mensaje", Order = 2)] public string? Mensaje { get; private set; }

    public void AplicarEnvoltura(string estado, string codigo, string mensaje)
    {
        Estado = estado;
        Codigo = codigo;
        Mensaje = mensaje;
    }
}
