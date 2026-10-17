# <Nombre del proyecto> — cliente de escritorio (.NET, WPF)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre del servicio SOAP>, URL completa `<UrlPorDefecto>`, WSDL `<URL>?wsdl` (tecnología del servidor: Java / .NET; cumple `CONVENCION_SOAP.md`)
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla operación / parámetros / respuesta / ¿requiere token?>
- **Flujo y textos de las pantallas** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado>
- **Arquitectura:** MVC por capas con MVVM; `vista → controlador (ViewModel) → servicio → ws`. Los tipos generados no salen de `servicio/`.

## 2. Estado actual
Plantilla base sin dominio: acceso (`LoginVista`, con un marcador `AutenticadorPendiente` en `App.xaml.cs`), pantalla de ejemplo (`InicioVista`), navegación, estilos, transporte y pruebas. Compila y pasa las pruebas de la plantilla; no tiene proxy ni operaciones.

- [ ] Proyecto y `.slnx` renombrados
- [ ] Servidor configurado (`ConfiguracionServicio.UrlPorDefecto`) y nombre de la aplicación (`Textos.NombreAplicacion`)
- [ ] WSDL guardado en `wsdl/` y proxy generado en `ws/Reference.cs` (paquetes del `.csproj` limpiados a `Http` y `Primitives`)
- [ ] `IClienteXxx` / `ClienteWSXxx` y servicio del dominio (`ServicioXxx : ServicioBase, IAutenticador`) conectado en `App.xaml.cs`; quitado `AutenticadorPendiente`
- [ ] Operaciones del dominio incorporadas (una fila por operación en §1)
- [ ] Pantallas del dominio construidas (controlador + vista + `DataTemplate`) y retiradas `InicioVista`/`InicioControlador`
- [ ] Pruebas del dominio (servicio con doble del cliente, controladores, `CanExecute` de los comandos que cargan datos, una carga de vista por pantalla)
- [ ] Imagen decorativa sustituida (si aplica)
- [ ] Login verificado contra el servidor real
- [ ] Servidor apagado / timeout probado
- [ ] Token vencido (`AUTH-003`) devuelve al login con el mensaje
- [ ] Ventana al tamaño inicial, maximizada y al mínimo (1040 × 640) revisadas a mano
- [ ] Tildes y «ñ» en pantalla
- [ ] (Si aplica) HTTPS probado con un certificado válido y de confianza
- [ ] Publicación (`dotnet publish`) probada

## 3. Decisiones tomadas
| Tema | Decisión | Motivo |
|---|---|---|
| | | |

## 4. Decisiones abiertas y riesgos
- 

## 5. Cómo ejecutar y verificar
Ver `README.md` §6 y §7. Datos propios de este proyecto (usuario de prueba, puertos, URL de desarrollo, particularidades): <completar>.

**Regenerar el proxy** (`ws/Reference.cs`): <comando exacto de `dotnet-svcutil` usado y de dónde sale el WSDL; ruta corta de trabajo>.

## 6. Problemas frecuentes de este proyecto
| Síntoma | Causa / solución |
|---|---|
| | |
