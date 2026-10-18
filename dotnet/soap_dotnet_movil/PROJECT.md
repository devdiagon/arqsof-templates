# <Nombre del proyecto> — cliente móvil (.NET, MAUI)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre del servicio SOAP>, URL completa `<http://host:puerto/ruta>` (`ConfiguracionServicio`: `Puerto`, `Ruta`), WSDL `<URL>?wsdl` (tecnología del servidor: Java / .NET; cumple `CONVENCION_SOAP.md`)
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla operación / parámetros / respuesta / ¿requiere token?>
- **Flujo y textos de las pantallas** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado; flujo de navegación>
- **Arquitectura:** MVC por capas con MVVM; `vista → controlador (ViewModel) → servicio → ws`; navegación con `Shell` (`vista/Navegacion`). Los tipos generados no salen de `servicio/`.

## 2. Estado actual
Plantilla base sin dominio: acceso (`LoginVista`, con un marcador `AutenticadorPendiente` en `MauiProgram.cs`), pantalla de ejemplo (`InicioVista`), navegación, estilos, transporte y pruebas. Compila y pasa las pruebas de la plantilla; no tiene proxy ni operaciones.

- [ ] Proyecto y `.slnx` renombrados
- [ ] Nombre de la aplicación (`ApplicationTitle` y `Textos.NombreAplicacion`) e identificador (`ApplicationId`)
- [ ] Servidor configurado (`ConfiguracionServicio`: `Puerto`, `Ruta`; `IpPc` para teléfono físico)
- [ ] WSDL guardado en `wsdl/` y proxy generado en `ws/Reference.cs` (paquetes del `.csproj` limpiados a `Http` y `Primitives`)
- [ ] **Prueba de humo del proxy en Android: Debug y Release**
- [ ] `IClienteXxx` / `ClienteWSXxx` y servicio del dominio (`ServicioXxx : ServicioBase, IAutenticador`) registrado en `MauiProgram.cs`; quitado `AutenticadorPendiente`
- [ ] Operaciones del dominio incorporadas (una fila por operación en §1)
- [ ] Pantallas del dominio construidas (controlador + vista + ruta en `AppShell.xaml.cs` + `Navegacion`) y retiradas `InicioVista`/`InicioControlador`
- [ ] Pruebas del dominio (servicio con doble del cliente, controladores, `CanExecute` de los comandos que cargan datos)
- [ ] Imagen decorativa sustituida (si aplica)
- [ ] Login verificado contra el servidor real (emulador)
- [ ] Servidor apagado / timeout probado
- [ ] Token vencido (`AUTH-003`) devuelve al login con el mensaje
- [ ] Atrás de Android y «Cerrar sesión» probados
- [ ] Flujo completo repetido en **Release**
- [ ] Tildes y «ñ» en pantalla; teclado numérico (signo menos y coma)
- [ ] Probado en teléfono real (`IpPc`, firewall, red Privada)
- [ ] (Si aplica) HTTPS con un certificado válido y de confianza en el dispositivo
- [ ] (Si aplica) Probado con IIS Express (binding comodín, Visual Studio como administrador, firewall)

## 3. Decisiones tomadas
| Tema | Decisión | Motivo |
|---|---|---|
| | | |

## 4. Decisiones abiertas y riesgos
- 

## 5. Cómo ejecutar y verificar
Ver `README.md` §6 y §7. Datos propios de este proyecto (usuario de prueba, puertos, URL de desarrollo, emulador, particularidades): <completar>.

**Regenerar el proxy** (`ws/Reference.cs`): <comando exacto de `dotnet-svcutil` usado y de dónde sale el WSDL; ruta corta de trabajo>.

## 6. Problemas frecuentes de este proyecto
| Síntoma | Causa / solución |
|---|---|
| | |
