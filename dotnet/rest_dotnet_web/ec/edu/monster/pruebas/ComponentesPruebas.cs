using System.Text;
using System.Text.Encodings.Web;
using ec.edu.monster.vista.componentes;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Mvc.Routing;
using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.pruebas;

/// <summary>
/// Cada componente <c>ui-*</c> se dibuja sin servidor ni vistas: se ejecuta el tag helper y se mide el HTML.
/// Garantizan el contrato de seguridad (todo texto dinámico sale escapado) y las clases que usa <c>estilos.css</c>.
/// <c>ui-formulario</c> y <c>ui-cerrar-sesion</c> se cubren en <see cref="PantallasWebPruebas"/> (necesitan el motor de vistas).
/// </summary>
public class ComponentesPruebas
{
    /// <summary>Ejecuta el tag helper y devuelve el HTML resultante (etiqueta, atributos y contenido), con el escape de Razor.</summary>
    private static string Renderizar(TagHelper componente, string etiqueta)
    {
        var contexto = new TagHelperContext(etiqueta, new TagHelperAttributeList(), new Dictionary<object, object>(), "id");
        var salida = new TagHelperOutput(etiqueta, new TagHelperAttributeList(),
            (_, _) => Task.FromResult<TagHelperContent>(new DefaultTagHelperContent()));

        componente.Process(contexto, salida);

        var html = new StringBuilder();
        using var escritor = new StringWriter(html);
        salida.PreElement.WriteTo(escritor, HtmlEncoder.Default);
        if (salida.TagName is not null)
        {
            escritor.Write($"<{salida.TagName}");
            foreach (var atributo in salida.Attributes)
                escritor.Write($" {atributo.Name}=\"{HtmlEncoder.Default.Encode(atributo.Value?.ToString() ?? string.Empty)}\"");
            escritor.Write(salida.TagMode == TagMode.SelfClosing ? " />" : ">");
        }
        salida.PreContent.WriteTo(escritor, HtmlEncoder.Default);
        salida.Content.WriteTo(escritor, HtmlEncoder.Default);
        salida.PostContent.WriteTo(escritor, HtmlEncoder.Default);
        if (salida.TagName is not null && salida.TagMode == TagMode.StartTagAndEndTag)
            escritor.Write($"</{salida.TagName}>");
        salida.PostElement.WriteTo(escritor, HtmlEncoder.Default);
        return html.ToString();
    }

    private const string Hostil = "<script>alert(1)</script>\" onfocus=\"x";

    private static void NoHayHtmlInyectado(string html)
    {
        Assert.DoesNotContain("<script>", html);
        Assert.DoesNotContain("\" onfocus=\"", html);
    }

    // ---- ui-encabezado ----

    [Theory]
    [InlineData(false, "texto-titulo")]
    [InlineData(true, "texto-titulo-app")]
    public void Encabezado_usa_el_titulo_de_pantalla_o_el_de_la_aplicacion(bool aplicacion, string clase)
    {
        var html = Renderizar(new UiEncabezadoTagHelper { Aplicacion = aplicacion }, "ui-encabezado");

        Assert.Contains("class=\"panel-encabezado\"", html);
        Assert.Contains($"<h1 class=\"{clase}\">", html);
    }

    // ---- ui-campo ----

    [Fact]
    public void Campo_dibuja_etiqueta_encima_y_enlazada_al_campo()
    {
        var html = Renderizar(new UiCampoTagHelper { Id = "Usuario", Etiqueta = "Usuario", Autocomplete = "username", Autofocus = true }, "ui-campo");

        Assert.Contains("<label class=\"etiqueta\" for=\"Usuario\">Usuario</label>", html);
        Assert.Contains("id=\"Usuario\"", html);
        Assert.Contains("name=\"Usuario\"", html);
        Assert.Contains("type=\"text\"", html);
        Assert.Contains("autocomplete=\"username\"", html);
        Assert.Contains("autofocus=\"autofocus\"", html);
        Assert.Contains("class=\"campo\"", html);
        Assert.DoesNotContain("campo--invalido", html);
    }

    [Fact]
    public void Campo_de_contrasenia_y_marca_de_error()
    {
        var html = Renderizar(new UiCampoTagHelper { Id = "Clave", Etiqueta = "Clave", Tipo = "password", Invalido = true }, "ui-campo");

        Assert.Contains("type=\"password\"", html);
        Assert.Contains("class=\"campo campo--invalido\"", html);
        Assert.DoesNotContain("value=", html);   // sin valor no se escribe el atributo
    }

    [Fact]
    public void Campo_escapa_etiqueta_valor_y_placeholder()
    {
        var html = Renderizar(new UiCampoTagHelper { Id = "X", Etiqueta = Hostil, Valor = Hostil, Placeholder = Hostil }, "ui-campo");

        NoHayHtmlInyectado(html);
        Assert.Contains("&lt;script&gt;", html);
    }

    // ---- ui-selector ----

    [Fact]
    public void Selector_dibuja_las_opciones_y_marca_solo_la_seleccionada()
    {
        var html = Renderizar(new UiSelectorTagHelper
        {
            Id = "Origen", Etiqueta = "Unidad de origen", Opciones = ["Metro", "Kilómetro", "metro"], Seleccionada = "Metro"
        }, "ui-selector");

        Assert.Contains("<label class=\"etiqueta\" for=\"Origen\">Unidad de origen</label>", html);
        Assert.Contains("<div class=\"selector\">", html);   // el contenedor dibuja la flecha
        Assert.Contains("<select class=\"campo\" id=\"Origen\" name=\"Origen\">", html);
        Assert.Contains("<option selected=\"selected\" value=\"Metro\">Metro</option>", html);
        Assert.Contains("<option value=\"metro\">metro</option>", html);   // la comparación distingue mayúsculas
        Assert.Equal(1, html.Split("selected=\"selected\"").Length - 1);
    }

    [Fact]
    public void Selector_sin_opciones_ni_seleccion_no_falla()
    {
        var html = Renderizar(new UiSelectorTagHelper { Id = "S", Etiqueta = "S", Invalido = true }, "ui-selector");

        Assert.Contains("<select class=\"campo campo--invalido\"", html);
        Assert.DoesNotContain("<option", html);
    }

    [Fact]
    public void Selector_escapa_etiqueta_y_opciones()
    {
        var html = Renderizar(new UiSelectorTagHelper { Id = "S", Etiqueta = Hostil, Opciones = [Hostil], Seleccionada = Hostil }, "ui-selector");

        NoHayHtmlInyectado(html);
        Assert.Contains("&lt;script&gt;", html);
    }

    // ---- ui-mensaje ----

    [Theory]
    [InlineData(null)]
    [InlineData("")]
    [InlineData("   ")]
    public void Mensaje_vacio_no_se_dibuja(string? texto)
    {
        Assert.Equal(string.Empty, Renderizar(new UiMensajeTagHelper { Mensaje = texto }, "ui-mensaje"));
    }

    [Theory]
    [InlineData("error", "panel-mensaje--error", true)]
    [InlineData("exito", "panel-mensaje--exito", false)]
    [InlineData("resultado", "panel-mensaje--resultado", false)]
    public void Mensaje_usa_la_clase_de_su_tipo_y_solo_el_error_es_una_alerta(string tipo, string clase, bool alerta)
    {
        var html = Renderizar(new UiMensajeTagHelper { Mensaje = "Listo", Tipo = tipo }, "ui-mensaje");

        Assert.Contains($"class=\"panel-mensaje {clase}\"", html);
        Assert.Contains(">Listo</div>", html);
        Assert.Equal(alerta, html.Contains("role=\"alert\""));
    }

    [Fact]
    public void Mensaje_escapa_el_texto()
    {
        var html = Renderizar(new UiMensajeTagHelper { Mensaje = Hostil }, "ui-mensaje");

        NoHayHtmlInyectado(html);
        Assert.Contains("&lt;script&gt;", html);
    }

    // ---- ui-boton ----

    [Fact]
    public void Boton_sin_href_es_un_boton_que_envia_el_formulario()
    {
        var html = Renderizar(new UiBotonTagHelper(), "ui-boton");

        Assert.StartsWith("<button", html);
        Assert.Contains("type=\"submit\"", html);
        Assert.Contains("class=\"boton boton--primario\"", html);
    }

    [Fact]
    public void Boton_con_href_es_un_enlace_secundario_de_salida_y_ancho_completo()
    {
        var html = Renderizar(new UiBotonTagHelper { Href = "/inicio", Tipo = "secundario", Completo = true, Salida = true }, "ui-boton");

        Assert.StartsWith("<a", html);
        Assert.Contains("href=\"/inicio\"", html);
        Assert.Contains("class=\"boton boton--secundario boton--completo boton--salida\"", html);
        Assert.DoesNotContain("type=", html);
    }

    [Fact]
    public void Boton_deshabilitado()
    {
        Assert.Contains("disabled=\"disabled\"", Renderizar(new UiBotonTagHelper { Deshabilitado = true }, "ui-boton"));
    }

    // ---- ui-lista-acciones ----

    /// <summary>Doble mínimo del generador de URL: arma «/controlador/accion?parametro=valor» por la ruta nombrada.</summary>
    private sealed class FabricaUrlFalsa : IUrlHelperFactory
    {
        public IUrlHelper GetUrlHelper(ActionContext contexto) => new UrlFalso(contexto);

        private sealed class UrlFalso(ActionContext contexto) : IUrlHelper
        {
            public ActionContext ActionContext => contexto;
            public string? Action(UrlActionContext c) => null;
            public string? Content(string? contentPath) => contentPath;
            public bool IsLocalUrl(string? url) => true;
            public string? Link(string? routeName, object? values) => null;

            public string? RouteUrl(UrlRouteContext c)
            {
                Assert.Equal("default", c.RouteName);   // por la ruta por defecto: el destino puede no existir todavía
                var valores = (IDictionary<string, object?>)c.Values!;
                var extra = valores.Where(v => v.Key is not ("controller" or "action"))
                    .Select(v => $"{v.Key}={Uri.EscapeDataString(v.Value?.ToString() ?? string.Empty)}");
                return $"/{valores["controller"]}/{valores["action"]}?{string.Join("&", extra)}";
            }
        }
    }

    private static UiListaAccionesTagHelper Lista(IEnumerable<string>? elementos) =>
        new(new FabricaUrlFalsa())
        {
            ViewContext = new ViewContext(),
            Elementos = elementos,
            Controlador = "Detalle",
            Accion = "Ver",
            Parametro = "clave"
        };

    [Fact]
    public void ListaAcciones_dibuja_un_boton_primario_de_ancho_completo_por_elemento()
    {
        var html = Renderizar(Lista(["Uno", "Dos"]), "ui-lista-acciones");

        Assert.Contains("<nav class=\"lista-acciones\">", html);
        Assert.Contains("<a class=\"boton boton--primario boton--completo\" href=\"/Detalle/Ver?clave=Uno\">Uno</a>", html);
        Assert.Contains("href=\"/Detalle/Ver?clave=Dos\">Dos</a>", html);
    }

    [Fact]
    public void ListaAcciones_sin_elementos_no_se_dibuja()
    {
        Assert.Equal(string.Empty, Renderizar(Lista(null), "ui-lista-acciones"));
        Assert.Equal(string.Empty, Renderizar(Lista([]), "ui-lista-acciones"));
    }

    [Fact]
    public void ListaAcciones_escapa_el_texto_y_el_valor_del_enlace()
    {
        var html = Renderizar(Lista([Hostil]), "ui-lista-acciones");

        NoHayHtmlInyectado(html);
        Assert.Contains("&lt;script&gt;", html);
        Assert.Contains("clave=%3Cscript%3E", html);
    }
}
