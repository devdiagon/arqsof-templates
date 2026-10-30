using ec.edu.monster.api;
using ec.edu.monster.controlador;
using ec.edu.monster.db;
using ec.edu.monster.servicios;

var builder = WebApplication.CreateBuilder(args);

// Ensamblado del núcleo. Todo es sin estado: la conexión se abre y se cierra en cada operación.
// EJEMPLO: sustituir por las piezas del dominio.
builder.Services.AddSingleton<AccesoDB>();
builder.Services.AddSingleton<IElementoRepositorio, ElementoRepositorioSqlServer>();
builder.Services.AddSingleton<ElementoServicio>();
builder.Services.AddSingleton<ElementoControlador>();

// Capa api: un recurso por área, sin estado. Se resuelve por inyección en los parámetros de cada ruta (Rutas.cs).
builder.Services.AddSingleton<ElementoRecurso>();

var app = builder.Build();

// Primero en la canalización: toda excepción o respuesta vacía del framework (404, 405...) sale con la
// envoltura estado/codigo/mensaje (R6). No usar UseDeveloperExceptionPage ni UseExceptionHandler antes.
app.UseMiddleware<ManejadorErrores>();
Rutas.Mapear(app);

app.Run();
