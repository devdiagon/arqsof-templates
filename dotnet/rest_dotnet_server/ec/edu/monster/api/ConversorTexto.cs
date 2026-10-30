using System.Globalization;

namespace ec.edu.monster.api;

/// <summary>
/// Convierte los tipos reales del servidor a texto para el contrato (R9): decimales con punto
/// y dos decimales, fechas en ISO-8601 (yyyy-MM-dd) y enteros sin separadores.
/// Es independiente del idioma o la configuración regional del equipo.
/// </summary>
public static class ConversorTexto
{
    /// <summary>El redondeo es el de Java (HALF_UP): el empate se aleja de cero, no el bancario de .NET.</summary>
    public static string? Decimal(decimal? valor) =>
        valor is null
            ? null
            : Math.Round(valor.Value, 2, MidpointRounding.AwayFromZero).ToString("F2", CultureInfo.InvariantCulture);

    public static string? Fecha(DateOnly? fecha) =>
        fecha?.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture);

    public static string Entero(int valor) =>
        valor.ToString(CultureInfo.InvariantCulture);
}
