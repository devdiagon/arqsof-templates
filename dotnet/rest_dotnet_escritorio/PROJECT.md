# <Nombre del proyecto> — cliente de escritorio REST (.NET, WPF)

> Esqueleto de `PROJECT.md`: describe **este** cliente en **este** dominio (estado real, decisiones, pendientes). La guía genérica de la plantilla es `README.md`.
> Si eres un agente que continúa este trabajo, lee primero este archivo.

## 1. Contexto
- **Dominio:** <qué hace el sistema>
- **Servidor:** <nombre de la API REST>, URL base `<UrlBasePorDefecto>` (con «/» final), tecnología del servidor: Java / .NET; cumple `CONVENCION_REST.md`
- **Documentos de referencia:** <ERS, ECUD, contrato HTTP del servidor, documentos de contexto>
- **Endpoints usados por el cliente:** <tabla método / ruta / cuerpo / respuesta / ¿requiere token?>
- **Flujo y textos de las pantallas** (los comparte con los otros clientes del proyecto): <títulos, etiquetas, formato del resultado>
- **Arquitectura:** MVC por capas con MVVM; `vista → controlador (ViewModel) → servicio → api/dto`. Los DTO no salen de `servicio/`.

## 2. Estado actual
Plantilla base sin dominio: acceso (`LoginVista`, con un marcador `AutenticadorPendiente` en `App.xaml.cs`), pantalla de ejemplo (`InicioVista`), navegación, estilos (tema claro, imagen a la derecha), transporte y pruebas. Compila y pasa las pruebas de la plantilla; no tiene DTO ni endpoints del dominio.

- [ ] Proyecto y `.slnx` renombrados
- [ ] Servidor configurado (`ConfiguracionApi.UrlBasePorDefecto`, con «/» final) y nombre de la aplicación (`Textos.NombreAplicacion`)
- [ ] Contrato probado con curl o el archivo `.http` del servidor (login, una consulta protegida, un error)
- [ ] DTO del dominio en `dto/` (uno por cuerpo del contrato, una fila por endpoint en §1)
- [ ] Servicio del dominio (`ServicioXxx : ServicioBase, IAutenticador`) conectado en `App.xaml.cs`; quitado `AutenticadorPendiente`
- [ ] Pantallas del dominio construidas (controlador + vista + `DataTemplate`) y retiradas `InicioVista`/`InicioControlador`
- [ ] Pruebas del dominio (servicio con `ClienteApiFalso`, controladores, `CanExecute` de los comandos que cargan datos, una carga de vista por pantalla)
- [ ] Imagen decorativa sustituida (si aplica)
- [ ] Login verificado contra el servidor real
- [ ] Errores `400` / `404` del servidor probados: mensaje mostrado y sesión conservada
- [ ] Servidor apagado / timeout probado
- [ ] Token vencido (`AUTH-003`) devuelve al login con el mensaje
- [ ] Ventana al tamaño inicial, maximizada y al mínimo (1040 × 640) revisadas a mano
- [ ] Tildes y «ñ» en pantalla
- [ ] (Si aplica) HTTPS probado con un certificado válido y de confianza
- [ ] Publicación (`dotnet publish`) probada

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
