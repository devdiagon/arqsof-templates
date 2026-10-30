using ec.edu.monster.dto;
using ec.edu.monster.excepciones;

namespace ec.edu.monster.api;

/// <summary>
/// Decide el código HTTP de una respuesta (R7): 200 en todo éxito y, en un error, el HTTP de la
/// categoría de su entrada del catálogo. Nunca depende del código individual: agregar un error al catálogo
/// no exige tocar esta clase.
/// </summary>
public static class CodigoHttp
{
    public const int Ok = 200;

    public static int DeCategoria(CodigoError.CategoriaError categoria) => categoria switch
    {
        CodigoError.CategoriaError.Validacion => 400,
        CodigoError.CategoriaError.NoAutorizado => 401,
        CodigoError.CategoriaError.NoEncontrado => 404,
        CodigoError.CategoriaError.MetodoNoPermitido => 405,
        CodigoError.CategoriaError.Conflicto => 409,
        CodigoError.CategoriaError.TipoNoSoportado => 415,
        CodigoError.CategoriaError.OperacionInvalida => 422,
        _ => 500
    };

    /// <summary>HTTP de una respuesta ya mapeada: 200 si el estado es OK; si no, el de la categoría de su código.</summary>
    public static int DeRespuesta(string? estado, string? codigo)
    {
        if (estado == EstadoResultado.OK.ToString())
        {
            return Ok;
        }
        CodigoError? error = CodigoError.Todos.FirstOrDefault(e => e.Codigo == codigo);
        return DeCategoria(error?.Categoria ?? CodigoError.CategoriaError.Tecnico);
    }
}
