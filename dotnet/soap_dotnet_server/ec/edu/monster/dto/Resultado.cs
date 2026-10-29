namespace ec.edu.monster.dto;

/// <summary>
/// Estructura neutral que devuelve todo controlador: estado, código, mensaje y,
/// si hubo éxito, los datos. No conoce cómo se serializa.
/// </summary>
/// <typeparam name="T">Tipo de los datos devueltos en caso de éxito.</typeparam>
public record Resultado<T>(EstadoResultado Estado, string Codigo, string Mensaje, T? Datos);

/// <summary>
/// Constantes de éxito y fábricas de <see cref="Resultado{T}"/>. Clase aparte porque C# no infiere
/// el tipo genérico al llamar a un método estático de <c>Resultado&lt;T&gt;</c>.
/// </summary>
public static class Resultado
{
    public const string CodigoExito = "200";
    public const string MensajeExito = "Operación exitosa";

    public static Resultado<T> Exito<T>(T datos) =>
        new(EstadoResultado.OK, CodigoExito, MensajeExito, datos);

    public static Resultado<T> Error<T>(string codigo, string mensaje) =>
        new(EstadoResultado.ERROR, codigo, mensaje, default);
}
