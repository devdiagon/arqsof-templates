using System.Globalization;
using System.Text.RegularExpressions;
using ec.edu.monster.excepciones;

namespace ec.edu.monster.controlador;

/// <summary>
/// Reglas genéricas de forma de los datos de entrada (obligatoriedad y formato).
/// No conoce ningún dominio: cada controlador las combina con los formatos de su dominio.
/// Todo texto se recorta antes de validar; un valor nulo o en blanco cuenta como no enviado.
/// Un incumplimiento lanza DATA-001 con un mensaje específico del campo.
/// </summary>
public static class ValidadorForma
{
    // [0-9] y no \d: en .NET \d acepta dígitos de cualquier escritura; en Java solo los ASCII.
    private static readonly Regex SoloDigitos = new(@"^[0-9]+\z", RegexOptions.CultureInvariant);
    private static readonly Regex DecimalPositivoRegex =
        new(@"^([0-9]+)(\.([0-9]+))?\z", RegexOptions.CultureInvariant);

    /// <summary>
    /// El valor debe venir informado. Devuelve el valor recortado.
    /// </summary>
    public static string Obligatorio(string? valor, string campo)
    {
        if (string.IsNullOrWhiteSpace(valor))
        {
            throw Invalido($"El campo {campo} es obligatorio");
        }
        return valor.Trim();
    }

    /// <summary>
    /// Obligatorio y de hasta <paramref name="maximo"/> caracteres.
    /// </summary>
    public static string TextoHasta(string? valor, string campo, int maximo)
    {
        string texto = Obligatorio(valor, campo);
        if (texto.Length > maximo)
        {
            throw Invalido($"El campo {campo} no puede tener más de {maximo} caracteres");
        }
        return texto;
    }

    /// <summary>
    /// Obligatorio y de exactamente <paramref name="longitud"/> caracteres.
    /// </summary>
    public static string TextoExacto(string? valor, string campo, int longitud)
    {
        string texto = Obligatorio(valor, campo);
        if (texto.Length != longitud)
        {
            throw Invalido($"El campo {campo} debe tener exactamente {longitud} caracteres");
        }
        return texto;
    }

    /// <summary>
    /// Obligatorio y formado por exactamente <paramref name="longitud"/> dígitos.
    /// </summary>
    public static string DigitosExactos(string? valor, string campo, int longitud)
    {
        string texto = Obligatorio(valor, campo);
        if (texto.Length != longitud || !SoloDigitos.IsMatch(texto))
        {
            throw Invalido($"El campo {campo} debe tener exactamente {longitud} dígitos");
        }
        return texto;
    }

    /// <summary>
    /// Obligatorio, con punto decimal opcional (sin coma, signo ni separador de miles),
    /// mayor que cero y dentro de los límites de dígitos indicados.
    /// </summary>
    public static decimal DecimalPositivo(string? valor, string campo, int maxEnteros, int maxDecimales)
    {
        string texto = Obligatorio(valor, campo);
        Match coincidencia = DecimalPositivoRegex.Match(texto);
        if (!coincidencia.Success)
        {
            throw Invalido($"El campo {campo} debe ser un número con punto decimal, sin signo");
        }
        Group decimales = coincidencia.Groups[3];
        if (decimales.Success && decimales.Length > maxDecimales)
        {
            throw Invalido($"El campo {campo} no puede tener más de {maxDecimales} decimales");
        }
        if (coincidencia.Groups[1].Length > maxEnteros)
        {
            throw Invalido($"El campo {campo} no puede tener más de {maxEnteros} dígitos enteros");
        }
        decimal numero = decimal.Parse(texto, NumberStyles.AllowDecimalPoint, CultureInfo.InvariantCulture);
        if (numero <= 0)
        {
            throw Invalido($"El campo {campo} debe ser mayor que cero");
        }
        return numero;
    }

    private static ExcepcionMonster Invalido(string mensaje) =>
        new(CodigoError.DATA_001, mensaje);
}
