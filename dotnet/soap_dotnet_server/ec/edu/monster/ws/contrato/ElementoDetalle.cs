using System.Runtime.Serialization;

namespace ec.edu.monster.ws.contrato;

/// <summary>
/// EJEMPLO de elemento anidado en una lista (R7): todos los campos son texto.
/// </summary>
[DataContract(Name = "ElementoDetalle", Namespace = ConvencionSoap.Tns)]
public class ElementoDetalle
{
    [DataMember(Name = "codigoElemento", Order = 0, EmitDefaultValue = false)] public string? CodigoElemento { get; private set; }
    [DataMember(Name = "nombre", Order = 1, EmitDefaultValue = false)] public string? Nombre { get; private set; }
    [DataMember(Name = "valor", Order = 2, EmitDefaultValue = false)] public string? Valor { get; private set; }

    public ElementoDetalle(string codigoElemento, string nombre, string valor)
    {
        CodigoElemento = codigoElemento;
        Nombre = nombre;
        Valor = valor;
    }
}
