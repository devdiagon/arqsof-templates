namespace ec.edu.monster.excepciones;

public class ExcepcionServicio : Exception
{
    public const string CodigoRed = "RED";
    public const string CodigoTokenInvalido = "AUTH-002";
    public const string CodigoTokenVencido = "AUTH-003";

    public string Codigo { get; }

    /// <summary>Código HTTP de la respuesta; null si no hubo respuesta o el error es del cliente.</summary>
    public int? EstadoHttp { get; }

    /// <summary>Sin respuesta del servidor: caído, sin red, URL equivocada o tiempo agotado.</summary>
    public bool ServidorNoDisponible { get; }

    public bool ExigeLogin => Codigo is CodigoTokenInvalido or CodigoTokenVencido;

    public ExcepcionServicio(string mensaje, string codigo, int? estadoHttp = null,
        bool servidorNoDisponible = false, Exception? causa = null)
        : base(mensaje, causa)
    {
        Codigo = codigo;
        EstadoHttp = estadoHttp;
        ServidorNoDisponible = servidorNoDisponible;
    }
}
