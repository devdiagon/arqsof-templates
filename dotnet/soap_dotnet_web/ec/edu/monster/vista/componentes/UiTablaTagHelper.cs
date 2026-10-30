using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ec.edu.monster.vista.componentes;

/// <summary>
/// Tabla de registros (CLIENT_STYLES_DOTNET.md §5.13): encabezado fijo, filas de 36, columnas numéricas a la derecha. Se
/// desplaza dentro de su contenedor: la página nunca se desplaza en horizontal. <c>columnas</c>: los títulos;
/// <c>alDerecha</c>: por columna, si va a la derecha; <c>filas</c>: una lista de textos por fila, uno por columna;
/// <c>titulo</c> y <c>cantidad</c>: la línea sobre la tabla. Todo texto se escapa.
/// </summary>
[HtmlTargetElement("ui-tabla", TagStructure = TagStructure.WithoutEndTag)]
public class UiTablaTagHelper : TagHelper
{
    public IReadOnlyList<string> Columnas { get; set; } = [];
    public IReadOnlyList<bool> AlDerecha { get; set; } = [];
    public IEnumerable<IReadOnlyList<string>> Filas { get; set; } = [];
    public string? Titulo { get; set; }
    public string? Cantidad { get; set; }

    public override void Process(TagHelperContext contexto, TagHelperOutput salida)
    {
        salida.TagName = "div";
        salida.TagMode = TagMode.StartTagAndEndTag;
        salida.Attributes.SetAttribute("class", "tabla-bloque");

        if (!string.IsNullOrEmpty(Titulo))
        {
            var resumen = new TagBuilder("div");
            resumen.AddCssClass("tabla-resumen");
            var titulo = new TagBuilder("strong");
            titulo.InnerHtml.Append(Titulo);
            var cantidad = new TagBuilder("span");
            cantidad.InnerHtml.Append(Cantidad ?? string.Empty);
            resumen.InnerHtml.AppendHtml(titulo);
            resumen.InnerHtml.AppendHtml(cantidad);
            salida.Content.AppendHtml(resumen);
        }

        var encabezado = new TagBuilder("tr");
        for (int c = 0; c < Columnas.Count; c++)
        {
            var th = new TagBuilder("th");
            th.Attributes["scope"] = "col";
            if (EsNumerica(c)) th.AddCssClass("num");
            th.InnerHtml.Append(Columnas[c]);
            encabezado.InnerHtml.AppendHtml(th);
        }
        var thead = new TagBuilder("thead");
        thead.InnerHtml.AppendHtml(encabezado);

        var tbody = new TagBuilder("tbody");
        foreach (IReadOnlyList<string> fila in Filas)
        {
            var tr = new TagBuilder("tr");
            for (int c = 0; c < fila.Count; c++)
            {
                var td = new TagBuilder("td");
                if (EsNumerica(c)) td.AddCssClass("num");
                td.InnerHtml.Append(fila[c]);
                tr.InnerHtml.AppendHtml(td);
            }
            tbody.InnerHtml.AppendHtml(tr);
        }

        var tabla = new TagBuilder("table");
        tabla.AddCssClass("tabla");
        tabla.InnerHtml.AppendHtml(thead);
        tabla.InnerHtml.AppendHtml(tbody);
        var contenedor = new TagBuilder("div");
        contenedor.AddCssClass("tabla-contenedor");
        contenedor.InnerHtml.AppendHtml(tabla);
        salida.Content.AppendHtml(contenedor);
    }

    private bool EsNumerica(int columna) => columna < AlDerecha.Count && AlDerecha[columna];
}
