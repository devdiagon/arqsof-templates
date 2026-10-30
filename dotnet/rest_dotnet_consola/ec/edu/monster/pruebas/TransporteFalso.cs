using ec.edu.monster.api;

namespace ec.edu.monster.pruebas;

/// <summary>
/// Transporte simulado: guarda la última petición y devuelve una respuesta fija o lanza la excepción indicada.
/// Las pruebas de <c>api</c> no abren red ni levantan servidor.
/// </summary>
public sealed class TransporteFalso : ITransporte
{
    private readonly RespuestaHttp? _respuesta;
    private readonly Exception? _fallo;

    public TransporteFalso(int codigo, string? cuerpo) => _respuesta = new RespuestaHttp(codigo, cuerpo);

    public TransporteFalso(Exception fallo) => _fallo = fallo;

    public string? Metodo { get; private set; }
    public string? Ruta { get; private set; }
    public string? CuerpoJson { get; private set; }
    public string? Autorizacion { get; private set; }
    public int Llamadas { get; private set; }

    public Task<RespuestaHttp> EnviarAsync(string metodo, string ruta, string? cuerpoJson, string? autorizacion)
    {
        Llamadas++;
        Metodo = metodo;
        Ruta = ruta;
        CuerpoJson = cuerpoJson;
        Autorizacion = autorizacion;
        if (_fallo is not null)
        {
            throw _fallo;
        }
        return Task.FromResult(_respuesta!);
    }
}
