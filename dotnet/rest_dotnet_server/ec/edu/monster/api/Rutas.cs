using ec.edu.monster.api.contrato;

namespace ec.edu.monster.api;

/// <summary>
/// Tabla de endpoints del proyecto (docs/ENDPOINTS_REST.md), bajo el prefijo común (R1). Sin lógica: cada ruta
/// delega en un método de un recurso. Una fila de ENDPOINTS_REST.md = una línea aquí.
/// Los parámetros de ruta se declaran como <c>string</c>: el controlador los valida (DATA-001), no el enrutador.
/// EJEMPLO: sustituir por las rutas del dominio.
/// </summary>
public static class Rutas
{
    public static void Mapear(IEndpointRouteBuilder rutas)
    {
        RouteGroupBuilder api = rutas.MapGroup(ConvencionRest.Prefijo);

        api.MapGet("elementos",
            (ElementoRecurso r) => r.ListarElementos());
        api.MapGet("elementos/{codigoElemento}",
            (ElementoRecurso r, string codigoElemento) => r.ConsultarElemento(codigoElemento));
        api.MapPost("elementos",
            (ElementoRecurso r, HttpRequest p) => r.RegistrarElemento(p));
    }
}
