using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Etiqueta + selector (lista desplegable) de CLIENT_STYLES_DOTNET.md §5.4 y §5.5. Las opciones las entrega el
/// controlador (nunca fijas en la vista) y se escapan siempre; el texto de cada opción es también su valor, tal como
/// sale del catálogo del servidor.
/// </summary>
[HtmlTargetElement("ui-selector", TagStructure = TagStructure.WithoutEndTag)]
public class UiSelectorTagHelper : TagHelper
{
    public string Id { get; set; } = string.Empty;
    public string Etiqueta { get; set; } = string.Empty;
    public IEnumerable<string>? Opciones { get; set; }
    public string? Seleccionada { get; set; }
    public bool Invalido { get; set; }

    public override void Process(TagHelperContext contexto, TagHelperOutput salida)
    {
        salida.TagName = null;   // sin envoltorio propio: la etiqueta y el selector se escriben tal cual

        var etiqueta = new TagBuilder("label");
        etiqueta.AddCssClass("etiqueta");
        etiqueta.Attributes["for"] = Id;
        etiqueta.InnerHtml.Append(Etiqueta);

        var lista = new TagBuilder("select");
        lista.AddCssClass(Invalido ? "campo campo--invalido" : "campo");
        lista.Attributes["id"] = Id;
        lista.Attributes["name"] = Id;
        foreach (var opcion in Opciones ?? [])
        {
            var item = new TagBuilder("option");
            item.Attributes["value"] = opcion;
            if (string.Equals(opcion, Seleccionada, StringComparison.Ordinal))
                item.Attributes["selected"] = "selected";
            item.InnerHtml.Append(opcion);
            lista.InnerHtml.AppendHtml(item);
        }

        // La flecha (24 × 24, #424242) la dibuja el CSS en el contenedor
        var contenedor = new TagBuilder("div");
        contenedor.AddCssClass("selector");
        contenedor.InnerHtml.AppendHtml(lista);

        salida.Content.AppendHtml(etiqueta);
        salida.Content.AppendHtml(contenedor);
    }
}
