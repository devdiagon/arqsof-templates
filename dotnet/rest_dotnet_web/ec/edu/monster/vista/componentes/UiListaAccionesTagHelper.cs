using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Mvc.Routing;
using Microsoft.AspNetCore.Mvc.ViewFeatures;
using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Lista de acciones (CLIENT_STYLES_DOTNET.md §5.9): una columna de botones primarios de ancho completo, uno por elemento,
/// con datos que entrega el servidor (nunca fijos en la vista). Cada botón es un enlace a
/// <c>destino?parametro=elemento</c> (<c>destino</c>: ruta de la aplicación). Los textos y valores se escapan siempre.
/// </summary>
[HtmlTargetElement("ui-lista-acciones", TagStructure = TagStructure.WithoutEndTag)]
public class UiListaAccionesTagHelper(IUrlHelperFactory fabricaUrl) : TagHelper
{
    [ViewContext, HtmlAttributeNotBound]
    public ViewContext ViewContext { get; set; } = null!;

    public IEnumerable<string>? Elementos { get; set; }
    public string Destino { get; set; } = string.Empty;
    public string Parametro { get; set; } = string.Empty;

    public override void Process(TagHelperContext contexto, TagHelperOutput salida)
    {
        List<string> elementos = Elementos?.ToList() ?? [];
        if (elementos.Count == 0)
        {
            salida.SuppressOutput();
            return;
        }

        string baseUrl = fabricaUrl.GetUrlHelper(ViewContext).Content("~" + Destino);
        salida.TagName = "div";
        salida.TagMode = TagMode.StartTagAndEndTag;
        salida.Attributes.SetAttribute("class", "lista-acciones");

        foreach (string elemento in elementos)
        {
            var enlace = new TagBuilder("a");
            enlace.AddCssClass("boton boton--primario boton--ancho");
            enlace.Attributes["href"] = $"{baseUrl}?{Uri.EscapeDataString(Parametro)}={Uri.EscapeDataString(elemento)}";
            enlace.InnerHtml.Append(elemento);
            salida.Content.AppendHtml(enlace);
        }
    }
}
