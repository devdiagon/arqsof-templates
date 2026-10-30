using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Mvc.Routing;
using Microsoft.AspNetCore.Mvc.ViewFeatures;
using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Formulario POST de una pantalla con token anti-falsificación e indicador de progreso (CLIENT_STYLES_DOTNET.md §5.8).
/// <c>accion</c>: ruta de la aplicación, p. ej. <c>/consulta</c>. <c>ocupado</c>: texto mientras espera. <c>app.js</c>
/// deshabilita los controles y muestra el indicador al enviarse. <c>clase</c>: por defecto <c>formulario</c> (separa los
/// hijos 16).
/// </summary>
[HtmlTargetElement("ui-formulario")]
public class UiFormularioTagHelper(IHtmlGenerator generador, IUrlHelperFactory fabricaUrl) : TagHelper
{
    [ViewContext, HtmlAttributeNotBound]
    public ViewContext ViewContext { get; set; } = null!;

    public string Accion { get; set; } = string.Empty;
    public string? Ocupado { get; set; }
    public string Clase { get; set; } = "formulario";

    public override async Task ProcessAsync(TagHelperContext contexto, TagHelperOutput salida)
    {
        TagHelperContent hijos = await salida.GetChildContentAsync();

        salida.TagName = "form";
        salida.TagMode = TagMode.StartTagAndEndTag;
        salida.Attributes.SetAttribute("class", Clase);
        salida.Attributes.SetAttribute("method", "post");
        salida.Attributes.SetAttribute("action", fabricaUrl.GetUrlHelper(ViewContext).Content("~" + Accion));
        salida.Attributes.SetAttribute("data-ocupable", "");

        salida.Content.SetHtmlContent(hijos);
        salida.Content.AppendHtml(UiProgresoTagHelper.Construir(Ocupado));
        salida.Content.AppendHtml(generador.GenerateAntiforgery(ViewContext));
    }
}
