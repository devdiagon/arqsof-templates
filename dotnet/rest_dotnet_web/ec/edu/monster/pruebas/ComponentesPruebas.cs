using System.Text;
using System.Text.Encodings.Web;
using System.Text.Unicode;
using ec.edu.monster.vista.componentes;
using Microsoft.AspNetCore.Html;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Mvc.Routing;
using Microsoft.AspNetCore.Mvc.ViewFeatures;
using Microsoft.AspNetCore.Razor.TagHelpers;
using Moq;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>
/// Cada componente <c>ui-*</c> se dibuja sin servidor ni vistas: se ejecuta el tag helper y se mide el HTML. Garantizan el
/// contrato de seguridad (todo texto dinámico sale escapado) y las clases que usa <c>estilos.css</c>. El conjunto con las
/// vistas y el layout se verifica al recorrer la aplicación.
/// </summary>
public class ComponentesPruebas
{
    private const string Hostil = "<script>alert(1)</script>\" onfocus=\"x";
    // El mismo codificador que Program.cs: tildes, «…» y «→» tal cual; <, >, &, comillas y «+» escapados
    private static readonly HtmlEncoder Codificador = HtmlEncoder.Create(new TextEncoderSettings(
        UnicodeRanges.BasicLatin, UnicodeRanges.Latin1Supplement, UnicodeRanges.GeneralPunctuation, UnicodeRanges.Arrows));

    private const string Token = "<input name=\"__RequestVerificationToken\" type=\"hidden\" value=\"t\" />";

    // La aplicación vive bajo la ruta base «/app»: así se comprueba que los enlaces la respetan
    private static readonly IUrlHelperFactory FabricaUrl = CrearFabricaUrl();
    private static readonly IHtmlGenerator Generador = CrearGenerador();
    private static readonly ViewContext Contexto = new() { HttpContext = new DefaultHttpContext() };

    private static IUrlHelperFactory CrearFabricaUrl()
    {
        var url = new Mock<IUrlHelper>();
        url.Setup(u => u.Content(It.IsAny<string>()))
            .Returns<string>(ruta => ruta.StartsWith('~') ? "/app" + ruta[1..] : ruta);
        var fabrica = new Mock<IUrlHelperFactory>();
        fabrica.Setup(f => f.GetUrlHelper(It.IsAny<ActionContext>())).Returns(url.Object);
        return fabrica.Object;
    }

    private static IHtmlGenerator CrearGenerador()
    {
        var generador = new Mock<IHtmlGenerator>();
        generador.Setup(g => g.GenerateAntiforgery(It.IsAny<ViewContext>())).Returns(new HtmlString(Token));
        return generador.Object;
    }

    /// <summary>Ejecuta el tag helper como lo hace Razor (con el cuerpo como contenido hijo) y devuelve el HTML resultante.</summary>
    private static async Task<string> Renderizar(TagHelper componente, string etiqueta, string cuerpo = "")
    {
        var contexto = new TagHelperContext(etiqueta, new TagHelperAttributeList(), new Dictionary<object, object>(), "id");
        var salida = new TagHelperOutput(etiqueta, new TagHelperAttributeList(), (_, _) =>
        {
            var hijos = new DefaultTagHelperContent();
            hijos.SetHtmlContent(cuerpo);
            return Task.FromResult<TagHelperContent>(hijos);
        });

        await componente.ProcessAsync(contexto, salida);

        // Si el componente no escribió contenido propio, Razor emite el del cuerpo
        TagHelperContent contenido = salida.Content.IsModified ? salida.Content : await salida.GetChildContentAsync();

        var html = new StringBuilder();
        using var escritor = new StringWriter(html);
        salida.PreElement.WriteTo(escritor, Codificador);
        if (salida.TagName is not null)
        {
            escritor.Write($"<{salida.TagName}");
            foreach (TagHelperAttribute atributo in salida.Attributes)
            {
                escritor.Write(atributo.ValueStyle == HtmlAttributeValueStyle.Minimized
                    ? $" {atributo.Name}"
                    : $" {atributo.Name}=\"{Codificador.Encode(atributo.Value?.ToString() ?? string.Empty)}\"");
            }
            escritor.Write(salida.TagMode == TagMode.SelfClosing ? " />" : ">");
        }
        salida.PreContent.WriteTo(escritor, Codificador);
        contenido.WriteTo(escritor, Codificador);
        salida.PostContent.WriteTo(escritor, Codificador);
        if (salida.TagName is not null && salida.TagMode == TagMode.StartTagAndEndTag)
        {
            escritor.Write($"</{salida.TagName}>");
        }
        salida.PostElement.WriteTo(escritor, Codificador);
        return html.ToString();
    }

    private static void NoHayHtmlInyectado(string html)
    {
        Assert.DoesNotContain("<script>", html);
        Assert.DoesNotContain("\" onfocus=\"", html);
    }

    // ---- ui-encabezado ----

    [Theory]
    [InlineData(false, false, "<h1 class=\"encabezado\">Título</h1>")]
    [InlineData(true, false, "<h1 class=\"encabezado encabezado--aplicacion\">Título</h1>")]
    [InlineData(false, true, "<h1 class=\"encabezado encabezado--plano\">Título</h1>")]
    public async Task Encabezado_usa_el_titulo_de_pantalla_el_de_la_aplicacion_o_el_plano(bool aplicacion, bool plano, string esperado)
    {
        string html = await Renderizar(new UiEncabezadoTagHelper { Aplicacion = aplicacion, Plano = plano }, "ui-encabezado", "Título");

        Assert.Equal(esperado, html);
    }

    // ---- ui-campo ----

    [Fact]
    public async Task Campo_escribe_etiqueta_y_campo_con_el_nombre_por_defecto_igual_al_id()
    {
        string html = await Renderizar(new UiCampoTagHelper { Id = "dato", Etiqueta = "Nombre del dato", Valor = "abc" }, "ui-campo");

        Assert.StartsWith("<div>", html);
        Assert.Contains("<label class=\"etiqueta\" for=\"dato\">Nombre del dato</label>", html);
        Assert.Contains("class=\"campo\"", html);
        Assert.Contains("id=\"dato\"", html);
        Assert.Contains("name=\"dato\"", html);
        Assert.Contains("type=\"text\"", html);
        Assert.Contains("autocomplete=\"off\"", html);
        Assert.Contains("inputmode=\"text\"", html);
        Assert.Contains("value=\"abc\"", html);
        Assert.Contains("aria-label=\"Nombre del dato\"", html);
        Assert.DoesNotContain("autofocus", html);
    }

    [Fact]
    public async Task Campo_marca_el_error_el_foco_el_centrado_y_las_opciones()
    {
        string html = await Renderizar(new UiCampoTagHelper
        {
            Id = "secreto", Nombre = "pass", Etiqueta = "Contraseña", Tipo = "password", Autocomplete = "current-password",
            Inputmode = "decimal", Invalido = true, Autofocus = true, Centrado = true
        }, "ui-campo");

        Assert.StartsWith("<div class=\"campo-centrado\">", html);
        Assert.Contains("class=\"campo campo--error\"", html);
        Assert.Contains("name=\"pass\"", html);
        Assert.Contains("type=\"password\"", html);
        Assert.Contains("autocomplete=\"current-password\"", html);
        Assert.Contains("inputmode=\"decimal\"", html);
        Assert.Contains("autofocus=\"autofocus\"", html);
    }

    [Fact]
    public async Task Campo_sin_etiqueta_usa_el_placeholder_como_nombre_accesible()
    {
        string html = await Renderizar(new UiCampoTagHelper { Id = "x", Placeholder = "Dato" }, "ui-campo");

        Assert.DoesNotContain("<label", html);
        Assert.Contains("placeholder=\"Dato\"", html);
        Assert.Contains("aria-label=\"Dato\"", html);
    }

    [Fact]
    public async Task Campo_escapa_el_valor_la_etiqueta_y_el_placeholder()
    {
        string html = await Renderizar(new UiCampoTagHelper { Id = "x", Etiqueta = Hostil, Valor = Hostil, Placeholder = Hostil }, "ui-campo");

        NoHayHtmlInyectado(html);
        Assert.Contains("&lt;script&gt;", html);
    }

    // ---- ui-selector ----

    [Fact]
    public async Task Selector_escribe_las_opciones_y_marca_la_seleccionada()
    {
        string html = await Renderizar(new UiSelectorTagHelper
        {
            Id = "tipo", Etiqueta = "Tipo", Opciones = ["Uno", "Dos"], Seleccionada = "Dos"
        }, "ui-selector");

        Assert.Contains("<select class=\"campo\" id=\"tipo\" name=\"tipo\">", html);
        Assert.Contains("<option value=\"Uno\">Uno</option>", html);
        Assert.Contains("<option selected=\"selected\" value=\"Dos\">Dos</option>", html);
    }

    [Fact]
    public async Task Selector_escapa_las_opciones()
    {
        string html = await Renderizar(new UiSelectorTagHelper { Id = "x", Opciones = [Hostil] }, "ui-selector");

        NoHayHtmlInyectado(html);
    }

    // ---- ui-mensaje ----

    [Theory]
    [InlineData(null)]
    [InlineData("")]
    [InlineData("   ")]
    public async Task Mensaje_sin_texto_no_se_dibuja(string? texto)
    {
        string html = await Renderizar(new UiMensajeTagHelper { Mensaje = texto }, "ui-mensaje");

        Assert.Equal("", html);
    }

    [Theory]
    [InlineData("error", "class=\"mensaje\"", "role=\"alert\"")]
    [InlineData("exito", "class=\"mensaje mensaje--exito\"", "role=\"status\"")]
    [InlineData("resultado", "class=\"mensaje mensaje--resultado\"", "role=\"status\"")]
    public async Task Mensaje_usa_la_clase_y_el_rol_de_su_tipo(string tipo, string clase, string rol)
    {
        string html = await Renderizar(new UiMensajeTagHelper { Mensaje = "Listo", Tipo = tipo }, "ui-mensaje");

        Assert.Contains(clase, html);
        Assert.Contains(rol, html);
        Assert.Contains(">Listo</div>", html);
    }

    [Fact]
    public async Task Mensaje_escapa_el_texto()
    {
        string html = await Renderizar(new UiMensajeTagHelper { Mensaje = Hostil }, "ui-mensaje");

        NoHayHtmlInyectado(html);
        Assert.Contains("&lt;script&gt;", html);
    }

    // ---- ui-boton ----

    [Fact]
    public async Task Boton_sin_href_envia_el_formulario()
    {
        string html = await Renderizar(new UiBotonTagHelper(FabricaUrl) { ViewContext = Contexto }, "ui-boton", "Aceptar");

        Assert.Equal("<button class=\"boton boton--primario\" type=\"submit\">Aceptar</button>", html);
    }

    [Fact]
    public async Task Boton_con_href_es_un_enlace_que_respeta_la_ruta_base()
    {
        string html = await Renderizar(
            new UiBotonTagHelper(FabricaUrl) { ViewContext = Contexto, Tipo = "secundario", Href = "/inicio", Cancelar = true },
            "ui-boton", "Cancelar");

        Assert.Equal("<a class=\"boton boton--secundario\" href=\"/app/inicio\" data-cancelar=\"\">Cancelar</a>", html);
    }

    [Fact]
    public async Task Boton_centrado_ancho_y_salida_se_envuelven_en_el_orden_correcto()
    {
        string html = await Renderizar(
            new UiBotonTagHelper(FabricaUrl) { ViewContext = Contexto, Centrado = true, Salida = true, Ancho = true },
            "ui-boton", "X");

        Assert.StartsWith("<div class=\"acciones-salida\"><div class=\"accion-principal\"><button", html);
        Assert.EndsWith("</button></div></div>", html);
        Assert.Contains("boton--ancho", html);
    }

    [Fact]
    public async Task Boton_deshabilitado_lleva_el_atributo()
    {
        string html = await Renderizar(new UiBotonTagHelper(FabricaUrl) { ViewContext = Contexto, Deshabilitado = true }, "ui-boton", "X");

        Assert.Contains("disabled=\"disabled\"", html);
    }

    // ---- contenedores ----

    [Theory]
    [InlineData("cuadricula-acciones")]
    [InlineData("fila-botones")]
    [InlineData("fila-campos")]
    [InlineData("campo-con-acciones")]
    [InlineData("tarjetas")]
    public async Task Los_contenedores_envuelven_su_cuerpo_en_un_div_con_su_clase(string clase)
    {
        TagHelper componente = clase switch
        {
            "cuadricula-acciones" => new UiCuadriculaAccionesTagHelper(),
            "fila-botones" => new UiFilaBotonesTagHelper(),
            "fila-campos" => new UiFilaCamposTagHelper(),
            "campo-con-acciones" => new UiCampoConAccionesTagHelper(),
            _ => new UiTarjetasTagHelper()
        };

        string html = await Renderizar(componente, "ui-" + clase, "<p>x</p>");

        Assert.Equal($"<div class=\"{clase}\"><p>x</p></div>", html);
    }

    // ---- ui-tarjeta ----

    [Theory]
    [InlineData(true, "tarjeta__monto--suma")]
    [InlineData(false, "tarjeta__monto--resta")]
    public async Task Tarjeta_pinta_el_monto_segun_suba_o_baje_el_valor(bool suma, string clase)
    {
        string html = await Renderizar(new UiTarjetaTagHelper
        {
            Titulo = "Operación realizada", Suma = suma, Monto = "+25",
            Variacion = "100 → 125", Pie = "Ref. 12"
        }, "ui-tarjeta");

        Assert.Contains($"<div class=\"tarjeta__monto {clase}\">&#x2B;25</div>", html);
        Assert.Contains("<div class=\"tarjeta__titulo\">Operación realizada</div>", html);
        Assert.Contains("<div class=\"tarjeta__variacion\">100 → 125</div>", html);
        Assert.Contains("<div class=\"tarjeta__pie\">Ref. 12</div>", html);
        Assert.Contains("role=\"group\"", html);
    }

    [Fact]
    public async Task Tarjeta_sin_titulo_no_lo_dibuja()
    {
        string html = await Renderizar(new UiTarjetaTagHelper { Monto = "+$1.00", Variacion = "a", Pie = "b" }, "ui-tarjeta");

        Assert.DoesNotContain("tarjeta__titulo", html);
    }

    [Fact]
    public async Task Tarjeta_escapa_todos_sus_textos()
    {
        string html = await Renderizar(new UiTarjetaTagHelper { Titulo = Hostil, Monto = Hostil, Variacion = Hostil, Pie = Hostil }, "ui-tarjeta");

        NoHayHtmlInyectado(html);
    }

    // ---- ui-tabla ----

    [Fact]
    public async Task Tabla_dibuja_encabezado_filas_y_alinea_las_numericas_a_la_derecha()
    {
        string html = await Renderizar(new UiTablaTagHelper
        {
            Columnas = ["N.º", "Nombre", "Valor"],
            AlDerecha = [true, false, true],
            Filas = new List<string[]> { new[] { "3", "Ana", "50" }, new[] { "2", "Ana", "1100" } },
            Titulo = "Resultados",
            Cantidad = "2 resultado(s)"
        }, "ui-tabla");

        Assert.Contains("<div class=\"tabla-resumen\"><strong>Resultados</strong><span>2 resultado(s)</span></div>", html);
        Assert.Contains("<th class=\"num\" scope=\"col\">N.º</th><th scope=\"col\">Nombre</th><th class=\"num\" scope=\"col\">Valor</th>", html);
        Assert.Contains("<tr><td class=\"num\">3</td><td>Ana</td><td class=\"num\">50</td></tr>", html);
        Assert.Contains("<tr><td class=\"num\">2</td><td>Ana</td><td class=\"num\">1100</td></tr>", html);
        Assert.Contains("tabla-contenedor", html);
    }

    [Fact]
    public async Task Tabla_sin_titulo_no_dibuja_la_linea_de_resumen()
    {
        string html = await Renderizar(new UiTablaTagHelper { Columnas = ["A"], AlDerecha = [false], Filas = [] }, "ui-tabla");

        Assert.DoesNotContain("tabla-resumen", html);
    }

    [Fact]
    public async Task Tabla_escapa_titulos_celdas_y_resumen()
    {
        string html = await Renderizar(new UiTablaTagHelper
        {
            Columnas = [Hostil], AlDerecha = [false], Filas = new List<string[]> { new[] { Hostil } },
            Titulo = Hostil, Cantidad = Hostil
        }, "ui-tabla");

        NoHayHtmlInyectado(html);
    }

    // ---- ui-progreso ----

    [Fact]
    public async Task Progreso_usa_Procesando_por_defecto_o_el_texto_indicado()
    {
        string porDefecto = await Renderizar(new UiProgresoTagHelper(), "ui-progreso");
        string consultando = await Renderizar(new UiProgresoTagHelper { Texto = "Consultando…" }, "ui-progreso");

        Assert.Contains("progreso__giro", porDefecto);
        Assert.Contains("<span class=\"progreso__texto\">Procesando…</span>", porDefecto);
        Assert.Contains("<span class=\"progreso__texto\">Consultando…</span>", consultando);
    }

    // ---- ui-formulario ----

    [Fact]
    public async Task Formulario_es_un_post_con_token_progreso_y_la_ruta_base()
    {
        string html = await Renderizar(
            new UiFormularioTagHelper(Generador, FabricaUrl) { ViewContext = Contexto, Accion = "/consulta", Ocupado = "Procesando…" },
            "ui-formulario", "<p>campos</p>");

        Assert.StartsWith("<form class=\"formulario\" method=\"post\" action=\"/app/consulta\" data-ocupable=\"\">", html);
        Assert.Contains("<p>campos</p>", html);
        Assert.Contains("<span class=\"progreso__texto\">Procesando…</span>", html);
        Assert.Contains(Token, html);
        Assert.EndsWith("</form>", html);
        Assert.True(html.IndexOf("<p>campos</p>", StringComparison.Ordinal) < html.IndexOf("progreso", StringComparison.Ordinal));
    }

    // ---- ui-lista-acciones ----

    [Fact]
    public async Task Lista_de_acciones_dibuja_un_enlace_por_elemento_con_el_valor_codificado()
    {
        string html = await Renderizar(
            new UiListaAccionesTagHelper(FabricaUrl) { ViewContext = Contexto, Elementos = ["A B", Hostil], Destino = "/detalle", Parametro = "item" },
            "ui-lista-acciones");

        Assert.Contains("href=\"/app/detalle?item=A%20B\"", html);
        Assert.Contains(">A B</a>", html);
        NoHayHtmlInyectado(html);
    }

    [Fact]
    public async Task Lista_de_acciones_vacia_no_se_dibuja()
    {
        string html = await Renderizar(
            new UiListaAccionesTagHelper(FabricaUrl) { ViewContext = Contexto, Elementos = [], Destino = "/d", Parametro = "p" },
            "ui-lista-acciones");

        Assert.Equal("", html);
    }
}
