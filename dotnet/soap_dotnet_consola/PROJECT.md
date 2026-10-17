# <Nombre del proyecto> — cliente de consola (.NET)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre del servicio SOAP>, URL completa `<UrlPorDefecto>`, WSDL `<URL>?wsdl` (tecnología del servidor: Java / .NET; cumple `CONVENCION_SOAP.md`)
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla operación / parámetros / respuesta / ¿requiere token?>
- **Flujo, menús y textos de la consola** (los comparte con los otros clientes del proyecto): <opciones de cada menú, mensajes, formato del resultado>

## 2. Estado actual
Plantilla base sin dominio: transporte (`ClienteWS`, `ConfiguracionServicio`), tipos comunes (`Sesion`, `ExcepcionServicio`), base de servicios (`ServicioBase`) y entrada/salida (`VistaBase`). Compila y pasa las pruebas de la plantilla; no tiene proxy, login ni menús.

- [ ] Proyecto y `.slnx` renombrados
- [ ] Servidor configurado (`ConfiguracionServicio.UrlPorDefecto`)
- [ ] WSDL guardado en `wsdl/` y proxy generado en `ws/Reference.cs` (paquetes del `.csproj` limpiados a `Http` y `Primitives`)
- [ ] `IClienteXxx` / `ClienteXxx` y servicios de las operaciones del dominio (una fila por operación en §1)
- [ ] Pruebas del dominio (servicio con doble del cliente, controladores con vista falsa) y retiradas las que ya no apliquen
- [ ] Login verificado contra el servidor real
- [ ] Vistas y controladores del dominio construidos y conectados en `Program.cs`
- [ ] Servidor apagado / timeout probado
- [ ] Token vencido (`AUTH-003`) devuelve al login con el mensaje
- [ ] Tildes, «ñ», entrada vacía, opción inválida y entrada cerrada probadas en una terminal real
- [ ] Ejecución desde terminal (`dotnet run`) probada
- [ ] (Si aplica) HTTPS probado con un certificado válido y de confianza

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
