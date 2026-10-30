using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Indicador de progreso con su texto (CLIENT_STYLES_DOTNET.md §5.8). Oculto hasta que <c>app.js</c> marca el formulario
/// como ocupado; ocupa el lugar del mensaje. Lo incluye <c>ui-formulario</c>. <c>texto</c>: <c>Procesando…</c> (por defecto),
/// <c>Consultando…</c> o <c>Iniciando sesión…</c>.
/// </summary>
[HtmlTargetElement("ui-progreso", TagStructure = TagStructure.WithoutEndTag)]
public class UiProgresoTagHelper : TagHelper
{
    public string? Texto { get; set; }

    public override void Process(TagHelperContext contexto, TagHelperOutput salida)
    {
        salida.TagName = "div";
        salida.TagMode = TagMode.StartTagAndEndTag;
        salida.Attributes.SetAttribute("class", "progreso");
        salida.Attributes.SetAttribute("role", "status");
        salida.Content.AppendHtml(Giro());
        salida.Content.AppendHtml(Etiqueta(Texto));
    }

    /// <summary>El indicador completo, para quien lo incluye dentro de otro componente (<c>ui-formulario</c>).</summary>
    internal static TagBuilder Construir(string? texto)
    {
        var progreso = new TagBuilder("div");
        progreso.AddCssClass("progreso");
        progreso.Attributes["role"] = "status";
        progreso.InnerHtml.AppendHtml(Giro());
        progreso.InnerHtml.AppendHtml(Etiqueta(texto));
        return progreso;
    }

    private static TagBuilder Giro()
    {
        var giro = new TagBuilder("span");
        giro.AddCssClass("progreso__giro");
        giro.Attributes["aria-hidden"] = "true";
        return giro;
    }

    private static TagBuilder Etiqueta(string? texto)
    {
        var etiqueta = new TagBuilder("span");
        etiqueta.AddCssClass("progreso__texto");
        etiqueta.InnerHtml.Append(string.IsNullOrEmpty(texto) ? Textos.Procesando : texto);
        return etiqueta;
    }
}
