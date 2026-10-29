namespace ec.edu.monster.modelo;

/// <summary>
/// EJEMPLO de entidad (tabla elemento). Sustituir por las entidades del dominio.
/// Los formatos del dominio se declaran aquí como constantes y el controlador los combina con ValidadorForma.
/// </summary>
public record Elemento(
    string Codigo,
    string Nombre,
    decimal Valor)
{
    public const int LongitudCodigo = 4;
    public const int LongitudMaximaNombre = 40;
    public const int MaxEnterosValor = 10;
    public const int MaxDecimalesValor = 2;
}
