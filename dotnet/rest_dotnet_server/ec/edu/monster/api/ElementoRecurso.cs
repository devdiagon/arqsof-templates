using ec.edu.monster.controlador;

namespace ec.edu.monster.api;

/// <summary>
/// EJEMPLO de recurso REST (<c>api/elementos</c>). Solo recibe, delega y devuelve.
/// <list type="bullet">
/// <item>Una sola clase por recurso, en plural, con todas sus operaciones (R10); las rutas se declaran en <see cref="Rutas"/>.</item>
/// <item>Los parámetros de ruta y del cuerpo pasan como texto al controlador, que valida y convierte.</item>
/// <item>Los métodos con cuerpo lo leen con <see cref="CuerpoPeticion"/> y <see cref="CuerpoJson"/>; sus excepciones
/// (ERR-415, DATA-001) las convierte <see cref="ManejadorErrores"/>.</item>
/// <item>Todo éxito responde 200, también al crear (CONVENCION_REST.md 2).</item>
/// </list>
/// </summary>
public class ElementoRecurso
{
    private readonly ElementoControlador _controlador;

    public ElementoRecurso(ElementoControlador controlador)
    {
        _controlador = controlador;
    }

    /// <summary><c>GET elementos</c></summary>
    public IResult ListarElementos() =>
        RespuestasHttp.De(ElementoMapeador.ARespuestaElementos(_controlador.Listar()));

    /// <summary><c>GET elementos/{codigoElemento}</c></summary>
    public IResult ConsultarElemento(string codigoElemento) =>
        RespuestasHttp.De(ElementoMapeador.ARespuestaElemento(_controlador.Consultar(codigoElemento)));

    /// <summary><c>POST elementos</c> con cuerpo <c>{"codigoElemento": …, "nombre": …, "valor": …}</c>.</summary>
    public async Task<IResult> RegistrarElemento(HttpRequest peticion)
    {
        CuerpoJson json = CuerpoJson.Leer(await CuerpoPeticion.LeerTexto(peticion));
        return RespuestasHttp.De(ElementoMapeador.ARespuestaElemento(_controlador.Registrar(
            json.Texto("codigoElemento"), json.Texto("nombre"), json.Texto("valor"))));
    }
}
