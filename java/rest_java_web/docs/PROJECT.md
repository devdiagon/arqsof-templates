# <Nombre del proyecto> — cliente web (Jakarta EE, JSP, REST)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md` (en la raíz del proyecto); la arquitectura que rige es `ARQUITECTURA_CLIENTE.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre de la API REST>, URL base completa `<URL_BASE_POR_DEFECTO>` (contexto + prefijo de la API, con «/» final) (tecnología del servidor: Java / .NET; cumple `CONVENCION_REST.md`)
- **Despliegue del cliente:** Payara 7, context root `<context-root>`, URL `http://localhost:8080/<context-root>/`
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor, tabla de endpoints (`ENDPOINTS_REST.md`)>
- **Operaciones del servidor usadas por el cliente:** <tabla método de la interfaz / verbo y ruta / entrada / credenciales / respuesta>
- **Autenticación del servidor:** <cómo viajan las credenciales o el token; la define el proyecto, no la plantilla>
- **Flujo y textos de las pantallas** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado>
- **Pantallas y rutas:** <tabla ruta / controlador / JSP / ¿protegida?>
- **Fuente canónica del núcleo y versión copiada:** <proyecto y commit o etiqueta>

## 2. Estado actual
Plantilla base sin dominio, alineada con `ARQUITECTURA_CLIENTE.md`: configuración de la URL base (`ConfiguracionApi`), transporte HTTP (`Transporte`, `TransporteHttp`), armado de peticiones y lectura de respuestas sin red (`TraductorApi`), cliente perezoso compartido (`ClientePerezoso`), verificación de la envoltura (`ServicioBase`), tipos comunes (`RespuestaBaseDto`, `ExcepcionServicio` con `RED` y `SRV-000`), `ContextoWeb`, `FiltroCodificacion`, componentes JSP y estilos, una pantalla inicial de ejemplo (`/inicio`) y una pantalla de acceso pasiva (`login.jsp`, sin controlador). Compila y pasa las pruebas de la plantilla; no tiene login conectado ni pantallas de dominio.

- [ ] `ConfiguracionApi.URL_BASE_POR_DEFECTO` apuntando al servidor real; copia del contrato (`ENDPOINTS_REST.md` del proyecto) en `docs/`
- [ ] Nombre de la aplicación, context root y `artifactId` actualizados
- [ ] Interfaces propias `…Cliente` y `…ClienteApi`, DTO propios y servicios (`extends ServicioBase`) de las operaciones del dominio (una fila por operación en §1)
- [ ] Verificación manual del contrato hecha y registrada (`docs/CONVENCION_PRUEBAS.md` §6.3)
- [ ] Si el proyecto tiene login: sesión por empleado en la sesión HTTP (nunca compartida), controlador de acceso y login verificado contra el servidor real
- [ ] Servicios, controladores y vistas del dominio construidos y sus rutas protegidas, si hay login
- [ ] Servidor apagado / timeout probado
- [ ] Ventana ancha y estrecha (< 900 px) revisadas en un navegador
- [ ] WAR desplegado y probado en Payara 7

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
