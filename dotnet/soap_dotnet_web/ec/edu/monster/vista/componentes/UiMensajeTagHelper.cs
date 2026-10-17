using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Panel de mensaje o resultado (CLIENT_STYLES_DOTNET.md §5.2); no se dibuja si no hay texto.
/// <c>tipo</c>: <c>error</c> (por defecto), <c>exito</c> o <c>resultado</c>. El texto se escapa (nunca Html.Raw).
/// </summary>
[HtmlTargetElement("ui-mensaje", TagStructure = TagStructure.WithoutEndTag)]
public class UiMensajeTagHelper : TagHelper
{
    public string? Mensaje { get; set; }
    public string Tipo { get; set; } = "error";

    public override void Process(TagHelperContext contexto, TagHelperOutput salida)
    {
        if (string.IsNullOrWhiteSpace(Mensaje))
        {
            salida.SuppressOutput();
            return;
        }

        salida.TagName = "div";
        salida.TagMode = TagMode.StartTagAndEndTag;
        salida.Attributes.SetAttribute("class", $"panel-mensaje panel-mensaje--{Tipo}");
        if (Tipo == "error") salida.Attributes.SetAttribute("role", "alert");
        salida.Content.SetContent(Mensaje);
    }
}
