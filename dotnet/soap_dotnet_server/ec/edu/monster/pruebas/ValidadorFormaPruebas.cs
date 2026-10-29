using ec.edu.monster.controlador;
using ec.edu.monster.excepciones;
using Xunit;

namespace ec.edu.monster.pruebas;

public class ValidadorFormaPruebas
{
    private static ExcepcionMonster Rechazo(Action accion)
    {
        ExcepcionMonster e = Assert.Throws<ExcepcionMonster>(accion);
        Assert.Equal(CodigoError.DATA_001, e.Error);
        return e;
    }

    // ------------------------------------------------------------------ obligatorio

    [Fact]
    public void ObligatorioRecortaElValor()
    {
        Assert.Equal("abc", ValidadorForma.Obligatorio("  abc  ", "campo"));
    }

    [Theory]
    [InlineData(null)]
    [InlineData("")]
    [InlineData(" ")]
    [InlineData("   ")]
    public void ObligatorioRechazaNuloVacioYBlanco(string? valor)
    {
        ExcepcionMonster e = Rechazo(() => ValidadorForma.Obligatorio(valor, "nombre"));

        Assert.Equal("El campo nombre es obligatorio", e.Message);
    }

    // ------------------------------------------------------------------ longitudes

    [Fact]
    public void TextoHastaAceptaElLimiteExactoYRechazaElExceso()
    {
        Assert.Equal("123456789012345", ValidadorForma.TextoHasta("123456789012345", "referencia", 15));

        ExcepcionMonster e = Rechazo(() => ValidadorForma.TextoHasta("1234567890123456", "referencia", 15));
        Assert.Equal("El campo referencia no puede tener más de 15 caracteres", e.Message);
    }

    [Fact]
    public void TextoExactoExigeLaLongitudIndicada()
    {
        Assert.Equal("123456", ValidadorForma.TextoExacto("123456", "referencia", 6));

        ExcepcionMonster corto = Rechazo(() => ValidadorForma.TextoExacto("12345", "referencia", 6));
        Rechazo(() => ValidadorForma.TextoExacto("1234567", "referencia", 6));
        Assert.Equal("El campo referencia debe tener exactamente 6 caracteres", corto.Message);
    }

    [Fact]
    public void DigitosExactosAceptaSoloLaCantidadIndicadaDeDigitos()
    {
        Assert.Equal("00010001", ValidadorForma.DigitosExactos(" 00010001 ", "código", 8));
    }

    [Theory]
    [InlineData("1234567")]
    [InlineData("123456789")]
    [InlineData("0010000A")]
    [InlineData("0010 001")]
    [InlineData("-0100001")]
    [InlineData("٠٠١٠٠٠٠١")] // dígitos arábigo-índicos: \d de .NET los aceptaría
    public void DigitosExactosRechazaLongitudIncorrectaOCaracteresQueNoSonDigitos(string valor)
    {
        ExcepcionMonster e = Rechazo(() => ValidadorForma.DigitosExactos(valor, "código", 8));

        Assert.Equal("El campo código debe tener exactamente 8 dígitos", e.Message);
    }

    // ------------------------------------------------------------------ decimal positivo

    [Theory]
    [InlineData("100")]
    [InlineData("100.5")]
    [InlineData("100.50")]
    [InlineData("0.01")]
    [InlineData(" 50 ")]
    [InlineData("9999999999.99")]
    public void DecimalPositivoAceptaNumerosConPuntoDentroDeLosLimites(string valor)
    {
        decimal numero = ValidadorForma.DecimalPositivo(valor, "importe", 10, 2);

        Assert.Equal(decimal.Parse(valor.Trim(), System.Globalization.CultureInfo.InvariantCulture), numero);
    }

    [Theory]
    [InlineData("-5")]
    [InlineData("+5")]
    [InlineData("10,5")]
    [InlineData("abc")]
    [InlineData("1e3")]
    [InlineData(".5")]
    [InlineData("5.")]
    [InlineData("1.2.3")]
    [InlineData("1 000")]
    [InlineData("10.5.0")]
    public void DecimalPositivoRechazaFormatosQueNoSonNumeroConPunto(string valor)
    {
        ExcepcionMonster e = Rechazo(() => ValidadorForma.DecimalPositivo(valor, "importe", 10, 2));

        Assert.Equal("El campo importe debe ser un número con punto decimal, sin signo", e.Message);
    }

    [Theory]
    [InlineData("0")]
    [InlineData("0.00")]
    [InlineData("0.0")]
    public void DecimalPositivoRechazaElCero(string valor)
    {
        ExcepcionMonster e = Rechazo(() => ValidadorForma.DecimalPositivo(valor, "importe", 10, 2));

        Assert.Equal("El campo importe debe ser mayor que cero", e.Message);
    }

    [Fact]
    public void DecimalPositivoRechazaMasDecimalesDeLosPermitidos()
    {
        ExcepcionMonster e = Rechazo(() => ValidadorForma.DecimalPositivo("10.999", "importe", 10, 2));

        Assert.Equal("El campo importe no puede tener más de 2 decimales", e.Message);
    }

    [Fact]
    public void DecimalPositivoRechazaMasDigitosEnterosDeLosPermitidos()
    {
        ExcepcionMonster e = Rechazo(() => ValidadorForma.DecimalPositivo("12345678901", "importe", 10, 2));

        Assert.Equal("El campo importe no puede tener más de 10 dígitos enteros", e.Message);
    }

    [Fact]
    public void DecimalPositivoInformaPrimeroLosDecimalesQueLosEnteros()
    {
        ExcepcionMonster e = Rechazo(() => ValidadorForma.DecimalPositivo("12345678901.999", "importe", 10, 2));

        Assert.Contains("decimales", e.Message);
    }

    [Fact]
    public void DecimalPositivoRechazaNuloYBlanco()
    {
        Rechazo(() => ValidadorForma.DecimalPositivo(null, "importe", 10, 2));
        Rechazo(() => ValidadorForma.DecimalPositivo("   ", "importe", 10, 2));
    }
}
