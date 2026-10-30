using ec.edu.monster.vista;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>
/// <see cref="VistaBase"/> contra una consola simulada: se comprueba el texto exacto que ve el usuario. La clase de prueba
/// expone los miembros protegidos. Las vistas del dominio se prueban igual, o se sustituyen con Moq en las pruebas de los
/// controladores (sus métodos públicos son <c>virtual</c>).
/// </summary>
[Collection(ColeccionConsola.Nombre)]
public class VistaBasePruebas
{
    private sealed class VistaDePrueba : VistaBase
    {
        public int ElegirConCero(string titulo, IReadOnlyList<string> opciones, string textoCero) =>
            Elegir(titulo, opciones, textoCero);

        public int ElegirConCancelar(IReadOnlyList<string> opciones) => Elegir("", opciones);
    }

    private readonly VistaDePrueba _vista = new();

    [Fact]
    public void PedirEscribeLaEtiquetaSinSaltoDeLineaYDevuelveLaEntradaRecortada()
    {
        using var consola = new ConsolaFalsa("  valor  ");

        string valor = _vista.Pedir("Dato");

        Assert.Equal("valor", valor);
        Assert.Equal("Dato: ", consola.Salida);
    }

    [Fact]
    public void ElCierreDeLaEntradaLanzaFinDeFlujoEnLugarDeGirarEnUnMenu()
    {
        using var consola = new ConsolaFalsa();

        Assert.Throws<EndOfStreamException>(() => _vista.Pedir("Dato"));
        Assert.Throws<EndOfStreamException>(() => _vista.ElegirConCancelar(["Uno"]));
        Assert.Throws<EndOfStreamException>(() => _vista.Pausar());
    }

    [Fact]
    public void MostrarTituloLlevaLineaEnBlancoElTituloYGuionesDelMismoLargo()
    {
        using var consola = new ConsolaFalsa();

        _vista.MostrarTitulo("Menú principal");

        Assert.Equal("\nMenú principal\n--------------\n", consola.Salida);
    }

    [Fact]
    public void ElegirMuestraLaListaNumeradaConElCeroAlFinalYDevuelveElIndiceBase0()
    {
        using var consola = new ConsolaFalsa("2");

        int indice = _vista.ElegirConCero("", ["Uno", "Dos"], "Salir");

        Assert.Equal(1, indice);
        Assert.Equal("  1. Uno\n  2. Dos\n  0. Salir\nOpción: ", consola.Salida);
    }

    [Fact]
    public void ElCeroDevuelveMenosUnoYElTextoPorDefectoDelCeroEsCancelar()
    {
        using var consola = new ConsolaFalsa("0");

        Assert.Equal(-1, _vista.ElegirConCancelar(["Uno"]));
        Assert.Contains("  0. Cancelar\n", consola.Salida);
    }

    [Fact]
    public void UnTituloNoVacioSeImprimeAntesDeLasOpciones()
    {
        using var consola = new ConsolaFalsa("1");

        _vista.ElegirConCero("Elija", ["Uno"], "Salir");

        Assert.StartsWith("Elija\n  1. Uno\n", consola.Salida);
    }

    [Fact]
    public void UnaOpcionInvalidaSeInformaYSeVuelveAPedir()
    {
        using var consola = new ConsolaFalsa("x", "7", "", "1");

        int indice = _vista.ElegirConCero("", ["Uno"], "Salir");

        Assert.Equal(0, indice);
        Assert.Equal("  1. Uno\n  0. Salir\nOpción: [!] Opción inválida\nOpción: [!] Opción inválida\nOpción: [!] Opción inválida\nOpción: ",
            consola.Salida);
    }

    [Fact]
    public void PausarEscribeElTextoSinSaltoDeLineaYEsperaEnter()
    {
        using var consola = new ConsolaFalsa("");

        _vista.Pausar();

        Assert.Equal("Presione Enter para continuar", consola.Salida);
    }

    [Fact]
    public void ElErrorLlevaElPrefijoYElMensajeSinCambios()
    {
        using var consola = new ConsolaFalsa();

        _vista.MostrarError("El recurso no existe");

        Assert.Equal("[!] El recurso no existe\n", consola.Salida);
    }

    [Fact]
    public void SinPortadaDefinidaNoSeImprimeNada()
    {
        using var consola = new ConsolaFalsa();

        _vista.MostrarPortada();

        Assert.Equal("", consola.Salida);
    }

    [Fact]
    public void ElCampoObligatorioLlevaLaEtiquetaEnMinuscula()
    {
        Assert.Equal("El campo código de elemento es obligatorio",
            Textos.CampoObligatorio("Código de elemento"));
    }
}
