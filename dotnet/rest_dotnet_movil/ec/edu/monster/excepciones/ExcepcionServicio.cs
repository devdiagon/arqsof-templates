namespace ec.edu.monster.excepciones;

/// <summary>
/// Única excepción del cliente. Lleva el código (del servidor o propio del cliente) y el mensaje que se mostrará al usuario.
/// </summary>
public class ExcepcionServicio : Exception
{
    /// <summary>Códigos propios del cliente: nunca aparecen en el catálogo de errores del servidor.</summary>
    public const string Red = "RED";
    public const string Srv000 = "SRV-000";

    public const string MensajeRed = "Servidor no disponible, intente más tarde";
    public const string MensajeSrv000 = "No se pudo interpretar la respuesta del servidor";

    public string Codigo { get; }

    public ExcepcionServicio(string codigo, string mensaje) : base(mensaje)
    {
        Codigo = codigo;
    }

    public ExcepcionServicio(string codigo, string mensaje, Exception causa) : base(mensaje, causa)
    {
        Codigo = codigo;
    }

    /// <summary>Sin respuesta del servidor: caído, servicio no publicado, sin red o tiempo agotado.</summary>
    public bool ServidorNoDisponible => Codigo == Red;

    /// <summary>Respuesta que no se puede interpretar (vacía o nula).</summary>
    public bool RespuestaIlegible => Codigo == Srv000;
}
