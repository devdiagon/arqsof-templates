# <Nombre del proyecto> — cliente de consola REST (.NET)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre de la API REST>, URL base `<UrlBasePorDefecto>` (con «/» final), tecnología del servidor: Java / .NET; cumple `CONVENCION_REST.md`
- **Documentos de referencia:** <ERS, ECUD, contrato HTTP del servidor, documentos de contexto>
- **Endpoints usados por el cliente:** <tabla método / ruta / cuerpo / respuesta / ¿requiere token?>
- **Flujo, menús y textos de la consola** (los comparte con los otros clientes del proyecto): <opciones de cada menú, mensajes, formato del resultado>

## 2. Estado actual
Plantilla base sin dominio: transporte (`ClienteApi`, `ConfiguracionApi`), tipos comunes (`Sesion`, `ExcepcionServicio`, `RespuestaBaseDto`), base de servicios (`ServicioBase`) y entrada/salida (`VistaBase`). Compila y pasa las pruebas de la plantilla; no tiene DTO del dominio, login ni menús.

- [ ] Proyecto y `.slnx` renombrados
- [ ] Servidor configurado (`ConfiguracionApi.UrlBasePorDefecto`, con «/» final)
- [ ] Contrato probado con curl o el archivo `.http` del servidor (login, una consulta protegida, un error)
- [ ] DTO del dominio en `dto/` (uno por cuerpo del contrato, una fila por endpoint en §1)
- [ ] Servicios del dominio sobre `ServicioBase`
- [ ] Pruebas del dominio (servicio con `ClienteApiFalso`, controladores con vista falsa) y retiradas las que ya no apliquen
- [ ] Login verificado contra el servidor real
- [ ] Vistas y controladores del dominio construidos y conectados en `Program.cs`
- [ ] Errores `400` / `404` del servidor probados: mensaje mostrado y sesión conservada
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

## 6. Problemas frecuentes de este proyecto
| Síntoma | Causa / solución |
|---|---|
| | |
