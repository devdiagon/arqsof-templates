# <Nombre del proyecto> — cliente móvil REST (.NET, MAUI)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre de la API REST>, URL base `http://<host>:<puerto>/<ruta>/` (con «/» final; `ConfiguracionApi`: `Puerto`, `Ruta`), tecnología del servidor: Java / .NET; cumple `CONVENCION_REST.md`
- **Documentos de referencia:** <ERS, ECUD, contrato HTTP del servidor, documentos de contexto>
- **Endpoints usados por el cliente:** <tabla método / ruta / cuerpo / respuesta / ¿requiere token?>
- **Flujo y textos de las pantallas** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado; flujo de navegación>
- **Presentación:** <oscura (por defecto) o clara; ver `README.md` §5>
- **Arquitectura:** MVC por capas con MVVM; `vista → controlador (ViewModel) → servicio → api/dto`; navegación con `Shell` (`vista/Navegacion`). Los DTO no salen de `servicio/`.

## 2. Estado actual
Plantilla base sin dominio: acceso (`LoginVista`, con un marcador `AutenticadorPendiente` en `MauiProgram.cs`), pantalla de ejemplo (`InicioVista`), navegación, estilos, transporte y pruebas. Compila y pasa las pruebas de la plantilla; no tiene DTO ni endpoints del dominio.

- [ ] Proyecto y `.slnx` renombrados
- [ ] Nombre de la aplicación (`ApplicationTitle` y `Textos.NombreAplicacion`) e identificador (`ApplicationId`)
- [ ] Servidor configurado (`ConfiguracionApi`: `Puerto`, `Ruta`; `IpPc` para teléfono físico)
- [ ] Contrato probado con curl o el archivo `.http` del servidor (login, una consulta protegida, un error)
- [ ] DTO del dominio en `dto/` (uno por cuerpo del contrato, una fila por endpoint en §1)
- [ ] Servicio del dominio (`ServicioXxx : ServicioBase, IAutenticador`) registrado en `MauiProgram.cs`; quitado `AutenticadorPendiente`
- [ ] Pantallas del dominio construidas (controlador + vista + ruta en `AppShell.xaml.cs` + `Navegacion`) y retiradas `InicioVista`/`InicioControlador`
- [ ] Pruebas del dominio (servicio con `ClienteApiFalso`, controladores, `CanExecute` de los comandos que cargan datos)
- [ ] Presentación definitiva (oscura o clara, `README.md` §5) e imagen decorativa sustituida (si aplica)
- [ ] Login verificado contra el servidor real (emulador)
- [ ] Errores `400` / `404` del servidor probados: mensaje mostrado y sesión conservada
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

## 6. Problemas frecuentes de este proyecto
| Síntoma | Causa / solución |
|---|---|
| | |
