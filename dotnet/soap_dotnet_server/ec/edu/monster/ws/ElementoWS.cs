using CoreWCF;
using ec.edu.monster.controlador;
using ec.edu.monster.ws.contrato;

namespace ec.edu.monster.ws;

/// <summary>
/// EJEMPLO de endpoint SOAP publicado como ServicioElemento. Solo recibe, delega y devuelve.
/// La clase debe registrarse en el contenedor (<c>AddTransient</c>, ver Program.cs): sin eso toda llamada falla
/// con InternalServiceFault. El <c>ServiceBehavior</c> fija el nombre y el espacio de nombres del servicio.
/// </summary>
[ServiceBehavior(Name = ConvencionSoap.ServicioElemento, Namespace = ConvencionSoap.Tns,
    InstanceContextMode = InstanceContextMode.PerCall, ConcurrencyMode = ConcurrencyMode.Multiple)]
public class ElementoWS : IElementoWS
{
    private readonly ElementoControlador _controlador;

    public ElementoWS(ElementoControlador controlador)
    {
        _controlador = controlador;
    }

    public RespuestaElemento ConsultarElemento(string codigoElemento) =>
        ElementoMapeador.ARespuestaElemento(_controlador.Consultar(codigoElemento));

    public RespuestaElementos ListarElementos() =>
        ElementoMapeador.ARespuestaElementos(_controlador.Listar());

    public RespuestaElemento RegistrarElemento(string codigoElemento, string nombre, string valor) =>
        ElementoMapeador.ARespuestaElemento(_controlador.Registrar(codigoElemento, nombre, valor));
}
