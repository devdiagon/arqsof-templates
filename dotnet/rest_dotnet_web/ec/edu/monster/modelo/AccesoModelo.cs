namespace ec.edu.monster.modelo;

/// <summary>Modelo de la pantalla de acceso. La contraseña llega en el POST pero nunca se devuelve a la vista.</summary>
public class AccesoModelo
{
    public string Usuario { get; set; } = string.Empty;

    public string Contrasenia { get; set; } = string.Empty;

    /// <summary>Solo el mensaje del servidor o del cliente, nunca el código.</summary>
    public string? Mensaje { get; set; }
}
