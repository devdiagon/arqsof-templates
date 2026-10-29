using CoreWCF;
using ec.edu.monster.ws.contrato;

namespace ec.edu.monster.ws;

/// <summary>
/// EJEMPLO de contrato del servicio SOAP ServicioElemento. En CADA operación se conservan las declaraciones de
/// la convención: <c>OperationContract(Name)</c> (R5) y retorno llamado "respuesta" (R5). Los nombres de los
/// parámetros C# son los nombres XML (R3: calificados con el TNS por el DataContract). La SOAPAction con URI
/// (R4) es la que WCF genera por defecto.
/// </summary>
[ServiceContract(Name = ConvencionSoap.ServicioElemento, Namespace = ConvencionSoap.Tns)]
public interface IElementoWS
{
    [OperationContract(Name = "consultarElemento")]
    [return: MessageParameter(Name = ConvencionSoap.Retorno)]
    RespuestaElemento ConsultarElemento(string codigoElemento);

    [OperationContract(Name = "listarElementos")]
    [return: MessageParameter(Name = ConvencionSoap.Retorno)]
    RespuestaElementos ListarElementos();

    [OperationContract(Name = "registrarElemento")]
    [return: MessageParameter(Name = ConvencionSoap.Retorno)]
    RespuestaElemento RegistrarElemento(string codigoElemento, string nombre, string valor);
}
