using CoreWCF;
using CoreWCF.Configuration;
using CoreWCF.Description;
using ec.edu.monster.controlador;
using ec.edu.monster.db;
using ec.edu.monster.servicios;
using ec.edu.monster.ws;
using ec.edu.monster.ws.contrato;

var builder = WebApplication.CreateBuilder(args);

// CoreWCF: metadatos (WSDL en <url>?wsdl) anunciando el host con el que se consulta.
builder.Services.AddServiceModelServices().AddServiceModelMetadata();
builder.Services.AddSingleton<IServiceBehavior, UseRequestHeadersForMetadataAddressBehavior>();

// Ensamblado del núcleo. Todo es sin estado: la conexión se abre y se cierra en cada operación.
// EJEMPLO: sustituir por las piezas del dominio.
builder.Services.AddSingleton<AccesoDB>();
builder.Services.AddSingleton<IElementoRepositorio, ElementoRepositorioSqlServer>();
builder.Services.AddSingleton<ElementoServicio>();
builder.Services.AddSingleton<ElementoControlador>();

// CoreWCF solo construye con parámetros en el constructor los endpoints registrados aquí; sin esto el WSDL
// se publica bien pero toda llamada falla con HTTP 500 InternalServiceFault (CONVENCION_SOAP.md 3.2).
// Instancia por llamada (PerCall): una nueva en cada petición.
builder.Services.AddTransient<ElementoWS>();

var app = builder.Build();

app.UseServiceModel(serviceBuilder =>
{
    // EJEMPLO: un servicio por área; la ruta es libre (R9), por convención "/<NombreServicio>".
    serviceBuilder.AddService<ElementoWS>();
    serviceBuilder.AddServiceEndpoint<ElementoWS, IElementoWS>(
        Enlace(ConvencionSoap.ServicioElemento), "/" + ConvencionSoap.ServicioElemento);

    app.Services.GetRequiredService<ServiceMetadataBehavior>().HttpGetEnabled = true;
});

app.MapGet("/", () => "Servidor SOAP en marcha. WSDL en /" + ConvencionSoap.ServicioElemento + "?wsdl");

app.Run();

// Sin Name y Namespace el binding sale en tempuri.org (CONVENCION_SOAP.md 3.2, punto 4).
static BasicHttpBinding Enlace(string servicio) =>
    new() { Name = servicio + "PortBinding", Namespace = ConvencionSoap.Tns };
