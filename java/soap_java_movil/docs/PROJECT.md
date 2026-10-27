# <Nombre del proyecto> — cliente móvil (Android, Java)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md` (en la raíz del proyecto); la arquitectura que rige es `ARQUITECTURA_CLIENTE.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre(s) del/los servicio(s) SOAP>, URL completa de cada uno en `res/values/config.xml` (`url_servicio`…), WSDL `<URL>?wsdl` (tecnología del servidor: Java / .NET; cumple `CONVENCION_SOAP.md`; TNS `http://ws.monster.edu.ec/`)
- **Documentos de referencia:** <ERS, ECUD, documentos de contexto del servidor>
- **Operaciones del servidor usadas por el cliente:** <tabla servicio / operación / parámetros / respuesta>
- **Flujo y textos de las pantallas** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado>
- **Fuente canónica del núcleo:** <consola SOAP del proyecto y versión copiada de `dto`, `excepciones`, `modelo`, `servicios` y de las interfaces `…Cliente` de `ws`>

## 2. Estado actual
Plantilla base sin dominio, alineada con `ARQUITECTURA_CLIENTE.md`: transporte SOAP a mano (`ClienteSoapHttp`, `SoapXml`), traducción de fallos de red (`TraductorRed`), verificación de la envoltura (`ServicioBase`), tipos comunes (`RespuestaBaseDto`, `ExcepcionServicio` con `RED` y `SRV-000`), molde de controlador (`ControladorBase`), zona de estado (`ZonaEstado`), `Textos`, estilos, andamiaje de pruebas manuales (`Verificador`, `PruebasWs`), una pantalla inicial de ejemplo (`InicioActivity`) y una pantalla de acceso pasiva (`LoginActivity`, sin controlador). Compila y pasa las pruebas de la plantilla; no tiene login conectado ni pantallas de dominio.

- [ ] `docs/PANTALLAS.md` completado antes de construir las pantallas (esqueleto de la plantilla)
- [ ] Servidor configurado (`res/values/config.xml` → `url_servicio`, `network_security_config.xml`)
- [ ] Núcleo copiado de la consola (`dto`, `excepciones`, `modelo`, `servicios` e interfaces `…Cliente`), sin diferencias (`diff -r`), y `Textos` con los valores compartidos de la especificación
- [ ] `ws` reescrito: `XClienteWS` por servicio sobre `ClienteSoapHttp`/`SoapXml`, con el XML real del servidor en `SoapXmlTest` (una fila por operación en §1)
- [ ] Retirado el ejemplo de conformidad (`ConvencionClienteTest`): las pruebas de conformidad están fuera de alcance (`docs/CONVENCION_PRUEBAS.md` §8)
- [ ] Verificación manual del contrato hecha y registrada (`docs/CONVENCION_PRUEBAS.md` §6.3)
- [ ] Si el proyecto tiene login: controlador de `LoginActivity` y sesión definida en `modelo` y login verificado contra el servidor real
- [ ] Servicios, controladores y pantallas del dominio construidos y conectados en `AplicacionCliente` y `FabricaControladores`
- [ ] Pruebas manuales (`ec.edu.monster.pruebas`): `PruebasServicio` del dominio agregada a `PruebasTodas`, hasta 15 réplicas en total, ejecutadas desde el IDE
- [ ] Probado en el emulador y en un teléfono real
- [ ] Servidor apagado / timeout probado
- [ ] Si el proyecto tiene login: credenciales inválidas muestran el mensaje del servidor y no inician sesión
- [ ] Rotación de pantalla, teclado y tildes/«ñ» probados

## 3. Decisiones tomadas
| Tema | Decisión | Motivo |
|---|---|---|
| `ws` | Sobre SOAP a mano (`ClienteSoapHttp` + `SoapXml`); sin `ws.generado` | Android no incluye JAX-WS (`ARQUITECTURA_CLIENTE.md` §4.4). **Excepción documentada** a la copia del núcleo |
| `Textos` | Clase `Textos` y no `strings.xml` (salvo `app_name`) | Mantener la regla de copia y la comparación de valores entre clientes |
| | | |

## 4. Decisiones abiertas y riesgos
- 

## 5. Cómo ejecutar y verificar
Ver `README.md` §6 y §7. Datos propios de este proyecto (usuario de prueba, puertos, particularidades): <completar>.

## 6. Problemas frecuentes de este proyecto
| Síntoma | Causa / solución |
|---|---|
| | |
