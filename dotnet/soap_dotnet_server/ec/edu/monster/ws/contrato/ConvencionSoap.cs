namespace ec.edu.monster.ws.contrato;

/// <summary>
/// Constantes de la convención SOAP (CONVENCION_SOAP.md). El espacio de nombres de destino (TNS)
/// se declara solo aquí y todo lo demás lo referencia (R2).
/// </summary>
public static class ConvencionSoap
{
    /// <summary>Espacio de nombres de destino de todos los servicios SOAP del equipo (R2).</summary>
    public const string Tns = "http://ws.monster.edu.ec/";

    /// <summary>EJEMPLO: nombre del servicio publicado. Sustituir por el del dominio.</summary>
    public const string ServicioElemento = "ServicioElemento";

    /// <summary>Nombre fijo del único elemento de retorno de toda operación (R5).</summary>
    public const string Retorno = "respuesta";
}
