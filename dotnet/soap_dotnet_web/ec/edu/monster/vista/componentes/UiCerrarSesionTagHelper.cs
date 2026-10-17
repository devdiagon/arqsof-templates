using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Mvc.Routing;
using Microsoft.AspNetCore.Mvc.ViewFeatures;
using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>Botón secundario de salida separado 32 (CLIENT_STYLES_DOTNET.md §5.7). Cerrar sesión es un POST con token anti-falsificación.</summary>
[HtmlTargetElement("ui-cerrar-sesion", TagStructure = TagStructure.WithoutEndTag)]
public class UiCerrarSesionTagHelper(IHtmlGenerator generador, IUrlHelperFactory fabricaUrl) : TagHelper
{
    [ViewContext, HtmlAttributeNotBound]
    public ViewContext ViewContext { get; set; } = null!;

    public override void Process(TagHelperContext contexto, TagHelperOutput salida)
    {
        var url = fabricaUrl.GetUrlHelper(ViewContext).Action("CerrarSesion", "Acceso");

        salida.TagName = "form";
        salida.TagMode = TagMode.StartTagAndEndTag;
        salida.Attributes.SetAttribute("method", "post");
        salida.Attributes.SetAttribute("action", url);
        salida.Attributes.SetAttribute("class", "formulario");
        salida.Content.SetHtmlContent("<button type=\"submit\" class=\"boton boton--secundario boton--completo boton--salida\">Cerrar sesión</button>");
        salida.Content.AppendHtml(generador.GenerateAntiforgery(ViewContext));
    }
}
