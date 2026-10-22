using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>Etiqueta + campo de texto (CLIENT_STYLES_DOTNET.md §5.3 y §5.4). El valor y los textos se escapan siempre.</summary>
[HtmlTargetElement("ui-campo", TagStructure = TagStructure.WithoutEndTag)]
public class UiCampoTagHelper : TagHelper
{
    public string Id { get; set; } = string.Empty;
    public string Etiqueta { get; set; } = string.Empty;
    public string Tipo { get; set; } = "text";
    public string? Valor { get; set; }
    public string? Placeholder { get; set; }
    public string? Autocomplete { get; set; }
    public string? Inputmode { get; set; }
    public bool Invalido { get; set; }
    public bool Autofocus { get; set; }

    public override void Process(TagHelperContext contexto, TagHelperOutput salida)
    {
        salida.TagName = null;   // sin envoltorio: la etiqueta y el campo se escriben tal cual

        var etiqueta = new TagBuilder("label");
        etiqueta.AddCssClass("etiqueta");
        etiqueta.Attributes["for"] = Id;
        etiqueta.InnerHtml.Append(Etiqueta);

        var campo = new TagBuilder("input") { TagRenderMode = TagRenderMode.SelfClosing };
        campo.AddCssClass(Invalido ? "campo campo--invalido" : "campo");
        campo.Attributes["id"] = Id;
        campo.Attributes["name"] = Id;
        campo.Attributes["type"] = Tipo;
        if (Valor is not null) campo.Attributes["value"] = Valor;
        if (Placeholder is not null) campo.Attributes["placeholder"] = Placeholder;
        if (Autocomplete is not null) campo.Attributes["autocomplete"] = Autocomplete;
        if (Inputmode is not null) campo.Attributes["inputmode"] = Inputmode;
        if (Autofocus) campo.Attributes["autofocus"] = "autofocus";

        salida.Content.AppendHtml(etiqueta);
        salida.Content.AppendHtml(campo);
    }
}
