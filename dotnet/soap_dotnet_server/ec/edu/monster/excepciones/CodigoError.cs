namespace ec.edu.monster.excepciones;

/// <summary>
/// Catálogo de errores del servidor. Cada entrada define código, mensaje por defecto y categoría.
/// Un código nunca se reutiliza con otro significado.
/// Núcleo: DATA_001, ERR_404, ERR_405, ERR_415 y ERR_500 (no se quitan). Los tres ERR de plataforma no salen en el
/// cable SOAP (los errores de negocio viajan con HTTP 200), pero el catálogo es idéntico en las cuatro variantes
/// (ARQUITECTURA_SERVIDOR.md 4.3) y el servidor REST los usa (CONVENCION_REST.md 2.1).
/// Las entradas ELE_* son de EJEMPLO: sustituirlas por las del dominio.
/// Es una clase con instancias fijas y no un <c>enum</c>, porque un enum de C# no lleva datos.
/// </summary>
public sealed class CodigoError
{
    public enum CategoriaError
    {
        Validacion, NoEncontrado, Conflicto, OperacionInvalida, NoAutorizado,
        MetodoNoPermitido, TipoNoSoportado, Tecnico
    }

    public static readonly CodigoError DATA_001 = new("DATA-001", "Dato de entrada inválido", CategoriaError.Validacion);

    public static readonly CodigoError ELE_001 = new("ELE-001", "El elemento no existe", CategoriaError.NoEncontrado);
    public static readonly CodigoError ELE_002 = new("ELE-002", "El elemento ya existe", CategoriaError.Conflicto);

    public static readonly CodigoError ERR_404 = new("ERR-404", "Ruta no encontrada", CategoriaError.NoEncontrado);
    public static readonly CodigoError ERR_405 = new("ERR-405", "Método no permitido", CategoriaError.MetodoNoPermitido);
    public static readonly CodigoError ERR_415 = new("ERR-415", "Tipo de contenido no soportado", CategoriaError.TipoNoSoportado);
    public static readonly CodigoError ERR_500 = new("ERR-500", "Error interno del servidor", CategoriaError.Tecnico);

    /// <summary>Todas las entradas, en el orden del catálogo (equivale a <c>values()</c> de Java).</summary>
    public static readonly IReadOnlyList<CodigoError> Todos =
        [DATA_001, ELE_001, ELE_002, ERR_404, ERR_405, ERR_415, ERR_500];

    public string Codigo { get; }
    public string Mensaje { get; }
    public CategoriaError Categoria { get; }

    private CodigoError(string codigo, string mensaje, CategoriaError categoria)
    {
        Codigo = codigo;
        Mensaje = mensaje;
        Categoria = categoria;
    }

    public override string ToString() => Codigo;
}
