using System.Runtime.Serialization;

namespace ec.edu.monster.ws.contrato;

/// <summary>
/// EJEMPLO de contenedor de lista (R7): el elemento <c>elementos</c> con el elemento <c>elemento</c>
/// repetido dentro. Va aparte porque en .NET la lista con nombre de elemento propio es una clase.
/// </summary>
[CollectionDataContract(Name = "ListaElementos", ItemName = "elemento", Namespace = ConvencionSoap.Tns)]
public class ListaElementos : List<ElementoDetalle>
{
}
