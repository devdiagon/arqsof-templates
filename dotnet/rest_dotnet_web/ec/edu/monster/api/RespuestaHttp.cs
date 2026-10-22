namespace ec.edu.monster.api;

/// <summary>Lo que devolvió el servidor: código HTTP y cuerpo sin interpretar.</summary>
public record RespuestaHttp(int Codigo, string Cuerpo);
