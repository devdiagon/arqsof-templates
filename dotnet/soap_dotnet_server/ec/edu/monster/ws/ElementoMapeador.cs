using ec.edu.monster.dto;
using ec.edu.monster.modelo;
using ec.edu.monster.ws.contrato;

namespace ec.edu.monster.ws;

/// <summary>
/// EJEMPLO de mapeador: convierte Resultado en tipos de contrato, pasando los tipos reales a texto.
/// </summary>
public static class ElementoMapeador
{
    public static RespuestaElemento ARespuestaElemento(Resultado<Elemento> resultado)
    {
        var respuesta = new RespuestaElemento();
        respuesta.AplicarEnvoltura(resultado.Estado.ToString(), resultado.Codigo, resultado.Mensaje);
        Elemento? datos = resultado.Datos;
        if (datos is not null)
        {
            respuesta.CargarDatos(datos.Codigo, datos.Nombre, ConversorTexto.Decimal(datos.Valor)!);
        }
        return respuesta;
    }

    public static RespuestaElementos ARespuestaElementos(Resultado<List<Elemento>> resultado)
    {
        var respuesta = new RespuestaElementos();
        respuesta.AplicarEnvoltura(resultado.Estado.ToString(), resultado.Codigo, resultado.Mensaje);
        if (resultado.Datos is not null)
        {
            foreach (Elemento e in resultado.Datos)
            {
                respuesta.Elementos.Add(new ElementoDetalle(e.Codigo, e.Nombre, ConversorTexto.Decimal(e.Valor)!));
            }
        }
        return respuesta;
    }
}
