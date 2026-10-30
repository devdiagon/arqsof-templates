using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Encabezado de pantalla (CLIENT_STYLES_DOTNET.md §5.1): panel con un único texto, siempre arriba y sin botones.
/// <c>aplicacion="true"</c> en el acceso: título de la aplicación, centrado y más grande. <c>plano="true"</c>: texto grande
/// en negrita sin panel (el saludo del menú).
/// </summary>
[HtmlTargetElement("ui-encabezado")]
public class UiEncabezadoTagHelper : TagHelper
{
    public bool Aplicacion { get; set; }
    public bool Plano { get; set; }

    public override void Process(TagHelperContext contexto, TagHelperOutput salida)
    {
        string clases = "encabezado";
        if (Aplicacion) clases += " encabezado--aplicacion";
        if (Plano) clases += " encabezado--plano";

        salida.TagName = "h1";
        salida.TagMode = TagMode.StartTagAndEndTag;
        salida.Attributes.SetAttribute("class", clases);
    }
}
