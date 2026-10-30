using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Etiqueta + selector (lista desplegable) de CLIENT_STYLES_DOTNET.md §5.4 y §5.5. Las opciones las entrega el controlador
/// (nunca fijas en la vista) y se escapan siempre; el texto de cada opción es también su valor, tal como lo entregó el
/// servidor. La flecha (24 × 24, <c>#424242</c>) la dibuja el CSS.
/// </summary>
[HtmlTargetElement("ui-selector", TagStructure = TagStructure.WithoutEndTag)]
public class UiSelectorTagHelper : TagHelper
{
    public string Id { get; set; } = string.Empty;
    public string? Nombre { get; set; }
    public string? Etiqueta { get; set; }
    public IEnumerable<string>? Opciones { get; set; }
    public string? Seleccionada { get; set; }
    public bool Invalido { get; set; }

    public override void Process(TagHelperContext contexto, TagHelperOutput salida)
    {
        salida.TagName = "div";
        salida.TagMode = TagMode.StartTagAndEndTag;

        if (!string.IsNullOrEmpty(Etiqueta))
        {
            var etiqueta = new TagBuilder("label");
            etiqueta.AddCssClass("etiqueta");
            etiqueta.Attributes["for"] = Id;
            etiqueta.InnerHtml.Append(Etiqueta);
            salida.Content.AppendHtml(etiqueta);
        }

        var lista = new TagBuilder("select");
        lista.AddCssClass(Invalido ? "campo campo--error" : "campo");
        lista.Attributes["id"] = Id;
        lista.Attributes["name"] = string.IsNullOrEmpty(Nombre) ? Id : Nombre;
        foreach (string opcion in Opciones ?? [])
        {
            var item = new TagBuilder("option");
            item.Attributes["value"] = opcion;
            if (string.Equals(opcion, Seleccionada, StringComparison.Ordinal))
            {
                item.Attributes["selected"] = "selected";
            }
            item.InnerHtml.Append(opcion);
            lista.InnerHtml.AppendHtml(item);
        }
        salida.Content.AppendHtml(lista);
    }
}
