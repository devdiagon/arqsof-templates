using System.Runtime.Serialization;

namespace ec.edu.monster.ws.contrato;

/// <summary>
/// EJEMPLO de respuesta con lista (R7): contenedor con el elemento repetido dentro. La lista se inicializa vacía:
/// en caso de error llega vacía, nunca ausente.
/// </summary>
[DataContract(Name = "RespuestaElementos", Namespace = ConvencionSoap.Tns)]
public class RespuestaElementos : RespuestaBase
{
    [DataMember(Name = "elementos", Order = 0)]
    public ListaElementos Elementos { get; private set; } = [];
}
