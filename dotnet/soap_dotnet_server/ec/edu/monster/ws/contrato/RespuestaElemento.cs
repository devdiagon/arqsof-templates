using System.Runtime.Serialization;

namespace ec.edu.monster.ws.contrato;

/// <summary>
/// EJEMPLO de respuesta con campos propios. Un campo propio no puede llamarse estado, codigo ni mensaje
/// (chocaría con la envoltura): por eso el código se publica como <c>codigoElemento</c>.
/// Los campos solo existen si hubo éxito; <c>EmitDefaultValue = false</c> omite los nulos.
/// </summary>
[DataContract(Name = "RespuestaElemento", Namespace = ConvencionSoap.Tns)]
public class RespuestaElemento : RespuestaBase
{
    [DataMember(Name = "codigoElemento", Order = 0, EmitDefaultValue = false)] public string? CodigoElemento { get; private set; }
    [DataMember(Name = "nombre", Order = 1, EmitDefaultValue = false)] public string? Nombre { get; private set; }
    [DataMember(Name = "valor", Order = 2, EmitDefaultValue = false)] public string? Valor { get; private set; }

    public void CargarDatos(string codigoElemento, string nombre, string valor)
    {
        CodigoElemento = codigoElemento;
        Nombre = nombre;
        Valor = valor;
    }
}
