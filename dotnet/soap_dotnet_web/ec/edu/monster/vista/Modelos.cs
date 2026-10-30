namespace ec.edu.monster.vista;

// Lo que cada controlador entrega a su vista (el equivalente de los atributos de la petición del cliente Java). Solo texto
// ya listo para mostrar: la vista no conoce dto ni ws. Los de las operaciones que cambian datos viajan de un POST al GET al que
// se redirige (Post/Redirect/Get), serializados en TempData.
// DOMINIO: agregar aquí un registro por pantalla con datos.

/// <summary>Página de último recurso: solo el mensaje.</summary>
public sealed record ErrorModelo(string Mensaje);
