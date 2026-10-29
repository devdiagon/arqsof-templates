using System.Globalization;

namespace ec.edu.monster.ws;

/// <summary>
/// Convierte los tipos reales del servidor a texto para el contrato (R7): decimales con punto
/// y dos decimales, fechas en ISO-8601 (yyyy-MM-dd) y enteros sin separadores.
/// Es independiente del idioma o la configuración regional del equipo.
/// </summary>
public static class ConversorTexto
{
    public static string? Decimal(decimal? valor) =>
        valor is null
            ? null
            : Math.Round(valor.Value, 2, MidpointRounding.AwayFromZero).ToString("F2", CultureInfo.InvariantCulture);

    public static string? Fecha(DateOnly? fecha) =>
        fecha?.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture);

    public static string Entero(int valor) =>
        valor.ToString(CultureInfo.InvariantCulture);
}
