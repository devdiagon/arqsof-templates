namespace ec.edu.monster.excepciones;

public class ExcepcionServicio : Exception
{
    public const string CodigoRed = "RED";
    public const string CodigoTokenInvalido = "AUTH-002";
    public const string CodigoTokenVencido = "AUTH-003";

    public string Codigo { get; }

    public bool ExigeLogin => Codigo is CodigoTokenInvalido or CodigoTokenVencido;

    /// <summary>Sin respuesta del servidor: caído, sin red, URL equivocada, certificado no confiable o timeout.</summary>
    public bool ServidorNoDisponible => Codigo == CodigoRed;

    public ExcepcionServicio(string mensaje, string codigo, Exception? causa = null)
        : base(mensaje, causa)
    {
        Codigo = codigo;
    }
}
