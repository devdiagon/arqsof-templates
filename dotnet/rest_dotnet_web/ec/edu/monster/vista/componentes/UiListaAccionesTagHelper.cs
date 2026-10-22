using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Mvc.Routing;
using Microsoft.AspNetCore.Mvc.ViewFeatures;
using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Lista de acciones (CLIENT_STYLES_DOTNET.md §5.9): una columna de botones primarios de ancho completo, uno por
/// elemento, con datos que entrega el servidor (nunca fijos en la vista). Cada botón es un enlace a
/// <c>controlador/accion?parametro=elemento</c>. Los textos y valores se escapan siempre.
/// </summary>
[HtmlTargetElement("ui-lista-acciones", TagStructure = TagStructure.WithoutEndTag)]
public class UiListaAccionesTagHelper(IUrlHelperFactory fabricaUrl) : TagHelper
{
    [ViewContext, HtmlAttributeNotBound]
    public ViewContext ViewContext { get; set; } = null!;

    public IEnumerable<string>? Elementos { get; set; }
    public string Controlador { get; set; } = string.Empty;
    public string Accion { get; set; } = string.Empty;
    public string Parametro { get; set; } = string.Empty;

    public override void Process(TagHelperContext contexto, TagHelperOutput salida)
    {
        var elementos = Elementos?.ToList() ?? [];
        if (elementos.Count == 0)
        {
            salida.SuppressOutput();
            return;
        }

        var url = fabricaUrl.GetUrlHelper(ViewContext);
        salida.TagName = "nav";
        salida.TagMode = TagMode.StartTagAndEndTag;
        salida.Attributes.SetAttribute("class", "lista-acciones");

        foreach (var elemento in elementos)
        {
            // Por la ruta "default": el destino puede crearse después sin tocar este componente
            var href = url.RouteUrl(new UrlRouteContext
            {
                RouteName = "default",
                Values = new Dictionary<string, object?>
                {
                    ["controller"] = Controlador,
                    ["action"] = Accion,
                    [Parametro] = elemento
                }
            });

            var enlace = new TagBuilder("a");
            enlace.AddCssClass("boton boton--primario boton--completo");
            enlace.Attributes["href"] = href;
            enlace.InnerHtml.Append(elemento);
            salida.Content.AppendHtml(enlace);
        }
    }
}
