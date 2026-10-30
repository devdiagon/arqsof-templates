# Estilos compartidos de los clientes gráficos .NET (escritorio WPF, web MVC y móvil MAUI)

Define **un solo lenguaje visual** para clientes con interfaz gráfica en **escritorio (WPF), web (ASP.NET Core MVC con vistas Razor) y móvil (.NET MAUI)**.
Es la versión .NET de `CLIENT_STYLES_JAVA.md`: **misma estructura, mismas medidas, mismos componentes y mismas reglas**; cambian la **tecnología de implementación** (§7) y la **temática de color**, que pasa de azul a **morado**. Las únicas diferencias deliberadas, además de esas dos, están en §10.
Es **independiente del dominio** (no menciona qué hace la aplicación), del protocolo de comunicación y del lenguaje: sirve para cualquier proyecto cuyas pantallas combinen **acceso con credenciales, listas de acciones, formularios y resultados**.
No aplica a clientes de consola.

Aquí solo se define **cómo se ve**. El flujo, los textos concretos, las imágenes y las reglas de negocio de cada proyecto se documentan aparte (en `dominio/ESPECIFICACION_CLIENTES.md` y en el `docs/PANTALLAS.md` de cada cliente).

> Verificación: los **contrastes** de §2 se calcularon con la fórmula WCAG 2.x (luminancia relativa) sobre los valores exactos de las tablas, incluidos los de los componentes de §5.11 a §5.14. **Estado:** aplicado y verificado a mano en el escritorio (WPF, §7.3), en la web (ASP.NET Core MVC, §7.2) y en el móvil (MAUI, §7.1, en un teléfono real); la presentación clara no se ha probado en pantalla; lo que se propone con APIs concretas y no se comprobó se marca **(no verificado)**.
>
> Revisión 2026-10-29 (puesta al día con `CLIENT_STYLES_JAVA.md` del 2026-10-26 y 2026-10-27, tras construirse los clientes gráficos Java): el **escritorio y la web** conservan la disposición dividida solo en la pantalla de acceso; en el resto usan el ancho completo y la imagen pasa a ser el fondo de la ventana, centrada y con opacidad (§6, §7.0, §7.2, §7.3). Se agregan los componentes **cuadrícula de acciones**, **tarjeta de resultado**, **tabla** y **zona de estado** (§5.11 a §5.14). El **móvil** usa la imagen completa y centrada (`AspectFit`, nunca recortada): cabecera en el acceso y fondo fijo con opacidad en el resto (§6, §7.1). Una **imagen horizontal** se muestra compacta, al 60 % de su contenedor (§6). La imagen decorativa pasa a **PNG** (§6). El título de la tarjeta de resultado va en **negrita** (§3, §5.12).
>
> Revisión 2026-10-30 (primer cliente gráfico .NET construido, escritorio WPF): se ajustan §3 (fuente), §4 (`padding-campo`), §5.3 (campo), §5.6 (foco), §5.8 (indicador), §5.13 (tabla), §7.0 (alineación vertical) y §7.3 (carpetas, tipos de recursos, plantillas de campo y botón, ventana, desplazamiento, cuadrícula, tabla, zona de estado, indicador y recursos).
>
> Revisión 2026-10-30 (cliente web construido, ASP.NET Core MVC): se ajustan §5.13 (tabla), §6 (imagen), §7.0 (alineación vertical), §7.2 (CSS, estructura Razor, componentes `ui-*`, JavaScript y codificación), §9 y §10.
>
> Revisión 2026-10-30 (cliente móvil construido, .NET MAUI): se ajusta §7.1 (introducción, recursos, estructura de la página, estilos, campo, indicador, zona de estado, tarjeta de resultado, tarjeta compacta de registro, imagen, barra de estado y recursos de Android) y se retiran sus marcas **(no verificado)**.

---

## 1. Principios

1. **Una paleta, dos presentaciones.** Los tokens existen en versión **oscura** y **clara**. El cliente se publica con **una sola** de ellas
   por defecto; **no hay selector de tema** en la interfaz. Elegir la clara es cambiar la tabla de valores, no el diseño.
2. **Se programa con nombres, no con hex.** Cada color, tamaño y radio tiene un nombre (`fondo`, `primario`…). El código de cada cliente
   define esos nombres una vez (diccionarios de recursos XAML, variables CSS) y no repite valores sueltos.
3. **Mismo flujo y mismos textos** en todos los clientes gráficos de un proyecto (los define el proyecto, no este documento).
4. **Legible primero.** Todo texto cumple contraste **≥ 4,5:1** (WCAG AA). El color nunca es el único indicador: el botón se reconoce por su etiqueta.
5. **Sin acoplar el estilo al dominio.** Los componentes (§5) se describen por su función (encabezado, campo, botón, lista de acciones…), nunca por lo que hace la aplicación.
6. **Mismo estilo que Java, otro color.** Un proyecto que tenga clientes Java y .NET usa `CLIENT_STYLES_JAVA.md` en los primeros (azul) y este documento en los segundos (morado); los componentes y medidas son idénticos.

---

## 2. Paleta de colores

Temática principal: **violeta/morado**. Los neutros (fondos, bordes, textos) llevan un matiz morado para que toda la interfaz se sienta de una sola familia.

### 2.1 Tokens de marca y de acción (iguales en ambas presentaciones)

| Token | Hex | Uso |
|---|---|---|
| `acento` | `#A78BFA` | Marca: foco, indicadores de carga, iconos y enlaces **sobre fondo oscuro**, resaltes. **No** se usa como texto sobre fondo claro (2,5:1). |
| `primario` | `#6D28D9` | Relleno del **botón primario**. Texto blanco encima: **7,1:1**. |
| `primario-presionado` | `#5B21B6` | Botón primario al pulsarlo / `hover` (9,0:1 con blanco). |
| `secundario` | `#4B4563` | Relleno del **botón secundario** (acciones de salida o de regreso). Texto blanco: **9,0:1**. Gris con matiz morado, para que no compita con el primario. |
| `secundario-presionado` | `#3A3550` | Botón secundario al pulsarlo / `hover` (11,6:1 con blanco). |
| `sobre-accion` | `#FFFFFF` | Texto e iconos dentro de botones `primario` y `secundario`. |

### 2.2 Presentación **oscura** (por defecto)

| Token | Hex | Uso | Contraste |
|---|---|---|---|
| `fondo` | `#1A1426` | Fondo de pantalla/ventana | — |
| `superficie` | `#271D3A` | Paneles y tarjetas **sin** imagen de fondo | texto `#F5F3FF` 14,5:1 |
| `panel-sobre-imagen` | `rgba(0,0,0,0.60)` (`#99000000` en ARGB) | Paneles **cuando hay imagen de fondo bajo ellos** (§6) | texto blanco ≥ 10:1 |
| `texto` | `#F5F3FF` | Texto principal | 16,3:1 sobre `fondo` |
| `texto-secundario` | `#B4A9CE` | Ayudas, etiquetas secundarias | 8,1:1 sobre `fondo`; 7,2:1 sobre `superficie` |
| `borde` | `#3B2D57` | Separadores y contorno de paneles | decorativo |
| `error` | `#F87171` | Texto o indicador de error | 6,5:1 sobre `fondo`; 5,7:1 sobre `superficie` |
| `exito` | `#4ADE80` | Texto o indicador de éxito | 10,3:1 sobre `fondo`; 9,1:1 sobre `superficie` |

### 2.3 Presentación **clara**

| Token | Hex | Uso | Contraste |
|---|---|---|---|
| `fondo` | `#F7F5FC` | Fondo de pantalla/ventana | — |
| `superficie` | `#FFFFFF` | Paneles y tarjetas | texto `#1E1033` 17,8:1 |
| `panel-sobre-imagen` | — | **No se usa imagen de fondo** en la presentación clara (ver §6) | — |
| `texto` | `#1E1033` | Texto principal | 16,5:1 sobre `fondo` |
| `texto-secundario` | `#4C4263` | Ayudas, etiquetas secundarias | 8,6:1 sobre `fondo` |
| `borde` | `#D9D2EA` | Separadores y contorno de paneles | decorativo |
| `enlace` | `#6D28D9` | Enlaces y foco (en la oscura se usa `acento`) | 6,6:1 sobre `fondo` |
| `error` | `#B91C1C` | Texto o indicador de error | 6,0:1 sobre `fondo` |
| `exito` | `#15803D` | Texto o indicador de éxito | 4,6:1 sobre `fondo` |

### 2.4 Tokens de formulario (iguales en ambas presentaciones)

| Token | Hex | Uso | Contraste |
|---|---|---|---|
| `campo-fondo` | `#FFFFFF` | Fondo de campos de texto y selectores (blanco en ambas presentaciones) | — |
| `campo-texto` | `#1E1033` | Texto escrito | 17,8:1 |
| `campo-placeholder` | `#6B6280` | Texto de ayuda dentro del campo | 5,7:1 |
| `campo-borde` | `#6B6280` | Contorno del campo (**obligatorio en la presentación clara**, opcional en la oscura) | 5,3:1 sobre `fondo` claro; 5,7:1 sobre `superficie` |

### 2.5 Tokens de la tarjeta de resultado (iguales en ambas presentaciones)

La tarjeta (§5.12) tiene fondo blanco en las dos presentaciones, así que usa los tokens de formulario y estos, que no cambian con la presentación:

| Token | Valor | Uso | Contraste |
|---|---|---|---|
| `exito-claro` | `#15803D` | Monto que **suma** | 5,0:1 sobre blanco |
| `error-claro` | `#B91C1C` | Monto que **resta** | 6,5:1 sobre blanco |
| `divisor-tarjeta` | `#D9D2EA` | Línea de 1 entre la variación y el pie (es el `borde` de la clara) | decorativo |
| `contorno-tarjeta` | `primario` al **50 %** (`#806D28D9` ARGB; `rgba(109,40,217,.5)`) | Contorno de 1,5 | decorativo |

Un verde o un rojo más pálido que `exito-claro` / `error-claro` incumpliría el contraste sobre blanco; por eso la tarjeta no usa los `exito` y `error` de la presentación oscura.

Notas de contraste:
- `primario` sobre `fondo` oscuro da solo 2,5:1: el botón se reconoce por su **etiqueta blanca (7,1:1)** y su forma, no solo por el relleno. Lo mismo vale para `secundario` sobre `fondo` oscuro (2,0:1).
- `acento` (`#A78BFA`) es el color de marca sobre fondo oscuro (6,6:1); no se usa como relleno de botón porque con texto blanco no alcanza contraste, ni como texto sobre fondo claro.
- Diferencias frente a `CLIENT_STYLES_JAVA.md`: el `primario` morado tiene **más** contraste con el texto blanco que el azul original (7,1:1 frente a 5,9:1), por lo que los botones quedan más legibles; el resto de los tokens (`error`, `exito`, `campo-*`) conserva su función con ajustes de matiz.
- Usos nuevos (§5.11 a §5.14), todos con texto ≥ 4,5:1: barra de la zona de estado sobre `superficie` (`error` 5,7:1, `exito` 9,1:1); texto de la tabla sobre `superficie` (14,5:1) y sobre el peor caso de `panel-sobre-imagen`, el negro (19,1:1); encabezados de columna en `texto-secundario` sobre `superficie` (7,2:1); texto del botón de la cuadrícula, el mismo `primario` con blanco (7,1:1).

---

## 3. Tipografía

| Plataforma | Familia | Notas |
|---|---|---|
| **Móvil (MAUI)** | La del sistema (`Roboto` en Android, `Segoe UI` en Windows) | No se incrusta ninguna fuente: eliminar `OpenSans` de la plantilla de MAUI (`ConfigureFonts` en `MauiProgram.cs`) o dejarla sin usar en los estilos. |
| **Web (MVC)** | `"Inter", "Segoe UI", Roboto, system-ui, -apple-system, Arial, sans-serif` | `Inter` es opcional (Google Fonts); la pila siempre cae a fuentes del sistema. |
| **Escritorio (WPF)** | `Segoe UI` (la predeterminada de Windows) | No forzar otra familia; fijarla una vez como recurso `FuenteBase` y aplicarla en la `Window`. |

Escala por rol (tamaño en **dp** en MAUI, **px** en web y **unidades independientes del dispositivo —DIP, 1/96 pulgada—** en WPF):

| Rol | Móvil | Web | Escritorio | Peso |
|---|---|---|---|---|
| Título de aplicación (pantalla de acceso) | 24 | 32 | 28 | Bold (700) |
| Título de pantalla | 22 | 28 | 24 | Bold (700) |
| Resultado | 18 | 22 | 18 | Medium (500) |
| Cuerpo y texto de campos | 16 | 16 | 14 | Regular (400) |
| Etiqueta de campo | 14 | 14 | 13 | Bold (700) |
| Mensajes y ayudas | 14 | 14 | 13 | Regular (400) |
| Botón | 18 | 16 | 15 | Bold (700) |

Reglas: máximo **dos pesos** por pantalla (regular y bold; *Medium* solo para el resultado); sin cursiva; sin mayúsculas forzadas; los textos llevan las tildes y caracteres propios del idioma.
**Título de la tarjeta de resultado (§5.12): Bold** en las tres plataformas .NET. En `CLIENT_STYLES_JAVA.md` es *Semibold*; aquí se usa negrita porque *Semibold* no existe en MAUI sin una fuente propia y así se mantienen dos pesos por pantalla.
En WPF `FontWeight.Medium` y `Bold` se aplican con `FontWeights`; en MAUI, `FontAttributes="Bold"` (el peso *Medium* no existe sin fuente propia: usar regular).

---

## 4. Medidas

**Unidad base: 4.** Escala de espaciado: `4 · 8 · 12 · 16 · 24 · 32`.

| Token | Valor | Uso |
|---|---|---|
| `radio` | **8** (dp / px / DIP) | Esquinas de botones, campos, selectores y paneles. Un único radio en todo el cliente. |
| `margen-pantalla` | **24** en todas las plataformas | Margen lateral del contenido. |
| `ancho-columna` | móvil: todo el ancho menos márgenes; web y escritorio: **420** con disposición dividida (§7.0), **480** (web) en columna única | Ancho del contenido de una pantalla. |
| `espacio-bloque` | **16** | Separación entre campos y paneles consecutivos. |
| `espacio-seccion` | **24** (entre grupos) y **32** (antes de las acciones de salida) | |
| `altura-minima-control` | móvil **48**; web **44**; escritorio **36** | Campos, selectores y botones. En móvil es también el área táctil mínima. |
| `padding-panel` | **16** (título) y **12** (mensaje/resultado) | |
| `padding-boton` | móvil **12 × 24**; web **10 × 20**; escritorio **8 × 20** | Vertical × horizontal. |
| `padding-campo` | móvil y web **12**; escritorio **6 × 12** | Vertical × horizontal. En WPF, `Thickness` se escribe horizontal, vertical (campo `12,6`; botón `20,8`). |
| `ancho-botón` | **100 %** del contenido para acciones de lista y de salida; **ajustado al texto y centrado** para la acción principal de un formulario | |

Elevación: **ninguna** (diseño plano). No usar sombras (`DropShadowEffect` en WPF, `Shadow` en MAUI, `box-shadow` en CSS); la jerarquía se logra con color y espacio.

---

## 5. Componentes

Cada componente se define por **tokens**, no por tecnología. Los nombres son los que se usan en los recursos de cada cliente (§7). Son los mismos que en `CLIENT_STYLES_JAVA.md`.

### 5.1 Encabezado de pantalla (panel de título)
Panel con un único texto. Fondo `superficie` (o `panel-sobre-imagen` si hay imagen debajo), `radio`, `padding-panel` 16, texto `texto` en **título de pantalla** (o **título de aplicación** en la pantalla de acceso, centrado).
Va **siempre arriba** y es el primer elemento de la pantalla; no lleva botones.

### 5.2 Panel de mensaje / resultado
Mismo fondo y radio que el encabezado, `padding-panel` 12, texto `texto`. Oculto hasta que haya contenido.
- **Resultado:** tamaño *Resultado*, centrado, altura mínima 72. Es una línea corta de texto plano. Cuando el resultado son varios datos con un importe (una operación que cambia un saldo), se usa la **tarjeta de resultado** (§5.12).
- **Mensaje de error o de validación:** tamaño *Mensajes y ayudas*, alineado a la izquierda. Se muestra **solo el mensaje**, nunca un código técnico interno.
- Recomendado: reforzar el estado con una barra de **4** a la izquierda en `error` / `exito` (no obligatorio; el texto debe seguir siendo comprensible sin el color). En la zona de estado (§5.14) la barra es obligatoria.

### 5.3 Campo de texto
Fondo `campo-fondo`, texto `campo-texto`, *placeholder* `campo-placeholder`, `radio`, alto mínimo `altura-minima-control`, `padding-campo`. Sin borde en la presentación oscura; con `campo-borde` de 1 en la clara.
Estados: **foco** → borde de 2 en `acento` (oscura) o `enlace` (clara); **deshabilitado** → opacidad 60 %; **error** → borde de 2 en `error`.
Los campos de contraseña ocultan el contenido.

### 5.4 Etiqueta de campo
Texto `texto` en *Etiqueta*, **encima** del campo, separada 6 del campo. Alineada con el margen izquierdo.

### 5.5 Selector (lista desplegable)
Igual que un campo de texto, con una **flecha hacia abajo** (24 × 24, color `#424242`) alineada a la derecha a 12 del borde, y el texto con `padding` derecho 40 para no pisarla.
La lista desplegable usa `campo-fondo` y `campo-texto`.

### 5.6 Botón primario
Relleno `primario`, texto `sobre-accion`, *Botón* bold, `radio`, alto mínimo `altura-minima-control`, `padding-boton`.
Estados: **presionado/hover** → `primario-presionado`; **foco de teclado** (escritorio) → contorno de 2 en `acento`, superpuesto; **deshabilitado** → opacidad 60 % y sin respuesta; **cargando** → deshabilitado + indicador de progreso (§5.8) junto al botón, no dentro.
Se usa para **la acción principal de la pantalla**. **Una acción principal por pantalla**; en una lista de acciones (§5.9) cada botón es primario porque cada uno es la acción.

### 5.7 Botón secundario
**Sólido** (no de contorno): relleno `secundario`, texto `sobre-accion`, mismas medidas que el primario. Estados: presionado/hover → `secundario-presionado`; deshabilitado → opacidad 60 %.
Se usa para **acciones de salida o de regreso** (cerrar sesión, volver, reintentar si no es la acción principal). Ancho 100 % del contenido, separado **32** del bloque anterior.

### 5.8 Indicador de progreso
Circular, indeterminado, color `acento` (oscura) o `primario` (clara), 36–48 de diámetro, centrado bajo la acción que lo originó. Escritorio: barra lineal indeterminada de 160 × 6, junto a su texto. Mientras está visible, los controles de esa pantalla están **deshabilitados** (evita doble envío).

### 5.9 Lista de acciones
Una columna de botones primarios de ancho completo, separados 16, generados con datos que provee el sistema (nunca fijos en el cliente). Debajo, 32 de separación y el botón secundario de salida.

### 5.10 Fondo
`fondo` plano. Opcional: **imagen decorativa** (§6). El fondo no contiene información ni controles.

### 5.11 Cuadrícula de acciones (escritorio y web)
Variante de la lista de acciones (§5.9) para el ancho completo: los botones primarios se disponen en una **rejilla de 2 columnas**, de igual tamaño, separados `espacio-bloque` (16) en ambos sentidos y **sin superponerse**. Cada botón es **grande**: altura **96**, texto *Botón* centrado; el ancho es la mitad del contenido menos la mitad de la separación. Los datos siguen viniendo del sistema (nunca fijos en el cliente). El botón secundario de salida va **debajo**, ancho completo, separado 32. Si el ancho no alcanza para dos columnas (ventana mínima), la rejilla se mantiene y los botones se reducen en ancho; el texto largo se parte en dos líneas. En la web, bajo 600 px de ancho pasa a una sola columna.

### 5.12 Tarjeta de resultado
Resume **un cambio de un valor** (por ejemplo, el efecto de una operación sobre un saldo). Es plana (sin sombra), con el mismo `radio` (8), `padding` 16 y **ancho fijo de 260**, con un **contorno de 1,5 en `contorno-tarjeta`** (el `primario` al 50 %, §2.5); su altura se ajusta al contenido. Se compone, de arriba abajo, de:

| Parte | Rol tipográfico (§3) | Color |
|---|---|---|
| **Título** (opcional), **centrado**: una etiqueta o el mensaje de éxito | Cuerpo en **Bold** | `campo-placeholder` |
| **Monto** con signo y símbolo, p. ej. `+$250.00` o `-$250.00` | Título de pantalla (24 bold) | `exito-claro` si suma, `error-claro` si resta |
| **Variación**: `{antes} → {después}` | Cuerpo | `campo-texto` |
| **Divisor**: línea de 1 | — | `divisor-tarjeta` |
| **Pie**: identificación del origen del cambio, alineado a la derecha | Mensajes y ayudas | `campo-placeholder` |

- Fondo `campo-fondo` (blanco) en **ambas** presentaciones: es una superficie de lectura, como un campo. Por eso sus textos usan los tokens de formulario y `exito-claro` / `error-claro` (§2.5), que dan 5,0:1 y 6,5:1 sobre blanco.
- El signo (`+`, `-`) acompaña siempre al color: el color nunca es el único indicador.
- Varias tarjetas se disponen en fila, separadas `espacio-bloque`, centradas. En el móvil (columna única) ocupan el ancho completo y se apilan.
- Oculta hasta que hay contenido.

### 5.13 Tabla
Lista de registros con columnas. Fondo `panel-sobre-imagen` si hay imagen debajo, o `superficie`; `radio`; texto `texto` en *Cuerpo*. **Encabezado** de columnas en *Etiqueta* (13 bold) con una línea `borde` de 1 debajo; **filas** de **36** de alto separadas por una línea `borde` de 1; sin cuadrícula vertical, sin sombras, sin selección. Columnas de texto alineadas a la izquierda y las **numéricas a la derecha**. Si hay más filas de las que caben, la tabla se desplaza por dentro y el encabezado queda fijo. En el escritorio la tabla ocupa el alto que queda de la pantalla (sin máximo de filas); en la web tiene un alto máximo de encabezado y 10 filas y se desplaza por dentro.
En el móvil (columna única) no hay tabla: cada registro se dibuja como una **tarjeta compacta** (§7.1).

### 5.14 Zona de estado
Un solo lugar, **bajo los botones** de la pantalla, que muestra de una en una: el **indicador de ocupado** con su texto (§5.8), un **error** o un **éxito**. El error y el éxito son un panel de mensaje (§5.2) con una **barra izquierda** de 4 del color `error` o `exito`; el texto es siempre el mensaje del sistema, nunca un código. Oculta si no hay nada que mostrar. Mientras hay una llamada en curso, todos los controles de la pantalla (incluido `Cancelar`) quedan deshabilitados.

---

## 6. Imagen decorativa (opcional)

- **Formato PNG** (especificación del 2026-10-29): admite transparencia y no degrada los bordes. No se usa JPEG. En la web el archivo es `fondo_app.png`, el mismo que usa el escritorio.
- Solo en la **presentación oscura**. La imagen **nunca se deforma**.
- Si la imagen es **vertical y opaca**, y sus **bordes tienen exactamente el color `fondo`** (`#1A1426`), puede mostrarse completa sobre `fondo` y fundirse sin costuras. Es la condición para usar el modo `contain` descrito abajo; si una imagen no la cumple, debe usarse con paneles `panel-sobre-imagen` o recortada. *Una imagen opaca preparada para el `fondo` azul de `CLIENT_STYLES_JAVA.md` (`#111E2F`) no cumple esta condición: hay que regenerarla con el color morado.* Si la imagen tiene **transparencia**, el color `fondo` se ve a través de ella y no se exige que sus bordes sean del color `fondo` (hay que comprobar, eso sí, que sus colores se lean sobre el morado).
- **Imagen horizontal (p. ej. un logotipo apaisado):** se muestra **completa y centrada** pero **compacta**: ocupa como máximo el **60 % del alto y del ancho de su contenedor** (un margen del 20 % por lado), en lugar de llenarlo, para que no domine el fondo. Vale para el acceso y para el fondo de la ventana; el tamaño del PNG no importa, la escala la fija el contenedor. **WPF y MAUI:** como XAML no admite márgenes en porcentaje, la imagen va en la celda central de un `Grid` de 3 × 3 con proporciones `0.2* / 0.6* / 0.2*` en filas y columnas, con `Stretch="Uniform"` (WPF) o `Aspect="AspectFit"` (MAUI); los valores viven en **recursos únicos** (`MargenImagen` = `0.2*` y `CentroImagen` = `0.6*`, que deben sumar 1 con el margen duplicado). **Web:** variable CSS `--margen-imagen: 20%`, usada como `inset` del `::before` de `.zona-imagen` (acceso) y de la capa fija de `.pagina--aplicacion` (resto), con `background-size: contain`. **El 60 % es un punto de partida:** se ajusta según la imagen del proyecto (más grande, más pequeña o 100 %) cambiando ese único valor.
- **Columna única (móvil):** la imagen se muestra **completa y centrada** (`AspectFit`, nunca `AspectFill`), sin recortar ni deformar; lo que sobra es `fondo`. **Acceso:** va como **cabecera** del formulario (≈ 160 dp de alto) a opacidad plena, en su propia zona, y los paneles usan `superficie`. **Resto de pantallas:** es el **fondo fijo** de la pantalla (fuera del contenedor que se desplaza), con **opacidad 40 %**, y los paneles usan `panel-sobre-imagen` para que el texto se lea sobre ella. En una imagen horizontal, un teléfono vertical deja bandas de `fondo` arriba y abajo.
- **Escritorio y web, fuera de la pantalla de acceso (§7.0):** la imagen es el **fondo de la ventana**: ocupa toda el área, **completa y centrada** (`Stretch="Uniform"` en WPF; `contain` en CSS, nunca `UniformToFill`/`cover`), sin deformarse; las franjas que sobran son `fondo`. Se dibuja con **opacidad 40 %** sobre `fondo` y se **reescala con la ventana** (al maximizar o redimensionar mantiene su proporción y sigue centrada). Como el contenido queda encima de la imagen, los paneles usan `panel-sobre-imagen`. La opacidad es una constante única y se puede ajustar. En la web es una capa fija (`position: fixed`) detrás del contenido, con `background-size: contain` y `opacity: .4`; bajo 900 px no se muestra (columna única sin imagen) y la presentación clara tampoco la usa.
- **Pantalla ancha con disposición dividida (el acceso, en escritorio y web, §7.0):** la imagen ocupa su propia zona, **completa y centrada** (`Stretch="Uniform"` en WPF; `background-size: contain` en CSS), sin recortar ni deformar; el resto de la zona es `fondo`. No se usa `UniformToFill`/`cover`: al maximizar la ventana, una imagen vertical ampliada para cubrir una zona ancha se recorta y parece estirada. Como el contenido no queda sobre la imagen, los paneles usan `superficie`.
- Sin imagen, los paneles usan `superficie`.
- **Siempre** definir `fondo` como color de respaldo por si la imagen no carga.
- En la presentación clara **no se usa imagen**: el fondo es `fondo` plano. Si se requiere imagen en claro, debe pasar una revisión de contraste propia.
- Logotipos o marcas del cliente **no forman parte** del estilo: si el proyecto los usa, se definen en su propia documentación.

---

## 7. Implementación por plataforma

### 7.0 Disposición según el ancho (columna única frente a disposición dividida)
El móvil es una **columna única**. El escritorio y la web **aprovechan el ancho horizontal** con una disposición dividida, la misma para ambos:

| Zona | Contenido |
|---|---|
| **Izquierda** (ancho fijo, ≈ 560) | La pantalla actual, en una columna de `ancho-columna` (**420**), **centrada** horizontal y verticalmente en la zona. |
| **Derecha** (el resto) | La imagen decorativa (§6), centrada. Solo decora: no contiene información ni controles. |

- La imagen es **parte de la ventana, no de cada pantalla**: se ve igual en todas mientras se navega.
- **Escritorio y web:** la disposición dividida se usa **solo en la pantalla de acceso**. En las demás pantallas el contenido ocupa el **ancho completo** de la ventana (con `margen-pantalla` a los lados) sobre la imagen de fondo (§6), sin zona derecha. Ancho del contenido (centrado): **480** en formularios de una columna, **640** en la cuadrícula de acciones (§5.11) y en formularios con dos campos en fila, y **todo el ancho disponible** en tablas (§5.13). En la web son anchos máximos (`max-width`), nunca fijos: bajo 900 px el contenido ocupa el ancho de la pantalla menos los márgenes.
- **Alineación vertical:** las pantallas se centran en vertical; la pantalla de tabla se ubica arriba. Escritorio: se desplazan solo si no caben; la de tabla ocupa el alto de la ventana y no se desplaza: solo la tabla lo hace, por dentro (§5.13, §7.3). Web: `contenido--completo` con `align-self: flex-start`; la página se desplaza y la tabla lo hace por dentro (§5.13, §7.2).
- Las medidas de componentes (§4 y §5) no cambian; solo cambia dónde se colocan.
- Si el ancho disponible es menor que el mínimo de la plataforma, la zona derecha puede quedar muy estrecha: en escritorio se fija un mínimo de ventana (§7.3); en la web se pasa a columna única sin imagen por debajo de ese ancho (§7.2).

### 7.1 Móvil (.NET MAUI, XAML)
Particularidades de la **columna única**: todos los elementos se apilan a ancho completo con `margen-pantalla` (24) a cada lado; los botones de una acción van **apilados** (el principal arriba y `Cancelar` debajo, separados 16, ambos ajustados al texto y centrados) y nunca en fila; los botones de un menú son primarios de **96 de alto**, a ancho completo, separados 16 (la cuadrícula de §5.11 es del escritorio y la web); una tarjeta de resultado (§5.12) ocupa el ancho completo y varias se apilan; una lista de registros se dibuja como **una tarjeta compacta por registro** (no una tabla), apiladas y dentro del mismo desplazamiento de la pantalla (un `BindableLayout` sobre un `VerticalStackLayout` dentro del `ScrollView`; un `CollectionView` desplazaría por su cuenta). Se oculta el teclado al enviar un formulario para que la zona de estado (§5.14) quede visible (`Unfocus()` de los campos al pasar a ocupado). El campo de importe usa teclado numérico.

Los tokens son recursos en `Resources/Styles/Colors.xaml`; los estilos, en `Resources/Styles/Styles.xaml`. Las páginas (`ContentPage`) **solo referencian estilos** con `{StaticResource …}` o `Style="{StaticResource …}"` y nunca valores sueltos. La plantilla de MAUI trae colores y estilos de ejemplo (`Primary`, `Gray…`): **reemplazarlos** por estos tokens para que no haya dos paletas.

```xml
<!-- Colors.xaml (presentación oscura) -->
<Color x:Key="Fondo">#1A1426</Color>
<Color x:Key="Superficie">#271D3A</Color>
<Color x:Key="PanelSobreImagen">#99000000</Color>
<Color x:Key="Texto">#F5F3FF</Color>
<Color x:Key="TextoSecundario">#B4A9CE</Color>
<Color x:Key="Borde">#3B2D57</Color>
<Color x:Key="Acento">#A78BFA</Color>
<Color x:Key="Primario">#6D28D9</Color>
<Color x:Key="PrimarioPresionado">#5B21B6</Color>
<Color x:Key="Secundario">#4B4563</Color>
<Color x:Key="SecundarioPresionado">#3A3550</Color>
<Color x:Key="SobreAccion">#FFFFFF</Color>
<Color x:Key="Error">#F87171</Color>
<Color x:Key="Exito">#4ADE80</Color>
<Color x:Key="CampoFondo">#FFFFFF</Color>
<Color x:Key="CampoTexto">#1E1033</Color>
<Color x:Key="CampoPlaceholder">#6B6280</Color>
<!-- tarjeta de resultado (§2.5) -->
<Color x:Key="ExitoClaro">#15803D</Color>
<Color x:Key="ErrorClaro">#B91C1C</Color>
<Color x:Key="DivisorTarjeta">#D9D2EA</Color>
<Color x:Key="ContornoTarjeta">#806D28D9</Color>
<!-- imagen compacta (§6): filas y columnas del Grid 3 x 3 -->
<GridLength x:Key="MargenImagen">0.2*</GridLength>
<GridLength x:Key="CentroImagen">0.6*</GridLength>
<x:Double x:Key="OpacidadImagen">0.4</x:Double>

<!-- Styles.xaml -->
<Style x:Key="BotonPrimario" TargetType="Button">
    <Setter Property="BackgroundColor" Value="{StaticResource Primario}" />
    <Setter Property="TextColor" Value="{StaticResource SobreAccion}" />
    <Setter Property="FontSize" Value="18" />
    <Setter Property="FontAttributes" Value="Bold" />
    <Setter Property="CornerRadius" Value="8" />
    <Setter Property="MinimumHeightRequest" Value="48" />
    <Setter Property="Padding" Value="24,12" />
    <Setter Property="VisualStateManager.VisualStateGroups">
        <VisualStateGroupList>
            <VisualStateGroup x:Name="CommonStates">
                <VisualState x:Name="Normal" />
                <VisualState x:Name="Pressed">
                    <VisualState.Setters><Setter Property="BackgroundColor" Value="{StaticResource PrimarioPresionado}" /></VisualState.Setters>
                </VisualState>
                <VisualState x:Name="Disabled">
                    <VisualState.Setters><Setter Property="Opacity" Value="0.6" /></VisualState.Setters>
                </VisualState>
            </VisualStateGroup>
        </VisualStateGroupList>
    </Setter>
</Style>
<Style x:Key="BotonSecundario" TargetType="Button" BasedOn="{StaticResource BotonPrimario}">
    <Setter Property="BackgroundColor" Value="{StaticResource Secundario}" />
    <!-- repetir el estado Pressed con SecundarioPresionado -->
</Style>
<Style x:Key="BotonMenu" TargetType="Button" BasedOn="{StaticResource BotonPrimario}">
    <Setter Property="MinimumHeightRequest" Value="96" />   <!-- botón de menú del móvil (§7.1) -->
</Style>
```

- **Campo y selector:** `Entry` y `Picker` no tienen radio ni borde propios. El campo es el componente `CampoTexto` (`ContentView`): un `Border` (`StrokeShape="RoundRectangle 8"`, `BackgroundColor="{StaticResource CampoFondo}"`, `Padding="12,0"`, `MinimumHeightRequest="48"`) con un `Entry` transparente (`TextColor` `CampoTexto`, `PlaceholderColor` `CampoPlaceholder`, tamaño 16). Propiedades enlazables: `Text` (bidireccional), `Placeholder`, `EsClave`, `Teclado` e `Invalido`; métodos `Enfocar()` y `Desenfocar()`. El foco y el error cambian el `Stroke` del `Border` (2 en `Acento` / `Error`; sin foco ni error, grosor 0). En Android se quita el subrayado del `Entry` con un `EntryHandler.Mapper.AppendToMapping` en `MauiProgram` (`BackgroundTintList` transparente). El `Picker` se envuelve del mismo modo **(no verificado)**.
- **Aplicación:** `Application.UserAppTheme = AppTheme.Dark` (o `Light`) fijo en el constructor de `App`, sin `AppThemeBinding`: no hay selector de tema (§1). La presentación clara redefine los mismos nombres en `Colors.xaml` con los valores de §2.3.
- **Indicador de progreso:** `ActivityIndicator` (estilo implícito con `Color="{StaticResource Acento}"`) de 40 × 40, con su texto, dentro de la zona de estado; los controles se deshabilitan mientras la pantalla está ocupada.
- **Zona de estado (§5.14):** componente `ZonaEstado` (`ContentView`) bajo los botones, enlazado al controlador de la pantalla (su `BindingContext`) y con la propiedad `Fondo` (`Superficie` en el acceso, `PanelSobreImagen` en el resto). Contiene dos `Border` excluyentes: ocupado (`ActivityIndicator` y texto) y mensaje (barra izquierda de 4, un `BoxView`, en `Error` / `Exito`, y texto con margen 12). Su visibilidad la fija el código según `Estado` (`Ninguno`, `Ocupado`, `Error`, `Exito`); con `Ninguno` el componente se oculta. El `Grid` raíz de cada pantalla enlaza `IsEnabled` a `NoOcupado`, de modo que mientras hay una llamada en curso toda la pantalla, incluido `Cancelar`, queda deshabilitada.
- **Imagen (§6):** archivo **PNG** declarado como imagen de MAUI (`Resources/Images/fondo_app.png`, referenciada como `fondo_app`). `Aspect="AspectFit"` (nunca `AspectFill`). **Acceso:** el `Image` es la cabecera del formulario, dentro de un `Grid` de **160** de alto con `Fondo` y las filas y columnas `MargenImagen` / `CentroImagen` (imagen horizontal compacta), a opacidad plena. **Resto de pantallas:** una sola `ContentPage` (`PaginaPrincipal`) con un `Grid` raíz; la capa de imagen (un `Grid` de 3 × 3 con `Fondo` e `InputTransparent="True"`, con el `Image` en la celda central y `Opacity="{StaticResource OpacidadImagen}"`) va detrás del `ScrollView`, **fuera** del contenedor que se desplaza, y solo es visible fuera del acceso; los paneles usan `PanelSobreImagen`.
- **Tarjeta de resultado (§5.12):** componente `TarjetaResultado` (`Border`) de ancho completo con el estilo `Tarjeta` (`StrokeShape="RoundRectangle 8"`, `Stroke` `ContornoTarjeta`, `StrokeThickness` 1.5, `BackgroundColor` `CampoFondo`, `Padding` 16); su `BindingContext` es el dato de la tarjeta. Título centrado en bold y color `CampoPlaceholder`; monto 22 bold centrado, en `ExitoClaro` y, con un `DataTrigger` sobre el indicador de suma, en `ErrorClaro`; variación centrada en `CampoTexto`; divisor con un `BoxView` de alto 1 en `DivisorTarjeta`; pie de tamaño 14 alineado a la derecha en `CampoPlaceholder`. Varias tarjetas se apilan, separadas 16 (`BindableLayout`).
- **Tarjeta compacta de registro (§5.13):** componente `TarjetaFila` (`Border` con `PanelSobreImagenEstilo` y `Padding` 12) con un `Grid` de dos columnas: la etiqueta en `TextoAyuda` a la izquierda y el valor a la derecha, una fila por dato; el número y el importe en bold. Una fila opcional (p. ej. la cuenta de referencia) solo es visible si el registro tiene valor. Se apilan separadas 12 con un `BindableLayout`.
- **Barra de estado y zonas seguras:** no se fija el color de la barra de estado ni se añaden zonas seguras propias; el contenido lleva `margen-pantalla` (`Padding` 24 del contenedor de la pantalla) sobre `Fondo`.
- **Estructura:** una sola `PaginaPrincipal` (`ContentPage`) aloja la pantalla actual: un `ContentView` por pantalla, con su controlador como `BindingContext`; sin `Shell`. El `ScrollView` contiene un `Grid` con `Padding` 24 cuyo `MinimumHeightRequest` sigue el alto del `ScrollView`, y dentro un `ContentView` centrado en vertical (arriba en la pantalla de lista de registros). Al mostrar una pantalla se limpia su controlador (`AlMostrar()`) y el desplazamiento vuelve al inicio.
- **Estilos:** implícitos para `Page` (`BackgroundColor` `Fondo`) y `Label` (`Texto`, 16); con clave: `TituloAplicacion`, `TituloPantalla`, `EtiquetaCampo`, `TextoMensaje`, `TextoAyuda`, `PanelSuperficie`, `PanelSobreImagenEstilo`, `BotonPrimario`, `BotonSecundario`, `BotonMenu`, `Tarjeta`, `TarjetaTitulo`, `TarjetaTexto`, `TarjetaPie` y `TarjetaDivisor`.
- **Recursos de Android:** `Platforms/Android/Resources/values/colors.xml` y el `Color` de `MauiIcon` y `MauiSplashScreen` en `Fondo` (`#1A1426`).
- Reglas MVVM: la `ContentPage` no tiene lógica; el ViewModel expone propiedades y comandos (`CommunityToolkit.Mvvm` es opcional).

### 7.2 Web (ASP.NET Core MVC con vistas Razor)
Los tokens son variables CSS; el tema por defecto se fija con **una clase en `<body>`** (`tema-oscuro` o `tema-claro`), sin script ni selector en pantalla. El CSS vive en `wwwroot/css/estilos.css` (reemplaza `site.css` de la plantilla; **quitar Bootstrap** o no usar sus clases en las vistas, para que no haya dos sistemas de estilos).

```css
:root { --radio: 8px; --primario: #6D28D9; --primario-p: #5B21B6; --secundario: #4B4563;
        --secundario-p: #3A3550; --acento: #A78BFA; --sobre-accion: #FFFFFF;
        --campo-fondo: #FFFFFF; --campo-texto: #1E1033; --campo-borde: #6B6280;
        --margen-imagen: 20%;                        /* imagen horizontal compacta (§6); 0 para una vertical */ }
.tema-oscuro { --fondo: #1A1426; --superficie: #271D3A; --texto: #F5F3FF; --texto-sec: #B4A9CE;
               --borde: #3B2D57; --error: #F87171; --exito: #4ADE80; --foco: var(--acento); }
.tema-claro  { --fondo: #F7F5FC; --superficie: #FFFFFF; --texto: #1E1033; --texto-sec: #4C4263;
               --borde: #D9D2EA; --error: #B91C1C; --exito: #15803D; --foco: #6D28D9; }
body { background: var(--fondo); color: var(--texto);
       font: 16px/1.4 "Inter","Segoe UI",Roboto,system-ui,-apple-system,Arial,sans-serif; }
.boton { min-height: 44px; padding: 10px 20px; border: 0; border-radius: var(--radio);
         color: var(--sobre-accion); font: 700 16px inherit; cursor: pointer; }
.boton--primario { background: var(--primario); }   .boton--primario:hover { background: var(--primario-p); }
.boton--secundario { background: var(--secundario); } .boton--secundario:hover { background: var(--secundario-p); }
.boton:disabled { opacity: .6; cursor: default; }
.campo { min-height: 44px; padding: 12px; border-radius: var(--radio); background: var(--campo-fondo);
         color: var(--campo-texto); border: 1px solid var(--campo-borde); }
.campo:focus { outline: 2px solid var(--foco); }
.contenedor { max-width: 480px; margin: 0 auto; padding: 24px; }
```
Disposición **dividida** (§7.0): rejilla de dos zonas a pantalla completa, contenido a la izquierda e imagen a la derecha.

```css
.pagina { display: grid; grid-template-columns: 560px 1fr; min-height: 100vh; background: var(--fondo); }
.zona-contenido { display: grid; place-items: center; padding: 24px; }
.columna { width: 100%; max-width: 420px; }
.zona-imagen { position: relative; background: var(--fondo); }
.zona-imagen::before { content: ""; position: absolute; inset: var(--margen-imagen);
                       background: url("../img/fondo_app.png") center / contain no-repeat; }
@media (max-width: 899px) {                   /* bajo el mínimo del escritorio: columna única, sin imagen */
  .pagina { grid-template-columns: 1fr; }
  .zona-imagen { display: none; }
}
```
`.contenedor { max-width: 480px }` queda solo para la variante de columna única (ancho < 900). La imagen usa `contain` (§6), nunca `cover`.

**Fuera del acceso** (§7.0): la página usa el modo `aplicacion`, sin zona derecha; el contenido va centrado y la imagen es una capa de fondo.

```css
.pagina--aplicacion { display: flex; align-items: center; justify-content: center; min-height: 100vh; background: transparent; }
.pagina--aplicacion::before {                  /* imagen de fondo: fija, completa, centrada, con opacidad */
  content: ""; position: fixed; inset: var(--margen-imagen); z-index: -1; opacity: .4;
  background: url("../img/fondo_app.png") center / contain no-repeat; }
.contenido { width: 100%; max-width: 480px; margin: 0 auto; padding: 24px; }
.contenido--amplio { max-width: 640px; }                              /* menú, transferencia */
.contenido--completo { max-width: none; align-self: flex-start; }     /* tabla: arriba */
.pagina--aplicacion .columna { max-width: none; }
.pagina--aplicacion .encabezado, .pagina--aplicacion .mensaje, .pagina--aplicacion .tabla-contenedor { background: rgba(0,0,0,.60); } /* panel-sobre-imagen */
@media (max-width: 899px) { .pagina--aplicacion::before { display: none; } }
.tema-claro .pagina--aplicacion::before { display: none; }
```
Tarjeta, cuadrícula y tabla (§5.11 a §5.13): tokens propios `--exito-claro: #15803D`, `--error-claro: #B91C1C`, `--divisor-tarjeta: #D9D2EA` y `--contorno-tarjeta: rgba(109, 40, 217, .5)`; el título de la tarjeta usa `font-weight: 700`. La tabla va dentro de un contenedor con `overflow-x: auto`, de modo que **la página nunca se desplaza en horizontal**. La zona de estado (§5.14) es un solo contenedor bajo los botones; el error y el éxito llevan `border-left: 4px solid var(--error | --exito)`.

Tokens propios adicionales: `--altura-control: 44px`, `--alto-fila: 36px`, `--alto-boton-cuadricula: 96px` y `--panel-solido: #0A0810` (el `panel-sobre-imagen` sobre `fondo`, opaco; `#FFFFFF` en la clara).
- **Tabla:** `.tabla-contenedor` con `padding: 12px`, `overflow: auto` y `max-height: calc(var(--alto-fila) * 11)`; encabezado `position: sticky; top: -12px` con fondo `--panel-solido`; celdas de `--alto-fila` con `white-space: nowrap`; columnas numéricas con la clase `num` (`text-align: right`).
- **Flecha de `ui-fila-campos`:** `.fila-campos` es una rejilla `1fr auto 1fr` con `align-items: end`; la flecha es `.fila-campos::before` en la columna central (24 × 24, `background: var(--texto)` con `mask` de un SVG, margen inferior `(--altura-control − 24px) / 2`).
- **Campo con acciones:** `.campo-con-acciones` es un `flex` con `flex-wrap` y `align-items: flex-end`; su primer hijo, el campo, tiene `flex: 0 1 420px` y `min-width: 240px`.
- **Indicador de progreso:** `.progreso` está oculto; con la clase `ocupado` en `.columna` se muestra y se ocultan `.mensaje`, `.tarjetas` y `.tabla-bloque`.
- **Bajo 600 px:** `.cuadricula-acciones` de una columna con botones de 72 de alto mínimo; `.fila-campos` de una columna con la flecha girada 90°; `.fila-botones` apilada con botones de ancho completo; `.campo-con-acciones` apilado; `.tarjeta` de ancho completo.

**Estructura Razor.** Las vistas viven en `ec/edu/monster/vista/`, en una carpeta plana (`RazorViewEngineOptions.ViewLocationFormats` = `/ec/edu/monster/vista/{0}.cshtml`): `_Layout.cshtml`, `_ViewImports.cshtml` (`@namespace ec.edu.monster.vista`, `@using ec.edu.monster.vista` y `@addTagHelper *, <Proyecto>`), `_ViewStart.cshtml` y una vista por pantalla. El esqueleto de página (disposición dividida o de aplicación, `<body class="tema-oscuro">`, hoja de estilos, imagen) vive en `_Layout.cshtml`; cada pantalla solo aporta su contenido. Cada componente de §5 es un *tag helper* `ui-*` en `ec/edu/monster/vista/componentes/`; las vistas solo usan `ui-*` (sin `class` ni `style`). Cada controlador entrega a su vista un modelo tipado con el texto ya listo para mostrar; la vista no conoce `dto` ni `ws`. Componentes:

| Etiqueta | Componente (§) | Atributos principales |
|---|---|---|
| `_Layout` | Estructura de página, disposición dividida o de aplicación, fondo (§5.10, §7.0) | `ViewData["Titulo"]` (obligatorio), `ViewData["Modo"]` (`acceso`, por defecto: dividida; `aplicacion`: ancho completo con imagen de fondo), `ViewData["Ancho"]` (`formulario`, `amplio`, `completo`). Incluye `estilos.css` y `app.js` con `asp-append-version` |
| `<ui-formulario accion>` | Formulario POST + indicador de progreso (§5.8) | `accion` (ruta de la aplicación, p. ej. `/login`), `ocupado` (texto del indicador), `clase` (por defecto `formulario`). Incluye el indicador de progreso y el token anti-falsificación |
| `<ui-encabezado>` | Encabezado de pantalla (§5.1) | `aplicacion="true"` en el acceso; `plano="true"`: texto grande en negrita sin panel (el saludo del menú) |
| `<ui-campo id>` | Etiqueta + campo de texto (§5.3, §5.4) | `nombre` (por defecto, el `id`), `etiqueta`, `tipo`, `valor`, `placeholder`, `autocomplete` (por defecto `off`), `inputmode` (por defecto `text`), `invalido`, `autofocus`, `centrado` |
| `<ui-selector id opciones>` | Etiqueta + selector (§5.5) | `nombre`, `etiqueta`, `opciones` (colección), `seleccionada`, `invalido` |
| `<ui-mensaje mensaje>` | Panel de mensaje o resultado (§5.2); no se dibuja vacío | `tipo`: `error` (defecto), `exito`, `resultado` |
| `<ui-boton>` | Botón primario / secundario (§5.6, §5.7) | `tipo`, `href` (ruta de la aplicación; sin él envía el formulario), `ancho`, `centrado`, `salida`, `cancelar`, `deshabilitado` |
| `<ui-lista-acciones elementos destino parametro>` | Lista de acciones (§5.9) con datos del servidor | `destino` (ruta de la aplicación); un botón primario por elemento |
| `<ui-cerrar-sesion/>` | Botón secundario de salida separado 32 (§5.7) | `POST /logout` con token anti-falsificación |
| `<ui-cuadricula-acciones>` | Cuadrícula de acciones (§5.11) | un `<ui-boton>` por acción; 2 columnas, 1 bajo 600 px |
| `<ui-fila-campos>` | Dos campos en horizontal con una flecha entre ellos | cuerpo: dos `<ui-campo>`; la flecha la dibuja el CSS; bajo 600 px se apilan y la flecha apunta hacia abajo |
| `<ui-tarjeta>` | Tarjeta de resultado (§5.12) | `titulo`, `suma`, `monto`, `variacion`, `pie` |
| `<ui-tarjetas>` | Fila de tarjetas de resultado | cuerpo: `<ui-tarjeta>`; se ocultan mientras hay una petición en curso |
| `<ui-tabla>` | Tabla (§5.13) | `columnas`, `al-derecha`, `filas`, `titulo`, `cantidad` |
| `<ui-fila-botones>` | Fila de botones: la acción principal y Cancelar, centrados, separados 16; apilados bajo 600 px | cuerpo: `<ui-boton>`; el de Cancelar lleva `cancelar="true"` (la tecla Esc lo activa) |
| `<ui-campo-con-acciones>` | Un campo con sus botones a la derecha | cuerpo: un `<ui-campo>` y un `<ui-fila-botones>` |
| `<ui-progreso texto>` | Indicador de progreso con su texto (§5.8); lo incluye `<ui-formulario>` | `texto` (`Procesando…`, `Consultando…`, `Iniciando sesión…`) |

Todo texto dinámico se escribe con el escape predeterminado de Razor (`@Modelo.Mensaje`); **no** usar `@Html.Raw` con datos del servidor. El JavaScript va en un único `wwwroot/js/app.js`:
- Al enviar un `form[data-ocupable]` o pulsar un `a.boton`, marca la `.columna` con `ocupado`, deshabilita sus controles y pone `aria-disabled` en los enlaces; el deshabilitado se aplica después del envío.
- Al volver con «Atrás» (`pageshow` persistido) libera la `.columna`.
- `Esc` activa el enlace `a[data-cancelar]`, salvo que la pantalla esté ocupada.

**Codificación de salida:** `WebEncoderOptions.TextEncoderSettings` con `BasicLatin`, `Latin1Supplement`, `GeneralPunctuation` y `Arrows`: las tildes, «ñ», «…», «·» y «→» salen sin entidades; `<`, `>`, `&`, las comillas y «+» siguen escapándose.

**Recursos estáticos:** `MapStaticAssets`; `_Layout.cshtml` referencia `~/css/estilos.css` y `~/js/app.js` con `asp-append-version`; la imagen, desde el CSS (`../img/fondo_app.png`). Todo `POST` exige el token anti-falsificación (`AutoValidateAntiforgeryTokenAttribute`).

### 7.3 Escritorio (WPF)
No requiere bibliotecas de terceros: el aspecto sale de **estilos y plantillas XAML propios**, definidos una vez. Los colores y medidas viven en `estilos/tema.xaml` (un `ResourceDictionary` con pinceles y números) y los estilos de controles en `estilos/controles.xaml`; ambos se cargan en `App.xaml` (`Application.Resources` → `ResourceDictionary.MergedDictionaries`). Las vistas **solo referencian recursos** (`{StaticResource …}`); no colorean ni dimensionan componentes sueltos.

```xml
<!-- tema.xaml (presentación oscura) -->
<Color x:Key="ColorFondo">#1A1426</Color>
<SolidColorBrush x:Key="Fondo" Color="{StaticResource ColorFondo}" />
<SolidColorBrush x:Key="Superficie" Color="#271D3A" />
<SolidColorBrush x:Key="PanelSobreImagen" Color="#99000000" />
<SolidColorBrush x:Key="Texto" Color="#F5F3FF" />
<SolidColorBrush x:Key="TextoSecundario" Color="#B4A9CE" />
<SolidColorBrush x:Key="Borde" Color="#3B2D57" />
<SolidColorBrush x:Key="Acento" Color="#A78BFA" />
<SolidColorBrush x:Key="Primario" Color="#6D28D9" />
<SolidColorBrush x:Key="PrimarioPresionado" Color="#5B21B6" />
<SolidColorBrush x:Key="Secundario" Color="#4B4563" />
<SolidColorBrush x:Key="SecundarioPresionado" Color="#3A3550" />
<SolidColorBrush x:Key="SobreAccion" Color="#FFFFFF" />
<SolidColorBrush x:Key="Error" Color="#F87171" />
<SolidColorBrush x:Key="Exito" Color="#4ADE80" />
<SolidColorBrush x:Key="CampoFondo" Color="#FFFFFF" />
<SolidColorBrush x:Key="CampoTexto" Color="#1E1033" />
<SolidColorBrush x:Key="CampoPlaceholder" Color="#6B6280" />
<!-- tarjeta de resultado (§2.5) -->
<SolidColorBrush x:Key="ExitoClaro" Color="#15803D" />
<SolidColorBrush x:Key="ErrorClaro" Color="#B91C1C" />
<SolidColorBrush x:Key="DivisorTarjeta" Color="#D9D2EA" />
<SolidColorBrush x:Key="ContornoTarjeta" Color="#806D28D9" />
<CornerRadius x:Key="Radio">8</CornerRadius>
<!-- constantes únicas -->
<system:Double x:Key="OpacidadImagen">0.4</system:Double>
<GridLength x:Key="MargenImagen">0.2*</GridLength>
<GridLength x:Key="CentroImagen">0.6*</GridLength>
<system:Double x:Key="AnchoTarjeta">260</system:Double>
<system:Double x:Key="AltoBotonCuadricula">96</system:Double>
<GridLength x:Key="AnchoPanelFormulario">560</GridLength>
<GridLength x:Key="BarraEstado">4</GridLength>
<Thickness x:Key="PaddingCampo">12,6</Thickness>

<!-- controles.xaml: botón primario con plantilla propia (los estilos por defecto de WPF no admiten radio) -->
<Style x:Key="BotonPrimario" TargetType="Button">
    <Setter Property="Foreground" Value="{StaticResource SobreAccion}" />
    <Setter Property="Background" Value="{StaticResource Primario}" />
    <Setter Property="Tag" Value="{StaticResource PrimarioPresionado}" />   <!-- pincel de hover y de presionado -->
    <Setter Property="FontSize" Value="15" />
    <Setter Property="FontWeight" Value="Bold" />
    <Setter Property="MinHeight" Value="36" />
    <Setter Property="Padding" Value="20,8" />
    <Setter Property="Cursor" Value="Hand" />
    <Setter Property="FocusVisualStyle" Value="{x:Null}" />
    <Setter Property="Template">
        <Setter.Value>
            <ControlTemplate TargetType="Button">
                <Grid>
                    <Border x:Name="Raiz" Background="{TemplateBinding Background}" CornerRadius="{StaticResource Radio}"
                            Padding="{TemplateBinding Padding}">
                        <ContentPresenter HorizontalAlignment="Center" VerticalAlignment="Center">
                            <ContentPresenter.Resources>
                                <Style TargetType="TextBlock">
                                    <Setter Property="TextWrapping" Value="Wrap" />
                                    <Setter Property="TextAlignment" Value="Center" />
                                </Style>
                            </ContentPresenter.Resources>
                        </ContentPresenter>
                    </Border>
                    <Border x:Name="Foco" CornerRadius="{StaticResource Radio}" BorderThickness="2"
                            BorderBrush="{StaticResource Acento}" IsHitTestVisible="False" Visibility="Collapsed" />
                </Grid>
                <ControlTemplate.Triggers>
                    <Trigger Property="IsMouseOver" Value="True">
                        <Setter TargetName="Raiz" Property="Background"
                                Value="{Binding Tag, RelativeSource={RelativeSource TemplatedParent}}" />
                    </Trigger>
                    <Trigger Property="IsPressed" Value="True">
                        <Setter TargetName="Raiz" Property="Background"
                                Value="{Binding Tag, RelativeSource={RelativeSource TemplatedParent}}" />
                    </Trigger>
                    <Trigger Property="IsKeyboardFocused" Value="True">
                        <Setter TargetName="Foco" Property="Visibility" Value="Visible" />
                    </Trigger>
                    <Trigger Property="IsEnabled" Value="False"><Setter TargetName="Raiz" Property="Opacity" Value="0.6" /></Trigger>
                </ControlTemplate.Triggers>
            </ControlTemplate>
        </Setter.Value>
    </Setter>
</Style>
<Style x:Key="BotonSecundario" TargetType="Button" BasedOn="{StaticResource BotonPrimario}">
    <Setter Property="Background" Value="{StaticResource Secundario}" />
    <Setter Property="Tag" Value="{StaticResource SecundarioPresionado}" />
</Style>
<Style x:Key="BotonCuadricula" TargetType="Button" BasedOn="{StaticResource BotonPrimario}">
    <Setter Property="Height" Value="{StaticResource AltoBotonCuadricula}" />
    <Setter Property="Margin" Value="8" />
</Style>
```
(`xmlns:system="clr-namespace:System;assembly=System.Runtime"` en el diccionario.)

- **Nombres de recursos únicos:** los pinceles de `tema.xaml` llevan el nombre del token (`CampoTexto`, `Primario`) y los estilos de `controles.xaml` llevan el nombre del control (`CajaTexto`, `CajaContrasenia`, `Selector`, `BotonPrimario`). Si un pincel y un estilo comparten clave, WPF usa uno por el otro y la aplicación se cae al cargar la vista (`"System.Windows.Style" no es un valor válido para la propiedad "Foreground"`), sin ningún error de compilación.
- **Tipos de los recursos:** lo que alimenta `ColumnDefinition.Width` o `RowDefinition.Height` es `GridLength` (`AnchoPanelFormulario`, `MargenImagen`, `CentroImagen`, `BarraEstado`); las medidas de largo son `System.Double`; los rellenos, `Thickness` (`PaddingCampo` = `12,6`).
- **Sufijo `Estilo`:** un estilo cuyo nombre coincide con el de un pincel lleva el sufijo `Estilo` (`PanelSobreImagenEstilo`, junto al pincel `PanelSobreImagen`).
- **Campos y selector:** `TextBox`, `PasswordBox` y `ComboBox` con plantilla propia: un `Border` con radio 8 y `campo-fondo`, **sin `Padding`**, que contiene el `ScrollViewer` `PART_ContentHost`. El `Padding` (`PaddingCampo`, `12,6`) y el alto mínimo 36 se fijan en el estilo del control. `ComboBox`: flecha `#424242` (§5.5). Foco y error: un `Border` superpuesto (`Anillo`, `IsHitTestVisible="False"`, grosor 0) que pasa a grosor 2 en `Acento` (foco) o en `Error` (error); el error se activa con la propiedad adjunta `AyudaCampo.Invalido`.
- **Ventana:** se abre en **1100 × 720** y el mínimo es **900 × 640** (`MinWidth`/`MinHeight`). Muestra una pantalla a la vez con `Mostrar(pantalla, vista)`, que alterna la visibilidad de dos `Grid` (acceso y aplicación); cada vista recibe su controlador como `DataContext` en el constructor. Dos modos (§7.0):
  - **Acceso (dividido):** un `Grid` de dos columnas, `Width="560"` y `Width="*"`. Izquierda: la pantalla en una columna de **420** centrada. Derecha: la imagen **completa y centrada** a opacidad plena (§6), dentro del `Grid` de 3 × 3 con `MargenImagen` / `CentroImagen` si es horizontal, con `Stretch="Uniform"` y `Background="{StaticResource Fondo}"`.
  - **Aplicación (ancho completo):** un `Grid` con la imagen detrás, **completa, centrada y con `Opacity="{StaticResource OpacidadImagen}"`** (`Stretch="Uniform"`), y encima la pantalla centrada con el ancho de §7.0 (480, 640 o completo). Al cambiar el tamaño de la ventana la imagen se recalcula sola; no se estira ni se recorta. Los paneles usan `PanelSobreImagen`.
  - **Desplazamiento y centrado:** cada modo coloca la pantalla en un `ScrollViewer` (`VerticalScrollBarVisibility="Auto"`, horizontal `Disabled`) con `Padding="{StaticResource MargenPantalla}"` y, dentro, un `Grid` **sin `Margin`** con `MinHeight` enlazado a `ViewportHeight` del `ScrollViewer`, que contiene un `ContentControl` centrado en vertical. Ancho: `Width` 420 en el acceso; en las demás, `MaxWidth` (480, 640 o infinito) con `HorizontalAlignment="Stretch"`.
  - **Pantalla de tabla:** el `ContentControl` usa `VerticalAlignment="Stretch"` y el `ScrollViewer`, `VerticalScrollBarVisibility="Disabled"`; la pantalla es un `Grid` de dos filas: `Auto` (encabezado, formulario y zona de estado) y `*` (resultado).
- **Cuadrícula de acciones (§5.11):** `UniformGrid Columns="2"` con cada botón a `Height="{StaticResource AltoBotonCuadricula}"` y `Margin="8"`, y el contenedor con `Margin="-8,16,-8,-8"` (`UniformGrid` no tiene espaciado propio), de modo que la separación entre botones sea 16 y no se superpongan. Estilo `BotonCuadricula` (basado en `BotonPrimario`: `Height` 96, `Margin` 8). El botón secundario de salida va debajo, a ancho completo, con `Margin` superior 32.
- **Tarjeta de resultado (§5.12):** un `Border` con `Width="{StaticResource AnchoTarjeta}"`, `CornerRadius="{StaticResource Radio}"`, `BorderBrush="{StaticResource ContornoTarjeta}"`, `BorderThickness="1.5"`, `Background="{StaticResource CampoFondo}"` y `Padding="16"`, que contiene un `StackPanel` con el título (`FontWeight="Bold"`, `CampoPlaceholder`, centrado), el monto (24 bold, `ExitoClaro` / `ErrorClaro`), la variación, un `Border` de alto 1 en `DivisorTarjeta` y el pie alineado a la derecha. Varias tarjetas van en un `ItemsControl` con `StackPanel Orientation="Horizontal"` centrado y `Margin` entre ellas.
- **Tabla (§5.13): un `ItemsControl` propio**, sin `DataGrid` ni `ListView`, porque éstos traen selección, cuadrícula y encabezados con su propio aspecto que habría que desactivar uno a uno. Estructura:
  - Un `Border` contenedor (`PanelSobreImagen` o `Superficie`, `CornerRadius` 8, `Padding` 0, `VerticalAlignment="Top"`) con `Grid.IsSharedSizeScope="True"`, que contiene un `Grid` de dos filas: `Auto` (encabezado) y `*` (filas).
  - **Encabezado:** un `Border` de **36 de alto** con una línea `Borde` de 1 debajo; dentro, un `Grid` con una columna `Auto` por dato (`SharedSizeGroup="C0"`, `"C1"`…) y una última columna `*` vacía. Queda **fuera** del `ScrollViewer`, así permanece fijo.
  - **Filas:** un `ScrollViewer` (`VerticalScrollBarVisibility="Auto"`, horizontal deshabilitado) con el `ItemsControl`, cuyo `ItemTemplate` es un `Border` de **36 de alto** con una línea `Borde` de 1 abajo y un `Grid` con las mismas columnas `SharedSizeGroup` y la columna `*` final.
  - **Celdas:** `TextBlock` con `Margin="12,0"` y `VerticalAlignment="Center"`; las de texto, a la izquierda; las numéricas (importes, números), con `HorizontalAlignment="Stretch"` y `TextAlignment="Right"`.
  - **Columna opcional:** el `Visibility` del encabezado y de las celdas se enlaza a un booleano del controlador (en las celdas, con `RelativeSource AncestorType=UserControl`).
  - No hay selección ni cuadrícula vertical por construcción: un `ItemsControl` no selecciona.
- **Zona de estado (§5.14):** un `UserControl` (`ZonaEstado`) bajo los botones, con la propiedad `Fondo` (`Superficie` en el acceso, `PanelSobreImagen` en el resto) y tres contenidos excluyentes que `DataTrigger` muestra según la propiedad `Estado` del controlador (enum `Ninguno`, `Ocupado`, `Error`, `Exito`). Error y éxito: un `Border` de radio 8 con `ClipToBounds`, una columna izquierda de `BarraEstado` (4) en `Error` / `Exito` y el texto con `Margin` 12. El `Grid` raíz de cada pantalla enlaza `IsEnabled` a `NoOcupado` del controlador, de modo que mientras hay una llamada en curso toda la pantalla, incluido `Cancelar`, queda deshabilitada.
- **Resultado:** una línea de texto plano, centrada, altura mínima 72 (§5.2). Los mensajes de error pueden ser largos: `TextBlock` con `TextWrapping="Wrap"` y ancho fijo.
- **Indicador de progreso:** `ProgressBar` lineal indeterminado de 160 × 6 en `Acento` (`IndicadorProgreso`), junto al texto de ocupado, dentro de la zona de estado.
- **Presentación clara:** cambiar los valores de `tema.xaml` por los de §2.3; los estilos no cambian (la imagen no se usa, §6).
- Las operaciones lentas (red) no se ejecutan en el hilo de la interfaz: comandos asíncronos (`async`/`await`; `AsyncRelayCommand` de `CommunityToolkit.Mvvm` es opcional). Con `await` la continuación vuelve sola al hilo UI.
- **Recursos:** la imagen es un **PNG** en la carpeta `estaticos/` con *Build Action* `Resource`, referenciado con la URI relativa `/estaticos/fondo_app.png`. Una imagen horizontal con transparencia (`fondo_app.png`, 331 × 211) se usa tal cual sobre `Fondo`, compacta al 60 % (§6).
- **Plataforma:** solo Windows (`net10.0-windows`). Escalado de pantalla: WPF usa DIP, por lo que las medidas de §4 se mantienen a cualquier DPI.

---

## 8. Reglas de textos de interfaz

Los textos concretos (títulos, etiquetas, formato del resultado) los define cada proyecto. Reglas comunes:

- Un solo idioma por cliente, con las tildes y caracteres propios; los **mismos textos** en todas las plataformas del proyecto.
- Botones en infinitivo o imperativo breve («Iniciar sesión», «Cerrar sesión», «Volver», «Reintentar»).
- Un título por pantalla, en el encabezado (§5.1). Las pantallas de lista de acciones **no** llevan texto explicativo bajo el título: la acción es evidente.
- Un *placeholder* describe el dato esperado; la etiqueta (§5.4) nombra el campo. No se repiten.
- Los mensajes de error son los que envía el sistema, sin código técnico y sin modificarlos; los propios del cliente (campo vacío, error inesperado) también respetan el idioma y las tildes.

---

## 9. Lista de verificación

Antes de dar por terminado un cliente comprobar:
- [ ] Todos los colores salen de **tokens** (sin hex sueltos en vistas XAML, páginas MAUI ni vistas Razor).
- [ ] Botón primario = `primario` + blanco; botón secundario **sólido** = `secundario` + blanco.
- [ ] Un solo radio (**8**) y la altura mínima de control de la plataforma.
- [ ] Texto principal y de botones con contraste **≥ 4,5:1** en la presentación elegida.
- [ ] Una sola acción principal por pantalla; las acciones de salida son botones secundarios.
- [ ] Mensajes sin códigos técnicos; cargas con progreso y controles deshabilitados.
- [ ] Sin sombras; sin más de dos pesos de fuente por pantalla (*Medium* solo en el resultado; el título de la tarjeta, en Bold).
- [ ] Imagen decorativa en **PNG**, sin deformar y con `fondo` de respaldo (`#1A1426`). Si es opaca y vertical, sus bordes son del mismo color; si es horizontal, se muestra compacta (60 %).
- [ ] Columna única (móvil): imagen completa y centrada (`AspectFit`, nunca recortada); cabecera a opacidad plena en el acceso y fondo fijo al 40 % con paneles `panel-sobre-imagen` en el resto.
- [ ] Escritorio y web: disposición dividida **solo en el acceso** (columna de contenido centrada, imagen en la ventana y mínimo de ventana definido); en el resto, imagen de fondo centrada (`contain`/`Uniform`) con opacidad 40 %, reescalable con la ventana, y paneles `panel-sobre-imagen`.
- [ ] Zona de estado única bajo los botones; con una llamada en curso, todos los controles (incluido `Cancelar`) deshabilitados.
- [ ] Tarjetas de resultado: signo y color (`exito-claro` / `error-claro`) juntos, y textos con contraste ≥ 4,5:1 sobre blanco. En el móvil, una lista de registros son tarjetas compactas, no una tabla.
- [ ] Tabla: encabezado fijo, filas de 36, columnas numéricas a la derecha, sin selección y **sin desplazamiento horizontal de la página**.
- [ ] Escritorio: campos con `Padding` solo en el control (no en el `Border` de la plantilla); recursos de filas y columnas de tipo `GridLength`; margen de pantalla como `Padding` del `ScrollViewer`; pantalla de tabla arriba, sin desplazamiento externo.
- [ ] Web: las vistas solo usan `ui-*` (sin `class` ni `style`); todo formulario `POST` lleva el token (`ui-formulario`, `ui-cerrar-sesion`); la pantalla de tabla usa `ancho` `completo`; la página nunca se desplaza en horizontal.
- [ ] Sin restos de la plantilla de ejemplo: ni paleta de MAUI, ni Bootstrap en la web, ni fuente `OpenSans`.
- [ ] Texto dinámico escapado en las vistas Razor (sin `Html.Raw` con datos del servidor).

---

## 10. Equivalencias con `CLIENT_STYLES_JAVA.md`

| Elemento | Java (azul) | .NET (morado) |
|---|---|---|
| `acento` | `#0EA5E9` | `#A78BFA` |
| `primario` / `primario-presionado` | `#0369A1` / `#075985` | `#6D28D9` / `#5B21B6` |
| `secundario` / `secundario-presionado` | `#475569` / `#334155` | `#4B4563` / `#3A3550` |
| `fondo` / `superficie` (oscura) | `#111E2F` / `#1B2B42` | `#1A1426` / `#271D3A` |
| `fondo` (clara) | `#F4F7FB` | `#F7F5FC` |
| `divisor-tarjeta` / `contorno-tarjeta` | `#CBD5E1` / `primario` al 50 % (`#0369A1`) | `#D9D2EA` / `primario` al 50 % (`#6D28D9`) |
| `exito-claro` / `error-claro` | `#15803D` / `#B91C1C` | `#15803D` / `#B91C1C` (iguales) |
| Título de la tarjeta de resultado | *Semibold* | **Bold** (en MAUI no existe *Semibold* sin fuente propia) |
| Imagen decorativa | `imagen-decorativa.png` / `fondo_app.png` | `fondo_app.png` en el escritorio y en la web (**PNG**) |
| Imagen compacta (60 %) | constante `Tema.ESCALA_IMAGEN` / variable CSS `--margen-imagen` | recursos `MargenImagen` + `CentroImagen` (`Grid` 3 × 3) / variable CSS `--margen-imagen` |
| Escritorio | Swing + FlatLaf | WPF (XAML, estilos y plantillas propios) |
| Tabla del escritorio | `TablaDatos`, componente propio sobre Swing | `ItemsControl` propio (encabezado fuera del `ScrollViewer`, columnas con `SharedSizeGroup`) |
| Cuadrícula del escritorio | `GridLayout` con separación de 16 (`ESPACIO_BLOQUE`) | `UniformGrid Columns="2"` con márgenes de 8 |
| Web | JSP + tag files (`WEB-INF/tags`) | MVC + vistas Razor (`_Layout`, *tag helpers* `ui-*` en `vista/componentes`) |
| Componentes de la web | `boton.tag`, `campo.tag`, `tabla.tag`… | `<ui-boton>`, `<ui-campo>`, `<ui-tabla>`… |
| Flecha de `ui-fila-campos` | SVG en línea en el tag file | `.fila-campos::before` con `mask` SVG (CSS) |
| `ui-campo-con-acciones` | fragmentos `campo` y `acciones` | cuerpo: `<ui-campo>` y `<ui-fila-botones>` |
| Rutas de `ui-formulario` y `ui-boton` | relativas al contexto (`/login`) | relativas a la aplicación (`/login`), con la ruta base |
| Móvil | Android XML (`colors.xml`, `styles.xml`) | MAUI XAML (`Colors.xaml`, `Styles.xaml`) |
| Imagen del móvil | `fitCenter` | `Aspect="AspectFit"` |
| Lista de registros del móvil | tarjeta compacta por registro | tarjeta compacta por registro (`BindableLayout`) |
| Medidas, tipografía por rol, componentes (§5) | — | **idénticos**, salvo el peso del título de la tarjeta |
