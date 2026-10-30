using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Mvc.Routing;
using Microsoft.AspNetCore.Mvc.ViewFeatures;
using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Botón primario o secundario (CLIENT_STYLES_DOTNET.md §5.6 y §5.7). Con <c>href</c> (ruta de la aplicación, p. ej.
/// <c>/menu</c>) es un enlace; sin él, envía el formulario. <c>ancho</c>: 100 % del contenido (acciones de lista y de
/// salida); <c>centrado</c>: ajustado al texto y centrado (la acción principal de un formulario); <c>salida</c>: separado 32
/// del bloque anterior; <c>cancelar</c>: es el botón Cancelar de la pantalla, que también activa la tecla Esc.
/// </summary>
[HtmlTargetElement("ui-boton")]
public class UiBotonTagHelper(IUrlHelperFactory fabricaUrl) : TagHelper
{
    [ViewContext, HtmlAttributeNotBound]
    public ViewContext ViewContext { get; set; } = null!;

    public string Tipo { get; set; } = "primario";
    public string? Href { get; set; }
    public bool Ancho { get; set; }
    public bool Centrado { get; set; }
    public bool Salida { get; set; }
    public bool Cancelar { get; set; }
    public bool Deshabilitado { get; set; }

    public override void Process(TagHelperContext contexto, TagHelperOutput salida)
    {
        string clases = $"boton boton--{Tipo}";
        if (Ancho) clases += " boton--ancho";

        salida.TagMode = TagMode.StartTagAndEndTag;
        salida.Attributes.SetAttribute("class", clases);
        if (Href is not null)
        {
            salida.TagName = "a";
            salida.Attributes.SetAttribute("href", fabricaUrl.GetUrlHelper(ViewContext).Content("~" + Href));
            if (Cancelar) salida.Attributes.SetAttribute("data-cancelar", "");
        }
        else
        {
            salida.TagName = "button";
            salida.Attributes.SetAttribute("type", "submit");
            if (Deshabilitado) salida.Attributes.SetAttribute("disabled", "disabled");
        }

        // Envolturas: la acción principal centrada y/o separada 32 de lo anterior (se cierran en orden inverso)
        string abre = (Salida ? "<div class=\"acciones-salida\">" : "") + (Centrado ? "<div class=\"accion-principal\">" : "");
        string cierra = (Centrado ? "</div>" : "") + (Salida ? "</div>" : "");
        if (abre.Length > 0)
        {
            salida.PreElement.SetHtmlContent(abre);
            salida.PostElement.SetHtmlContent(cierra);
        }
    }
}
