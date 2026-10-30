namespace ec.edu.monster.api;

/// <summary>
/// Lo que devuelve el transporte: el código HTTP y el cuerpo tal cual llegó (puede ser nulo o vacío). No se interpreta
/// aquí: eso es de <see cref="TraductorApi"/>.
/// </summary>
public sealed record RespuestaHttp(int Codigo, string? Cuerpo);
