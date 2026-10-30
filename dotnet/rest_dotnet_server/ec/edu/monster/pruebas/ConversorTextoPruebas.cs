using System.Globalization;
using ec.edu.monster.api;
using Xunit;

namespace ec.edu.monster.pruebas;

public class ConversorTextoPruebas : IDisposable
{
    private readonly CultureInfo _culturaOriginal = CultureInfo.CurrentCulture;

    public ConversorTextoPruebas()
    {
        // Configuración regional con coma decimal: el conversor no debe depender de ella.
        CultureInfo.CurrentCulture = new CultureInfo("es-EC");
    }

    public void Dispose()
    {
        CultureInfo.CurrentCulture = _culturaOriginal;
    }

    [Fact]
    public void ElDecimalSiempreLlevaDosDecimalesYPunto()
    {
        Assert.Equal("100.00", ConversorTexto.Decimal(100m));
        Assert.Equal("7000.50", ConversorTexto.Decimal(7000.5m));
        Assert.Equal("0.30", ConversorTexto.Decimal(0.30m));
    }

    [Fact]
    public void ElDecimalNuncaUsaNotacionCientificaNiSeparadorDeMiles()
    {
        Assert.Equal("1000.00", ConversorTexto.Decimal(1E+3m));
        Assert.Equal("1234567.89", ConversorTexto.Decimal(1234567.89m));
    }

    [Fact]
    public void ElDecimalRedondeaLaMitadHaciaArribaComoLaReferencia()
    {
        // HALF_UP de Java: el empate se aleja de cero (no el redondeo bancario de .NET por defecto).
        Assert.Equal("0.01", ConversorTexto.Decimal(0.005m));
        Assert.Equal("2.68", ConversorTexto.Decimal(2.675m));
        Assert.Equal("-0.01", ConversorTexto.Decimal(-0.005m));
    }

    [Fact]
    public void ElDecimalNuloQuedaNulo()
    {
        Assert.Null(ConversorTexto.Decimal(null));
    }

    [Fact]
    public void LaFechaSeEscribeEnIso8601()
    {
        Assert.Equal("2026-10-26", ConversorTexto.Fecha(new DateOnly(2026, 10, 26)));
        Assert.Equal("2022-01-06", ConversorTexto.Fecha(new DateOnly(2022, 1, 6)));
    }

    [Fact]
    public void LaFechaNulaQuedaNula()
    {
        Assert.Null(ConversorTexto.Fecha(null));
    }

    [Fact]
    public void ElEnteroSeEscribeSinSeparadores()
    {
        Assert.Equal("1234567", ConversorTexto.Entero(1234567));
        Assert.Equal("0", ConversorTexto.Entero(0));
    }
}
