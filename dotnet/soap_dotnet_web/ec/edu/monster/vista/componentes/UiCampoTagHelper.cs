using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Etiqueta + campo de texto (CLIENT_STYLES_DOTNET.md §5.3 y §5.4): texto, contraseña... El valor y los textos se escapan
/// siempre. <c>nombre</c> es el nombre del parámetro (por defecto, el <c>id</c>); <c>inputmode="decimal"</c> abre el teclado
/// numérico en el móvil y el campo sigue siendo texto; <c>centrado</c>: ancho reducido (un valor corto).
/// Un campo de contraseña nunca recibe <c>valor</c>: no se devuelve a la página.
/// </summary>
[HtmlTargetElement("ui-campo", TagStructure = TagStructure.WithoutEndTag)]
public class UiCampoTagHelper : TagHelper
{
    public string Id { get; set; } = string.Empty;
    public string? Nombre { get; set; }
    public string? Etiqueta { get; set; }
    public string Tipo { get; set; } = "text";
    public string? Valor { get; set; }
    public string? Placeholder { get; set; }
    public string Autocomplete { get; set; } = "off";
    public string Inputmode { get; set; } = "text";
    public bool Invalido { get; set; }
    public bool Autofocus { get; set; }
    public bool Centrado { get; set; }

    public override void Process(TagHelperContext contexto, TagHelperOutput salida)
    {
        salida.TagName = "div";
        salida.TagMode = TagMode.StartTagAndEndTag;
        if (Centrado)
        {
            salida.Attributes.SetAttribute("class", "campo-centrado");
        }

        if (!string.IsNullOrEmpty(Etiqueta))
        {
            var etiqueta = new TagBuilder("label");
            etiqueta.AddCssClass("etiqueta");
            etiqueta.Attributes["for"] = Id;
            etiqueta.InnerHtml.Append(Etiqueta);
            salida.Content.AppendHtml(etiqueta);
        }

        var campo = new TagBuilder("input") { TagRenderMode = TagRenderMode.SelfClosing };
        campo.AddCssClass(Invalido ? "campo campo--error" : "campo");
        campo.Attributes["type"] = Tipo;
        campo.Attributes["id"] = Id;
        campo.Attributes["name"] = string.IsNullOrEmpty(Nombre) ? Id : Nombre;
        campo.Attributes["autocomplete"] = Autocomplete;
        campo.Attributes["inputmode"] = Inputmode;
        if (Autofocus) campo.Attributes["autofocus"] = "autofocus";
        if (!string.IsNullOrEmpty(Placeholder)) campo.Attributes["placeholder"] = Placeholder;
        campo.Attributes["aria-label"] = (string.IsNullOrEmpty(Etiqueta) ? Placeholder : Etiqueta) ?? string.Empty;
        campo.Attributes["value"] = Valor ?? string.Empty;
        salida.Content.AppendHtml(campo);
    }
}
