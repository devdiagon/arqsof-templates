using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Cuadrícula de acciones (CLIENT_STYLES_DOTNET.md §5.11): botones primarios grandes en dos columnas (una bajo 600 px), sin
/// superponerse. El cuerpo son <c>ui-boton ancho="true"</c>.
/// </summary>
[HtmlTargetElement("ui-cuadricula-acciones")]
public class UiCuadriculaAccionesTagHelper : TagHelper
{
    public override void Process(TagHelperContext contexto, TagHelperOutput salida) =>
        ContenedoresTagHelpers.Envolver(salida, "cuadricula-acciones");
}

/// <summary>
/// Fila de botones ajustados a su texto, centrados y separados 16: la acción principal y Cancelar. Bajo 600 px se apilan a
/// ancho completo. El cuerpo son <c>ui-boton</c>.
/// </summary>
[HtmlTargetElement("ui-fila-botones")]
public class UiFilaBotonesTagHelper : TagHelper
{
    public override void Process(TagHelperContext contexto, TagHelperOutput salida) =>
        ContenedoresTagHelpers.Envolver(salida, "fila-botones");
}

/// <summary>
/// Dos campos en horizontal con una flecha entre ambos (el de la izquierda apunta al de la derecha). Bajo 600 px se apilan y
/// la flecha apunta hacia abajo. El cuerpo son dos <c>ui-campo</c>; la flecha la dibuja el CSS.
/// </summary>
[HtmlTargetElement("ui-fila-campos")]
public class UiFilaCamposTagHelper : TagHelper
{
    public override void Process(TagHelperContext contexto, TagHelperOutput salida) =>
        ContenedoresTagHelpers.Envolver(salida, "fila-campos");
}

/// <summary>
/// Un campo con sus botones a la derecha, alineados con su base. Bajo 600 px los botones pasan debajo. El cuerpo es un
/// <c>ui-campo</c> seguido de un <c>ui-fila-botones</c>.
/// </summary>
[HtmlTargetElement("ui-campo-con-acciones")]
public class UiCampoConAccionesTagHelper : TagHelper
{
    public override void Process(TagHelperContext contexto, TagHelperOutput salida) =>
        ContenedoresTagHelpers.Envolver(salida, "campo-con-acciones");
}

/// <summary>Fila de tarjetas de resultado, centradas y separadas 16. Se apilan cuando no caben. El cuerpo son <c>ui-tarjeta</c>.</summary>
[HtmlTargetElement("ui-tarjetas")]
public class UiTarjetasTagHelper : TagHelper
{
    public override void Process(TagHelperContext contexto, TagHelperOutput salida) =>
        ContenedoresTagHelpers.Envolver(salida, "tarjetas");
}

internal static class ContenedoresTagHelpers
{
    /// <summary>Convierte la etiqueta en un <c>div</c> con la clase indicada y deja su cuerpo tal cual.</summary>
    internal static void Envolver(TagHelperOutput salida, string clase)
    {
        salida.TagName = "div";
        salida.TagMode = TagMode.StartTagAndEndTag;
        salida.Attributes.SetAttribute("class", clase);
    }
}
