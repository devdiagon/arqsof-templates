using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Botón primario o secundario (CLIENT_STYLES_DOTNET.md §5.6 y §5.7). Con <c>href</c> es un enlace; sin él, envía el formulario.
/// <c>completo</c>: ancho 100 % (acciones de lista y de salida); <c>salida</c>: separado 32 del bloque anterior.
/// </summary>
[HtmlTargetElement("ui-boton")]
public class UiBotonTagHelper : TagHelper
{
    public string Tipo { get; set; } = "primario";
    public string? Href { get; set; }
    public bool Completo { get; set; }
    public bool Salida { get; set; }
    public bool Deshabilitado { get; set; }

    public override void Process(TagHelperContext contexto, TagHelperOutput salida)
    {
        var clases = $"boton boton--{Tipo}";
        if (Completo) clases += " boton--completo";
        if (Salida) clases += " boton--salida";

        salida.TagMode = TagMode.StartTagAndEndTag;
        salida.Attributes.SetAttribute("class", clases);
        if (Href is not null)
        {
            salida.TagName = "a";
            salida.Attributes.SetAttribute("href", Href);
        }
        else
        {
            salida.TagName = "button";
            salida.Attributes.SetAttribute("type", "submit");
            if (Deshabilitado) salida.Attributes.SetAttribute("disabled", "disabled");
        }
    }
}
