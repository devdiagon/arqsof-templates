namespace ec.edu.monster.vista;

/// <summary>
/// Lo que muestra una <see cref="TarjetaResultado"/> (docs/CLIENT_STYLES_DOTNET.md §5.12): textos ya preparados, sin
/// formato ni dominio. <paramref name="Suma"/> es verdadero si el valor sube (monto en verde, con «+») y falso si baja
/// (monto en rojo, con «-»). Quien la crea (el controlador de un proyecto) decide los textos.
/// </summary>
public sealed record DatosTarjeta(string Titulo, bool Suma, string Monto, string Variacion, string Pie);
