# PANTALLAS — Estilos y distribución del cliente móvil de <Nombre del proyecto> (Android)

> Esqueleto de `PANTALLAS.md`: se completa **antes** de construir las pantallas del proyecto (orden de los elementos, estados y decisiones). Las reglas de §1 a §4 son las del cliente móvil de EurekaBank, que sirvió de modelo; se conservan salvo que el proyecto decida otra cosa y lo anote en §7. Las pantallas de §5 y las decisiones de §7 son del proyecto.

Especifica **cómo se ve y cómo se ordena** cada pantalla. Es una capa sobre fuentes que no se repiten aquí:

| Fuente | Aporta |
|---|---|
| `ESPECIFICACION_CLIENTES.md` (o la del proyecto) | Qué datos se piden y se muestran, textos, formato de los datos, errores, navegación |
| `COMPARTIR_CLIENTE.md` §4.2 y §4.3 | Decisiones del controlador y orden de los elementos de cada pantalla |
| `docs/CLIENT_STYLES_JAVA.md` | Paleta, tipografía, medidas y componentes (§5) |

> Estado: <fecha>. <Pantallas definidas / pendientes.>

---

## 1. Marco de la pantalla

- **Una sola columna.** Todos los elementos se apilan en vertical, a ancho completo con **24 dp** de margen a cada lado. No hay modo dividido ni anchos máximos.
- Presentación oscura, sin selector de tema. Título de la aplicación (etiqueta del manifiesto): `<app_name>`, igual que `Textos.NOMBRE_APLICACION`.
- **Imagen del proyecto** (`res/drawable-nodpi/fondo_app.*`): se muestra siempre **completa y centrada, sin recortar ni deformar** (`fitCenter`, nunca `centerCrop`); lo que sobra es el color `fondo`. Con una imagen horizontal, en un teléfono vertical queda una franja centrada y bandas de `fondo` arriba y abajo. No depende de su resolución: si se cambia por otra, el tratamiento es el mismo.

| Modo | Pantallas | Tratamiento de la imagen |
|---|---|---|
| **Acceso** | Login | Va **encima del formulario**, como cabecera de 160 dp a opacidad plena, en su propia zona (el contenido no queda sobre ella) |
| **Aplicación** | El resto | Es el **fondo de la pantalla**: completa, centrada y fija (fuera del `ScrollView`, no se desplaza), con **opacidad 40 %** sobre `fondo`. El contenido va encima, en paneles `panel-sobre-imagen` |

- Si el contenido no cabe en alto (pantallas bajas, teclado abierto), la pantalla se desplaza en vertical; **nunca en horizontal**.
- Con el teclado abierto la pantalla se redimensiona (`adjustResize`) para que el campo activo quede visible.

## 2. Componentes

Los layouts solo referencian estilos (`res/values/styles.xml`): `Boton.Primario`, `Boton.Secundario`, `Panel`, `Panel.Titulo`, `Etiqueta`, `Campo`, `Estado.Indicador` y `Mensaje`. Además:

| Componente | Archivos | Uso |
|---|---|---|
| Zona de estado | `layout/zona_estado.xml` (se incluye con `<include android:id="@+id/zonaEstado" …>`), `vista/ZonaEstado`, `drawable/mensaje_error_fondo`, `drawable/mensaje_exito_fondo` | Un solo lugar, **bajo los botones**, que muestra de una en una: indicador de ocupado con su texto, un error (barra izquierda `error`) o un éxito (barra `exito`). Oculta si no hay nada. `ZonaEstado.mostrar(ocupado, mensaje, error)` dibuja el estado de un `ControladorBase` |
| Ocultar el teclado | `vista/Teclado` | Se llama al enviar un formulario: con el teclado abierto taparía la zona de estado |
| Tarjeta de resultado, botón de menú, lista | `CLIENT_STYLES_JAVA.md` §5.9 y §5.12 | Solo si el proyecto los necesita; se construyen con `Panel`/`Panel.Resultado` y los tokens de color |

## 3. Comportamiento común

| Aspecto | Regla |
|---|---|
| Foco inicial | El primer campo editable |
| Teclado | La acción «Listo» del último campo equivale al botón principal; el tipo de teclado (numérico, texto) solo sugiere, no valida el formato |
| Atrás | En una pantalla secundaria equivale a `Cancelar` (vuelve sin llamar al servidor); estando ocupada se ignora |
| Ocupado | **Todos** los controles de la pantalla deshabilitados (incluido `Cancelar`) con el indicador visible; una segunda acción se ignora |
| Campo vacío | No se llama al servidor; el mensaje nombra el **primero** vacío, en el orden de la pantalla |
| Error | Se permanece en la pantalla y se conserva lo escrito (solo las claves se limpian) |
| Botones | Apilados en vertical, a ancho completo, separados 16 dp, con los 24 dp de margen; el principal arriba y `Cancelar` (secundario) debajo |
| Rotación | El estado (mensaje, resultado, ocupado) lo conserva el controlador; lo escrito lo restaura Android |
| Proceso cerrado por el sistema | La sesión, si existe, estaba solo en memoria: al volver, cualquier pantalla con sesión pasa al login |

---

## 4. Estados de una pantalla que llama al servidor

`ControladorBase` publica un estado inmutable (`ocupado`, `mensaje`, `error`, `datos`); la actividad lo dibuja completo cada vez:

| Estado | Qué se ve |
|---|---|
| Libre | Formulario habilitado; zona de estado oculta |
| Ocupado | Controles deshabilitados; indicador con el texto de `Textos` (`Procesando…`, `Consultando…`) |
| Éxito | Resultado visible; si hay mensaje de éxito, en la zona de estado |
| Error | Mensaje del servidor o del cliente (nunca un código) en la zona de estado; se conservan los datos |

## 5. Pantallas

<!-- Una subsección por pantalla: esquema ASCII, orden de los elementos, textos (de Textos), estados y decisiones. -->

### 5.1 Login (si el proyecto tiene autenticación)

```
┌──────────────────────────┐
│     imagen (completa,    │   cabecera de 160 dp, opacidad plena
│     centrada)            │
│    Inicio de sesión      │   título, centrado
│ Usuario                  │
│ [______________________] │
│ Clave                    │
│ [••••••••______________] │
│    [ Iniciar sesión ]    │   primario, ajustado al texto, centrado
│  (zona de estado)        │
└──────────────────────────┘
```
- La plantilla trae la pantalla **pasiva** (`LoginActivity`, `activity_login.xml`); el proyecto la conecta a su controlador. Tras un error se conserva el usuario y se limpia la clave.

### 5.2 <Siguiente pantalla>

## 6. Qué cambia en el código existente

| Pieza | Cambio |
|---|---|
| | |

## 7. Decisiones

| # | Tema | Decisión |
|---|---|---|
| M1 | Disposición | Una columna a ancho completo con 24 dp de margen; sin anchos máximos |
| M2 | Imagen | `fitCenter` siempre (completa y centrada). Login: cabecera a opacidad plena; resto: fondo fijo al 40 % |
| M3 | Botones | Apilados a ancho completo, con margen a los bordes; principal arriba y `Cancelar` debajo |
| M4 | Atrás | Equivale a `Cancelar` en las pantallas secundarias |
| M5 | Teclado | Se oculta al enviar; el tipo de teclado es solo una sugerencia |
