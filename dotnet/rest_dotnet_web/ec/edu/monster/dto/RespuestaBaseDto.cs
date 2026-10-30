namespace ec.edu.monster.dto;

/// <summary>
/// Forma común de toda respuesta del servidor (regla R5 de docs/CONVENCION_REST.md): <c>estado</c> (OK/ERROR),
/// <c>codigo</c> y <c>mensaje</c>. Es un tipo propio del cliente, sin anotaciones de serializador: <c>api</c> lo llena
/// leyendo el JSON de la respuesta (ARQUITECTURA_CLIENTE.md 4.5). Los DTO de cada operación la extienden.
/// </summary>
public class RespuestaBaseDto
{
    public string? Estado { get; set; }
    public string? Codigo { get; set; }
    public string? Mensaje { get; set; }

    public bool EsOk => Estado == "OK";
}
