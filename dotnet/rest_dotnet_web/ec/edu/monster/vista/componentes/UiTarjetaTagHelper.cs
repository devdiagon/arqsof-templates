using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Tarjeta de resultado (CLIENT_STYLES_DOTNET.md §5.12): resume el cambio de un valor. Título centrado, monto con signo
/// (verde si suma, rojo si resta), variación, divisor y pie a la derecha. Recibe los textos ya preparados; no conoce el
/// dominio ni el formato de los valores. Todo texto se escapa.
/// </summary>
[HtmlTargetElement("ui-tarjeta", TagStructure = TagStructure.WithoutEndTag)]
public class UiTarjetaTagHelper : TagHelper
{
    public string? Titulo { get; set; }
    public bool Suma { get; set; }
    public string Monto { get; set; } = string.Empty;
    public string Variacion { get; set; } = string.Empty;
    public string Pie { get; set; } = string.Empty;

    public override void Process(TagHelperContext contexto, TagHelperOutput salida)
    {
        salida.TagName = "div";
        salida.TagMode = TagMode.StartTagAndEndTag;
        salida.Attributes.SetAttribute("class", "tarjeta");
        salida.Attributes.SetAttribute("role", "group");

        if (!string.IsNullOrEmpty(Titulo))
        {
            salida.Content.AppendHtml(Parte("tarjeta__titulo", Titulo));
        }
        salida.Content.AppendHtml(Parte($"tarjeta__monto {(Suma ? "tarjeta__monto--suma" : "tarjeta__monto--resta")}", Monto));
        salida.Content.AppendHtml(Parte("tarjeta__variacion", Variacion));
        salida.Content.AppendHtml(Parte("tarjeta__pie", Pie));
    }

    private static TagBuilder Parte(string clase, string texto)
    {
        var parte = new TagBuilder("div");
        parte.AddCssClass(clase);
        parte.InnerHtml.Append(texto);
        return parte;
    }
}
