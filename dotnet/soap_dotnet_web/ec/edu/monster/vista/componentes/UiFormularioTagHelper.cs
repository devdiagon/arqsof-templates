using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Mvc.Routing;
using Microsoft.AspNetCore.Mvc.ViewFeatures;
using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Formulario POST con token anti-falsificación e indicador de progreso (CLIENT_STYLES_DOTNET.md §5.8).
/// <c>app.js</c> deshabilita los controles y muestra el indicador mientras se envía.
/// </summary>
[HtmlTargetElement("ui-formulario")]
public class UiFormularioTagHelper(IHtmlGenerator generador, IUrlHelperFactory fabricaUrl) : TagHelper
{
    [ViewContext, HtmlAttributeNotBound]
    public ViewContext ViewContext { get; set; } = null!;

    public string Accion { get; set; } = string.Empty;
    public string? Controlador { get; set; }

    public override async Task ProcessAsync(TagHelperContext contexto, TagHelperOutput salida)
    {
        var hijos = await salida.GetChildContentAsync();
        var url = fabricaUrl.GetUrlHelper(ViewContext).Action(Accion, Controlador);

        salida.TagName = "form";
        salida.TagMode = TagMode.StartTagAndEndTag;
        salida.Attributes.SetAttribute("method", "post");
        salida.Attributes.SetAttribute("action", url);
        salida.Attributes.SetAttribute("class", "formulario");
        salida.Attributes.SetAttribute("data-formulario", "");

        salida.Content.SetHtmlContent("<fieldset class=\"formulario__controles\">");
        salida.Content.AppendHtml(hijos);
        salida.Content.AppendHtml("</fieldset><div class=\"progreso\" role=\"status\" aria-label=\"Cargando\" hidden></div>");
        salida.Content.AppendHtml(generador.GenerateAntiforgery(ViewContext));
    }
}
