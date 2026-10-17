# <Nombre del proyecto> — cliente web (.NET, ASP.NET Core MVC)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre del servicio SOAP>, URL completa `<UrlPorDefecto>`, WSDL `<URL>?wsdl` (tecnología del servidor: Java / .NET; cumple `CONVENCION_SOAP.md`)
- **Despliegue del cliente:** perfil `http` (Kestrel), `http://localhost:5193/`
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla operación / parámetros / respuesta / ¿requiere token?>
- **Flujo y textos de las pantallas** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado>
- **Pantallas y rutas:** <tabla ruta / controlador / vista / ¿protegida?>
- **Arquitectura:** MVC por capas: `vista (Razor) ← controlador → servicio → ws`. Los tipos generados (`ws/Reference.cs`) no salen de `servicio/`. El navegador nunca habla SOAP: lo hace el servidor web.

## 2. Estado actual
Plantilla base sin dominio: acceso funcional (`Acceso/Acceder`, con un marcador `AutenticadorPendiente` en `Program.cs`), pantalla de ejemplo (`Inicio/Index`), sesión, estilos, componentes `ui-*`, transporte y pruebas. Compila y pasa las pruebas de la plantilla; no tiene proxy ni operaciones.

- [ ] Proyecto y `.slnx` renombrados; `@addTagHelper` de `vista/_ViewImports.cshtml` con el nuevo nombre del ensamblado
- [ ] Servidor configurado (`ConfiguracionServicio.UrlPorDefecto`) y nombre de la aplicación (`Textos.NombreAplicacion`)
- [ ] WSDL guardado en `wsdl/` y proxy generado en `ws/Reference.cs` (paquetes del `.csproj` limpiados a `Http` y `Primitives`)
- [ ] `IClienteXxx` / `ClienteWSXxx` y servicio del dominio (`ServicioXxx : ServicioBase, IAutenticador`) conectado en `Program.cs`; quitado `AutenticadorPendiente`
- [ ] Operaciones del dominio incorporadas (una fila por operación en §1)
- [ ] Pantallas del dominio construidas (controlador con `[ExigirSesion]` + vista + modelo) y retiradas la pantalla `Inicio` de ejemplo (conservando la acción `Error`)
- [ ] Pruebas del dominio (servicio con doble del cliente, una prueba de recorrido por pantalla en `PantallasWebPruebas`, componentes nuevos en `ComponentesPruebas`)
- [ ] Imagen decorativa sustituida (si aplica)
- [ ] Acceso verificado en el navegador contra el servidor real
- [ ] Servidor SOAP apagado / timeout probado (≈ 10 s, «No se pudo comunicar con el servidor»)
- [ ] Token vencido (`AUTH-003`) devuelve al acceso con el mensaje
- [ ] Ventana ancha y estrecha (< 900 px) revisadas en un navegador; tildes y «ñ» en pantalla
- [ ] (Si aplica) HTTPS probado con un certificado válido y de confianza
- [ ] Publicación (`dotnet publish -c Release`) probada

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
