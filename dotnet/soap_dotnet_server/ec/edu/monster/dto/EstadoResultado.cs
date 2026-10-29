namespace ec.edu.monster.dto;

/// <summary>
/// Estado de un <see cref="Resultado{T}"/>. Vive fuera de él porque un tipo anidado en una
/// clase genérica depende de T (diferencia de plataforma respecto a Java).
/// </summary>
public enum EstadoResultado
{
    OK,
    ERROR
}
