using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>Encabezado de pantalla (CLIENT_STYLES_DOTNET.md §5.1). <c>aplicacion="true"</c> en el acceso: título de la aplicación, centrado.</summary>
[HtmlTargetElement("ui-encabezado")]
public class UiEncabezadoTagHelper : TagHelper
{
    public bool Aplicacion { get; set; }

    public override void Process(TagHelperContext contexto, TagHelperOutput salida)
    {
        salida.TagName = "div";
        salida.TagMode = TagMode.StartTagAndEndTag;
        salida.Attributes.SetAttribute("class", "panel-encabezado");
        salida.PreContent.SetHtmlContent(Aplicacion ? "<h1 class=\"texto-titulo-app\">" : "<h1 class=\"texto-titulo\">");
        salida.PostContent.SetHtmlContent("</h1>");
    }
}
