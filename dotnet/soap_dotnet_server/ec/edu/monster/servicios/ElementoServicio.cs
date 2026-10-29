using ec.edu.monster.db;
using ec.edu.monster.excepciones;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicios;

/// <summary>
/// EJEMPLO de servicio: reglas de negocio, cálculos puros y el patrón único de conexión y transacción
/// (<see cref="Transaccional"/>). No conoce SOAP ni los tipos de contrato.
/// Las operaciones públicas son <c>virtual</c> para poder sustituirlas con Moq en las pruebas.
/// </summary>
public class ElementoServicio
{
    private readonly AccesoDB _accesoDB;
    private readonly IElementoRepositorio _elementoRepositorio;

    public ElementoServicio(AccesoDB accesoDB, IElementoRepositorio elementoRepositorio)
    {
        _accesoDB = accesoDB;
        _elementoRepositorio = elementoRepositorio;
    }

    // ------------------------------------------------------------------ operaciones

    public virtual Elemento Consultar(string codigo) =>
        Transaccional.Ejecutar(_accesoDB, transaccion =>
            _elementoRepositorio.BuscarPorCodigo(transaccion, codigo)
                ?? throw new ExcepcionMonster(CodigoError.ELE_001));

    public virtual List<Elemento> Listar() =>
        Transaccional.Ejecutar(_accesoDB, transaccion => _elementoRepositorio.Listar(transaccion));

    public virtual Elemento Registrar(string codigo, string nombre, decimal valor) =>
        Transaccional.Ejecutar(_accesoDB, transaccion =>
        {
            if (_elementoRepositorio.BuscarPorCodigo(transaccion, codigo) is not null)
            {
                throw new ExcepcionMonster(CodigoError.ELE_002);
            }
            var elemento = new Elemento(codigo, nombre, RedondearValor(valor));
            _elementoRepositorio.Insertar(transaccion, elemento);
            return elemento;
        });

    // ------------------------------------------------------------------ cálculos (sin acceso a datos)

    public static decimal RedondearValor(decimal valor) =>
        Math.Round(valor, Elemento.MaxDecimalesValor, MidpointRounding.AwayFromZero);
}
