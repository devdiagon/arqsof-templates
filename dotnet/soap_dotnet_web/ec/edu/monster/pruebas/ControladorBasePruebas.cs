using ec.edu.monster.controlador;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc.ViewFeatures;
using Xunit;

namespace ec.edu.monster.pruebas;

public class ControladorBasePruebas
{
    private sealed record Dato(string Texto, int Numero);

    /// <summary>Guarda lo que TempData le entrega entre una petición y la siguiente, sin cookies ni servidor.</summary>
    private sealed class ProveedorFalso : ITempDataProvider
    {
        private IDictionary<string, object>? _guardado;

        public IDictionary<string, object> LoadTempData(HttpContext context) =>
            _guardado is null ? new Dictionary<string, object>() : new Dictionary<string, object>(_guardado);

        public void SaveTempData(HttpContext context, IDictionary<string, object> values) =>
            _guardado = values.Count == 0 ? null : new Dictionary<string, object>(values);
    }

    // Privada: MVC solo reconoce como controladores los tipos públicos, así que no se registra en la aplicación
    private sealed class ControladorDePrueba : ControladorBase
    {
        public static string Recortar(string? valor) => Texto(valor);
        public static bool EstaVacio(string? valor) => Vacio(valor);
        public void Dejar(Dato dato) => GuardarFlash(dato);
        public Dato? Tomar() => TomarFlash<Dato>();
        public bool HayOtro() => TomarFlash<OtroDato>() is not null;
    }

    private sealed record OtroDato(string Texto);

    private readonly ProveedorFalso _proveedor = new();

    /// <summary>Una petición nueva: un controlador con el TempData que dejó la anterior.</summary>
    private ControladorDePrueba Peticion()
    {
        var http = new DefaultHttpContext();
        return new ControladorDePrueba { TempData = new TempDataDictionary(http, _proveedor) };
    }

    private void Terminar(ControladorDePrueba controlador) => controlador.TempData.Save();

    [Theory]
    [InlineData(null, "")]
    [InlineData("", "")]
    [InlineData("  abc ", "abc")]
    public void Texto_recorta_y_trata_nulo_como_vacio(string? entrada, string esperado) =>
        Assert.Equal(esperado, ControladorDePrueba.Recortar(entrada));

    [Theory]
    [InlineData(null, true)]
    [InlineData("", true)]
    [InlineData("   ", true)]
    [InlineData("a", false)]
    public void Vacio_incluye_nulo_y_espacios(string? entrada, bool esperado) =>
        Assert.Equal(esperado, ControladorDePrueba.EstaVacio(entrada));

    [Fact]
    public void Lo_guardado_en_un_POST_se_lee_en_el_GET_siguiente_y_solo_una_vez()
    {
        ControladorDePrueba post = Peticion();
        post.Dejar(new Dato("listo", 7));
        Terminar(post);

        ControladorDePrueba get = Peticion();
        Dato? leido = get.Tomar();
        Terminar(get);
        ControladorDePrueba recarga = Peticion();   // F5

        Assert.Equal(new Dato("listo", 7), leido);
        Assert.Null(recarga.Tomar());
    }

    [Fact]
    public void Sin_nada_guardado_no_hay_resultado()
    {
        Assert.Null(Peticion().Tomar());
    }

    [Fact]
    public void Cada_tipo_tiene_su_propio_resultado()
    {
        ControladorDePrueba post = Peticion();
        post.Dejar(new Dato("a", 1));
        Terminar(post);

        Assert.False(Peticion().HayOtro());
    }
}
