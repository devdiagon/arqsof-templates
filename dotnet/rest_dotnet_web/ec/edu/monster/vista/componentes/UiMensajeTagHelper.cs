using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Panel de mensaje o resultado (CLIENT_STYLES_DOTNET.md §5.2); no se dibuja si no hay texto. Solo el mensaje del sistema,
/// nunca un código técnico. <c>tipo</c>: <c>error</c> (por defecto), <c>exito</c> o <c>resultado</c> (centrado, tamaño de
/// resultado). El texto se escapa siempre (nunca <c>Html.Raw</c>).
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

        (string clase, string rol) = Tipo switch
        {
            "resultado" => ("mensaje mensaje--resultado", "status"),
            "exito" => ("mensaje mensaje--exito", "status"),
            _ => ("mensaje", "alert")
        };

        salida.TagName = "div";
        salida.TagMode = TagMode.StartTagAndEndTag;
        salida.Attributes.SetAttribute("class", clase);
        salida.Attributes.SetAttribute("role", rol);
        salida.Content.SetContent(Mensaje);
    }
}
