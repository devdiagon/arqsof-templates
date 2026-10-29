namespace ec.edu.monster.dto;

/// <summary>
/// Forma común de toda respuesta del servidor (regla R6 de docs/CONVENCION_SOAP.md): <c>estado</c> (OK/ERROR),
/// <c>codigo</c> y <c>mensaje</c>. Es un tipo propio del cliente: el envoltorio de <c>ws</c> lo llena copiando la
/// respuesta del proxy generado. Los DTO de cada operación la extienden.
/// </summary>
public class RespuestaBaseDto
{
    public string? Estado { get; set; }
    public string? Codigo { get; set; }
    public string? Mensaje { get; set; }

    public bool EsOk => Estado == "OK";
}
