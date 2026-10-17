# <Nombre del proyecto> — cliente web (Jakarta EE, JSP)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre del servicio SOAP>, URL completa `<URL_POR_DEFECTO>`, servicio `<NOMBRE_SERVICIO>`, namespace `<NAMESPACE>`, WSDL `<URL>?wsdl` (tecnología del servidor: Java / .NET; cumple `CONVENCION_SOAP.md`)
- **Despliegue del cliente:** Payara 7, context root `<context-root>`, URL `http://localhost:8080/<context-root>/login`
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla operación / parámetros / respuesta / ¿requiere token?>
- **Flujo y textos de las pantallas** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado>
- **Pantallas y rutas:** <tabla ruta / controlador / JSP / ¿protegida?>

## 2. Estado actual
Plantilla base sin dominio: login funcional (`/login`) y pantalla de ejemplo (`/inicio`). Compila y pasa las pruebas de la plantilla.

- [ ] Servidor SOAP configurado (`ConfiguracionServicio`: `URL_POR_DEFECTO`, `NOMBRE_SERVICIO`, `NAMESPACE`)
- [ ] Contrato (con `action` y `targetNamespace`) y pruebas de conformidad (`ConvencionClienteTest`) adaptados a las operaciones reales
- [ ] Nombre de la aplicación, context root y `artifactId` actualizados
- [ ] Login verificado contra el servidor real
- [ ] Operaciones del dominio incorporadas (una fila por operación en §1)
- [ ] Pantallas del dominio construidas y sus rutas agregadas a `FiltroAcceso`
- [ ] Servidor SOAP apagado / timeout probado
- [ ] Token vencido (`AUTH-003`) devuelve al login con el mensaje
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
