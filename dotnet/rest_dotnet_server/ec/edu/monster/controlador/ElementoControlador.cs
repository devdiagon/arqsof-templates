using ec.edu.monster.dto;
using ec.edu.monster.modelo;
using ec.edu.monster.servicios;

namespace ec.edu.monster.controlador;

/// <summary>
/// EJEMPLO de controlador: valida la forma de las entradas (llegan como texto), las convierte a sus tipos reales,
/// delega en el servicio y devuelve siempre un Resultado.
/// </summary>
public class ElementoControlador : ControladorBase
{
    private readonly ElementoServicio _elementoServicio;

    public ElementoControlador(ElementoServicio elementoServicio)
    {
        _elementoServicio = elementoServicio;
    }

    public Resultado<Elemento> Consultar(string? codigo) =>
        Ejecutar(() => _elementoServicio.Consultar(Codigo(codigo)));

    public Resultado<List<Elemento>> Listar() =>
        Ejecutar(() => _elementoServicio.Listar());

    public Resultado<Elemento> Registrar(string? codigo, string? nombre, string? valor) =>
        Ejecutar(() =>
        {
            string codigoElemento = Codigo(codigo);
            string nombreElemento = ValidadorForma.TextoHasta(nombre, "nombre", Elemento.LongitudMaximaNombre);
            decimal valorElemento = ValidadorForma.DecimalPositivo(valor, "valor",
                Elemento.MaxEnterosValor, Elemento.MaxDecimalesValor);
            return _elementoServicio.Registrar(codigoElemento, nombreElemento, valorElemento);
        });

    private static string Codigo(string? valor) =>
        ValidadorForma.DigitosExactos(valor, "código", Elemento.LongitudCodigo);
}
