# <Nombre del proyecto> — cliente web REST (.NET, ASP.NET Core MVC)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre de la API REST>, URL base `<UrlBasePorDefecto>` (con «/» final), tecnología del servidor: Java / .NET; cumple `CONVENCION_REST.md`
- **Despliegue del cliente:** perfil `http` (Kestrel), `http://localhost:5092/`
- **Documentos de referencia:** <ERS, ECUD, contrato HTTP del servidor, documentos de contexto>
- **Endpoints usados por el cliente:** <tabla método / ruta / cuerpo / respuesta / ¿requiere token?>
- **Flujo y textos de las pantallas** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado>
- **Pantallas y rutas:** <tabla ruta / controlador / vista / ¿protegida?>
- **Arquitectura:** MVC por capas: `vista (Razor) ← controlador → servicio → api/dto`. Los DTO no salen de `servicio/`. El navegador nunca habla con la API: lo hace el servidor web.

## 2. Estado actual
Plantilla base sin dominio: acceso funcional (`Acceso/Acceder`, con un marcador `AutenticadorPendiente` en `Program.cs`), pantalla de ejemplo (`Inicio/Index`), sesión, estilos (tema claro, imagen a la derecha), componentes `ui-*`, transporte y pruebas. Compila y pasa las pruebas de la plantilla; no tiene DTO ni endpoints del dominio.

- [ ] Proyecto y `.slnx` renombrados; `@addTagHelper` de `vista/_ViewImports.cshtml` con el nuevo nombre del ensamblado
- [ ] Servidor configurado (`ConfiguracionApi.UrlBasePorDefecto`, con «/» final) y nombre de la aplicación (`Textos.NombreAplicacion`)
- [ ] Contrato probado con curl o el archivo `.http` del servidor (login, una consulta protegida, un error)
- [ ] DTO del dominio en `dto/` (uno por cuerpo del contrato, una fila por endpoint en §1)
- [ ] Servicio del dominio (`ServicioXxx : ServicioBase, IAutenticador`) registrado en `Program.cs`; quitado `AutenticadorPendiente`
- [ ] Pantallas del dominio construidas (controlador con `[ExigirSesion]` + vista + modelo) y retirada la pantalla `Inicio` de ejemplo (conservando la acción `Error`)
- [ ] Pruebas del dominio (servicio con `ClienteApiFalso`, una prueba de recorrido por pantalla en `PantallasWebPruebas`, componentes nuevos en `ComponentesPruebas`)
- [ ] Imagen decorativa sustituida (si aplica)
- [ ] Acceso verificado en el navegador contra el servidor real
- [ ] Errores `400` / `404` del servidor probados: mensaje mostrado, datos capturados y sesión conservados
- [ ] Servidor apagado / timeout probado (≈ 10 s, «Servidor no disponible, intente más tarde»)
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

## 6. Problemas frecuentes de este proyecto
| Síntoma | Causa / solución |
|---|---|
| | |
