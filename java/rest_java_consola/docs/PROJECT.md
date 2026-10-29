# <Nombre del proyecto> — cliente de consola (Java, REST)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md` (en la raíz del proyecto); la arquitectura que rige es `ARQUITECTURA_CLIENTE.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre de la API REST>, URL base completa `<URL_BASE_POR_DEFECTO>` (contexto + prefijo de la API, con «/» final) (tecnología del servidor: Java / .NET; cumple `CONVENCION_REST.md`)
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor, tabla de endpoints (`ENDPOINTS_REST.md`)>
- **Operaciones del servidor usadas por el cliente:** <tabla método de la interfaz / verbo y ruta / entrada / credenciales / respuesta>
- **Autenticación del servidor:** <cómo viajan las credenciales o el token; la define el proyecto, no la plantilla>
- **Flujo, menús y textos de la consola** (los comparte con los otros clientes del proyecto): <opciones de cada menú, mensajes, formato del resultado>

## 2. Estado actual
Plantilla base sin dominio, alineada con `ARQUITECTURA_CLIENTE.md`: configuración de la URL base (`ConfiguracionApi`), transporte HTTP (`Transporte`, `TransporteHttp`), armado de peticiones y lectura de respuestas sin red (`TraductorApi`), verificación de la envoltura (`ServicioBase`), tipos comunes (`RespuestaBaseDto`, `ExcepcionServicio` con `RED` y `SRV-000`) y entrada/salida (`VistaBase`, `Textos`). Compila y pasa las pruebas de la plantilla; no tiene login ni menús.

- [ ] `ConfiguracionApi.URL_BASE_POR_DEFECTO` apuntando al servidor real; copia del contrato (`ENDPOINTS_REST.md` del proyecto) en `docs/`
- [ ] `artifactId` actualizado
- [ ] Interfaces propias `…Cliente` y `…ClienteApi`, DTO propios y servicios (`extends ServicioBase`) de las operaciones del dominio (una fila por operación en §1)
- [ ] Verificación manual del contrato hecha y registrada (`docs/CONVENCION_PRUEBAS.md` §6.3)
- [ ] Si el proyecto tiene login: sesión definida en `modelo` y login verificado contra el servidor real
- [ ] Servicios, vistas y controladores del dominio construidos y conectados en `Main`
- [ ] Servidor apagado / timeout probado
- [ ] Si el proyecto tiene login: credenciales inválidas muestran el mensaje del servidor y no inician sesión
- [ ] Tildes, «ñ», entrada vacía y opción inválida probadas en una terminal real
- [ ] Ejecución desde terminal (`mvn compile exec:java`) y jar `-shaded` (`java -jar`) probados con el dominio

## 3. Decisiones tomadas
| Tema | Decisión | Motivo |
|---|---|---|
| | | |

## 4. Decisiones abiertas y riesgos
- 

## 5. Cómo ejecutar y verificar
Ver `README.md` §6 y §7. Datos propios de este proyecto (usuario de prueba, puertos, particularidades): <completar>.

## 6. Problemas frecuentes de este proyecto
| Síntoma | Causa / solución |
|---|---|
| | |
