namespace ec.edu.monster.excepciones;

/// <summary>
/// Única excepción de negocio. Transporta una entrada del catálogo y, opcionalmente,
/// un mensaje específico que reemplaza al mensaje por defecto.
/// </summary>
public class ExcepcionMonster : Exception
{
    public CodigoError Error { get; }

    public ExcepcionMonster(CodigoError error) : base(error.Mensaje)
    {
        Error = error;
    }

    public ExcepcionMonster(CodigoError error, string mensaje) : base(mensaje)
    {
        Error = error;
    }

    public string Codigo => Error.Codigo;

    public CodigoError.CategoriaError Categoria => Error.Categoria;
}
