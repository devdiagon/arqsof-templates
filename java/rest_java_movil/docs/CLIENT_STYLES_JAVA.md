# Estilos compartidos de los clientes gráficos (escritorio, web y móvil)

Define **un solo lenguaje visual** para clientes con interfaz gráfica en **escritorio (Swing), web y móvil (Android)**.
Es **independiente del dominio** (no menciona qué hace la aplicación), del protocolo de comunicación y del lenguaje: sirve para cualquier proyecto cuyas pantallas combinen **acceso con credenciales, listas de acciones, formularios y resultados**.
No aplica a clientes de consola.

Aquí solo se define **cómo se ve**. El flujo, los textos concretos, las imágenes y las reglas de negocio de cada proyecto se documentan aparte (en `dominio/ESPECIFICACION_CLIENTES.md` y en el `docs/PANTALLAS.md` de cada cliente).

> Verificación: los **contrastes** de §2 se calcularon con la fórmula WCAG 2.x.
>
> Revisión 2026-10-26 (primeros clientes gráficos construidos): el **escritorio y la web** conservan la disposición dividida solo en la pantalla de acceso; en el resto de la aplicación usan el ancho completo y la imagen pasa a ser el fondo de la ventana, centrada y con opacidad (§6, §7.0, §7.2, §7.3). Se agregan los componentes **cuadrícula de acciones**, **tarjeta de resultado** y **tabla** (§5.11 a §5.13).
>
> Revisión 2026-10-27 (cliente móvil construido): el **móvil** usa la imagen con `fitCenter` (completa y centrada, nunca recortada): en el acceso como cabecera y en el resto como fondo fijo con opacidad (§6); una **imagen horizontal** se muestra compacta (60 % de su contenedor) en escritorio y web (§6); se agregan la **zona de estado** (§5.14) y las particularidades de la columna única (§7.1).
>
> Revisión 2026-10-29 (correcciones de coherencia, sin cambios de diseño): las cifras de contraste de la tarjeta (§5.12) se dan sobre blanco; la regla de pesos (§3) admite *Medium* y *Semibold* donde ya se usaban; los fragmentos de CSS (§7.2) muestran la imagen compacta con `--margen-imagen`; `<ui:pantalla>` queda en una sola fila; y la lista de verificación (§9) deja de pedir la imagen recortada en el móvil.

---

## 1. Principios

1. **Una paleta, dos presentaciones.** Los tokens existen en versión **oscura** y **clara**. El cliente se publica con **una sola** de ellas
   por defecto; **no hay selector de tema** en la interfaz. Elegir la clara es cambiar la tabla de valores, no el diseño.
2. **Se programa con nombres, no con hex.** Cada color, tamaño y radio tiene un nombre (`fondo`, `primario`…). El código de cada cliente
   define esos nombres una vez (recursos de Android, variables CSS, constantes de Swing) y no repite valores sueltos.
3. **Mismo flujo y mismos textos** en todos los clientes gráficos de un proyecto (los define el proyecto, no este documento).
4. **Legible primero.** Todo texto cumple contraste **≥ 4,5:1** (WCAG AA). El color nunca es el único indicador: el botón se reconoce por su etiqueta.
5. **Sin acoplar el estilo al dominio.** Los componentes (§5) se describen por su función (encabezado, campo, botón, lista de acciones…), nunca por lo que hace la aplicación.

---

## 2. Paleta de colores

### 2.1 Tokens de marca y de acción (iguales en ambas presentaciones)

| Token | Hex | Uso |
|---|---|---|
| `acento` | `#0EA5E9` | Marca: foco, indicadores de carga, iconos y enlaces **sobre fondo oscuro**, resaltes. **No** se usa como texto sobre fondo claro (2,6:1). |
| `primario` | `#0369A1` | Relleno del **botón primario**. Texto blanco encima: **5,9:1**. |
| `primario-presionado` | `#075985` | Botón primario al pulsarlo / `hover` (7,6:1 con blanco). |
| `secundario` | `#475569` | Relleno del **botón secundario** (acciones de salida o de regreso). Texto blanco: **7,6:1**. Funciona sobre fondo oscuro y claro. |
| `secundario-presionado` | `#334155` | Botón secundario al pulsarlo / `hover` (10,4:1 con blanco). |
| `sobre-accion` | `#FFFFFF` | Texto e iconos dentro de botones `primario` y `secundario`. |

### 2.2 Presentación **oscura** (por defecto actual)

| Token | Hex | Uso | Contraste |
|---|---|---|---|
| `fondo` | `#111E2F` | Fondo de pantalla/ventana | — |
| `superficie` | `#1B2B42` | Paneles y tarjetas **sin** imagen de fondo | texto blanco 14,3:1 |
| `panel-sobre-imagen` | `rgba(0,0,0,0.60)` (`#99000000` en Android) | Paneles **cuando hay imagen de fondo bajo ellos** (§6) | texto blanco ≥ 10:1 |
| `texto` | `#F1F5F9` | Texto principal | 15,3:1 sobre `fondo` |
| `texto-secundario` | `#94A3B8` | Ayudas, etiquetas secundarias | 6,5:1 sobre `fondo` |
| `borde` | `#2D3F5A` | Separadores y contorno de paneles | decorativo |
| `error` | `#F87171` | Texto o indicador de error | 6,1:1 sobre `fondo` |
| `exito` | `#4ADE80` | Texto o indicador de éxito | 9,6:1 sobre `fondo` |

### 2.3 Presentación **clara**

| Token | Hex | Uso | Contraste |
|---|---|---|---|
| `fondo` | `#F4F7FB` | Fondo de pantalla/ventana | — |
| `superficie` | `#FFFFFF` | Paneles y tarjetas | texto `#0F172A` 17,5:1 |
| `panel-sobre-imagen` | — | **No se usa imagen de fondo** en la presentación clara (ver §6) | — |
| `texto` | `#0F172A` | Texto principal | 16,6:1 sobre `fondo` |
| `texto-secundario` | `#475569` | Ayudas, etiquetas secundarias | 7,0:1 sobre `fondo` |
| `borde` | `#CBD5E1` | Separadores y contorno de paneles | decorativo |
| `enlace` | `#0369A1` | Enlaces y foco (en la oscura se usa `acento`) | 5,5:1 sobre `fondo` |
| `error` | `#B91C1C` | Texto o indicador de error | 6,0:1 sobre `fondo` |
| `exito` | `#15803D` | Texto o indicador de éxito | 4,7:1 sobre `fondo` |

### 2.4 Tokens de formulario (iguales en ambas presentaciones)

| Token | Hex | Uso | Contraste |
|---|---|---|---|
| `campo-fondo` | `#FFFFFF` | Fondo de campos de texto y selectores (blanco en ambas presentaciones) | — |
| `campo-texto` | `#0F172A` | Texto escrito | 17,9:1 |
| `campo-placeholder` | `#64748B` | Texto de ayuda dentro del campo | 4,8:1 |
| `campo-borde` | `#64748B` | Contorno del campo (**obligatorio en la presentación clara**, opcional en la oscura) | 4,4–4,8:1 |

Notas de contraste:
- `primario` sobre `fondo` oscuro da 2,8:1: el botón se reconoce por su **etiqueta blanca (5,9:1)** y su forma, no solo por el relleno.
- `acento` (`#0EA5E9`) es el color de marca; se conserva como acento, no como relleno de botón porque con texto blanco solo alcanza 2,8:1.

---

## 3. Tipografía

| Plataforma | Familia | Notas |
|---|---|---|
| **Móvil (Android)** | `Roboto` (fuente del sistema) | No se incrusta ninguna fuente. |
| **Web** | `"Inter", "Segoe UI", Roboto, system-ui, -apple-system, Arial, sans-serif` | `Inter` es opcional (Google Fonts); la pila siempre cae a fuentes del sistema. |
| **Escritorio (Swing)** | La del *look and feel* (FlatLaf → `Segoe UI` en Windows) | No forzar otra familia. |

Escala por rol (tamaño en **sp** en móvil, **px** en web y escritorio):

| Rol | Móvil | Web | Escritorio | Peso |
|---|---|---|---|---|
| Título de aplicación (pantalla de acceso) | 24 | 32 | 28 | Bold (700) |
| Título de pantalla | 22 | 28 | 24 | Bold (700) |
| Resultado | 18 | 22 | 18 | Medium (500) |
| Cuerpo y texto de campos | 16 | 16 | 14 | Regular (400) |
| Etiqueta de campo | 14 | 14 | 13 | Bold (700) |
| Mensajes y ayudas | 14 | 14 | 13 | Regular (400) |
| Botón | 18 | 16 | 15 | Bold (700) |

Reglas: máximo **dos pesos** por pantalla (regular y bold; *Medium* solo para el resultado y *Semibold* solo para el título de la tarjeta de resultado, §5.12); sin cursiva; sin mayúsculas forzadas; los textos llevan las tildes y caracteres propios del idioma.

---

## 4. Medidas

**Unidad base: 4.** Escala de espaciado: `4 · 8 · 12 · 16 · 24 · 32`.

| Token | Valor | Uso |
|---|---|---|
| `radio` | **8** (dp / px) | Esquinas de botones, campos, selectores y paneles. Un único radio en todo el cliente. |
| `margen-pantalla` | **24** en todas las plataformas | Margen lateral del contenido. |
| `ancho-columna` | móvil: todo el ancho menos márgenes; web y escritorio: **420** con disposición dividida (§7.0), **480** (web) en columna única | Ancho del contenido de una pantalla. |
| `espacio-bloque` | **16** | Separación entre campos y paneles consecutivos. |
| `espacio-seccion` | **24** (entre grupos) y **32** (antes de las acciones de salida) | |
| `altura-minima-control` | móvil **48**; web **44**; escritorio **36** | Campos, selectores y botones. En móvil es también el área táctil mínima. |
| `padding-panel` | **16** (título) y **12** (mensaje/resultado) | |
| `padding-boton` | móvil **12 × 24**; web **10 × 20**; escritorio **8 × 20** | Vertical × horizontal. |
| `ancho-botón` | **100 %** del contenido para acciones de lista y de salida; **ajustado al texto y centrado** para la acción principal de un formulario | |

Elevación: **ninguna** (diseño plano). No usar sombras; la jerarquía se logra con color y espacio.

---

## 5. Componentes

Cada componente se define por **tokens**, no por tecnología. Los nombres son los que se usan en los recursos de cada cliente (§7).

### 5.1 Encabezado de pantalla (panel de título)
Panel con un único texto. Fondo `superficie` (o `panel-sobre-imagen` si hay imagen debajo), `radio`, `padding-panel` 16, texto `texto` en **título de pantalla** (o **título de aplicación** en la pantalla de acceso, centrado).
Va **siempre arriba** y es el primer elemento de la pantalla; no lleva botones.

### 5.2 Panel de mensaje / resultado
Mismo fondo y radio que el encabezado, `padding-panel` 12, texto `texto`. Oculto hasta que haya contenido.
- **Resultado:** tamaño *Resultado*, centrado, altura mínima 72. Es una línea corta de texto plano. Cuando el resultado son varios datos con un importe (una operación que cambia un saldo), se usa la **tarjeta de resultado** (§5.12).
- **Mensaje de error o de validación:** tamaño *Mensajes y ayudas*, alineado a la izquierda. Se muestra **solo el mensaje**, nunca un código técnico interno.
- Recomendado: reforzar el estado con una barra de **4** a la izquierda en `error` / `exito` (no obligatorio; el texto debe seguir siendo comprensible sin el color).

### 5.3 Campo de texto
Fondo `campo-fondo`, texto `campo-texto`, *placeholder* `campo-placeholder`, `radio`, alto mínimo `altura-minima-control`, `padding` 12. Sin borde en la presentación oscura; con `campo-borde` de 1 en la clara.
Estados: **foco** → borde de 2 en `acento` (oscura) o `enlace` (clara); **deshabilitado** → opacidad 60 %; **error** → borde de 2 en `error`.
Los campos de contraseña ocultan el contenido.

### 5.4 Etiqueta de campo
Texto `texto` en *Etiqueta*, **encima** del campo, separada 6 del campo. Alineada con el margen izquierdo.

### 5.5 Selector (lista desplegable)
Igual que un campo de texto, con una **flecha hacia abajo** (24 × 24, color `#424242`) alineada a la derecha a 12 del borde, y el texto con `padding` derecho 40 para no pisarla.
La lista desplegable usa `campo-fondo` y `campo-texto`.

### 5.6 Botón primario
Relleno `primario`, texto `sobre-accion`, *Botón* bold, `radio`, alto mínimo `altura-minima-control`, `padding-boton`.
Estados: **presionado/hover** → `primario-presionado`; **deshabilitado** → opacidad 60 % y sin respuesta; **cargando** → deshabilitado + indicador de progreso (§5.8) junto al botón, no dentro.
Se usa para **la acción principal de la pantalla**. **Una acción principal por pantalla**; en una lista de acciones (§5.9) cada botón es primario porque cada uno es la acción.

### 5.7 Botón secundario
**Sólido** (no de contorno): relleno `secundario`, texto `sobre-accion`, mismas medidas que el primario. Estados: presionado/hover → `secundario-presionado`; deshabilitado → opacidad 60 %.
Se usa para **acciones de salida o de regreso** (cerrar sesión, volver, reintentar si no es la acción principal). Ancho 100 % del contenido, separado **32** del bloque anterior.

### 5.8 Indicador de progreso
Circular, indeterminado, color `acento` (oscura) o `primario` (clara), 36–48 de diámetro, centrado bajo la acción que lo originó. Mientras está visible, los controles de esa pantalla están **deshabilitados** (evita doble envío).

### 5.9 Lista de acciones
Una columna de botones primarios de ancho completo, separados 16, generados con datos que provee el sistema (nunca fijos en el cliente). Debajo, 32 de separación y el botón secundario de salida.

### 5.10 Fondo
`fondo` plano. Opcional: **imagen decorativa** (§6). El fondo no contiene información ni controles.

### 5.11 Cuadrícula de acciones (escritorio y web)
Variante de la lista de acciones (§5.9) para el ancho completo: los botones primarios se disponen en una **rejilla de 2 columnas**, de igual tamaño, separados `espacio-bloque` (16) en ambos sentidos y **sin superponerse**. Cada botón es **grande**: altura **96**, texto *Botón* centrado; el ancho es la mitad del contenido menos la mitad de la separación. Los datos siguen viniendo del sistema (nunca fijos en el cliente). El botón secundario de salida va **debajo**, ancho completo, separado 32. Si el ancho no alcanza para dos columnas (ventana mínima), la rejilla se mantiene y los botones se reducen en ancho; el texto largo se parte en dos líneas. En la web, bajo 600 px de ancho pasa a una sola columna.

### 5.12 Tarjeta de resultado
Resume **un cambio de un valor** (por ejemplo, el efecto de una operación sobre un saldo). Es plana (sin sombra), con el mismo `radio` (8), `padding` 16 y **ancho fijo de 260**, con un **contorno sutil de 1,5 en el color `primario`** (translúcido); su altura se ajusta al contenido. Se compone, de arriba abajo, de:

| Parte | Rol tipográfico (§3) | Color |
|---|---|---|
| **Título** (opcional), **centrado**: una etiqueta o el mensaje de éxito | Cuerpo con peso intermedio, casi negrita (Semibold) | `campo-placeholder` |
| **Monto** con signo y símbolo, p. ej. `+$250.00` o `-$250.00` | Título de pantalla (24 bold) | `exito` de la presentación **clara** si suma, `error` de la clara si resta |
| **Variación**: `{antes} → {después}` | Cuerpo | `campo-texto` |
| **Divisor**: línea de 1 en `borde` de la presentación clara | — | — |
| **Pie**: identificación del origen del cambio, alineado a la derecha | Mensajes y ayudas | `campo-placeholder` |

- Fondo `campo-fondo` (blanco) en **ambas** presentaciones: es una superficie de lectura, como un campo. Por eso sus textos usan los tokens de formulario y los `exito` y `error` **de la presentación clara**, que son los más suaves que alcanzan 4,5:1 sobre blanco (**5,0:1 y 6,5:1** sobre blanco; 4,7:1 y 6,0:1 sobre el `fondo` claro, §2.3). Un verde o rojo más pálido incumpliría el contraste.
- El signo (`+`, `-`) acompaña siempre al color: el color nunca es el único indicador.
- Varias tarjetas se disponen en fila, separadas `espacio-bloque`, centradas.
- Oculta hasta que hay contenido.

### 5.13 Tabla
Lista de registros con columnas. Fondo `panel-sobre-imagen` si hay imagen debajo, o `superficie`; `radio`; texto `texto` en *Cuerpo*. **Encabezado** de columnas en *Etiqueta* (13 bold) con una línea `borde` de 1 debajo; **filas** de **36** de alto separadas por una línea `borde` de 1; sin cuadrícula vertical, sin sombras, sin selección. Columnas de texto alineadas a la izquierda y las **numéricas a la derecha**. Si hay más filas de las que caben, la tabla se desplaza por dentro y el encabezado queda fijo.

---

### 5.14 Zona de estado
Un solo lugar, **bajo los botones** de la pantalla, que muestra de una en una: el **indicador de ocupado** con su texto (§5.8), un **error** o un **éxito**. El error y el éxito son un panel de mensaje (§5.2) con una **barra izquierda** de 4 del color `error` o `exito`; el texto es siempre el mensaje del sistema, nunca un código. Oculta si no hay nada que mostrar. Mientras hay una llamada en curso, todos los controles de la pantalla (incluido `Cancelar`) quedan deshabilitados.

## 6. Imagen decorativa (opcional)

- Solo en la **presentación oscura**. La imagen **nunca se deforma**.
- Si la imagen es **vertical** y sus **bordes tienen exactamente el color `fondo`**, puede mostrarse completa sobre `fondo` y fundirse sin costuras. Es la condición para usar el modo `contain` descrito abajo; si una imagen no la cumple, debe usarse con paneles `panel-sobre-imagen` o recortada.
- **Imagen horizontal (p. ej. un logotipo apaisado):** se muestra **completa y centrada** pero **compacta**: ocupa como máximo el **60 % del alto y del ancho de su contenedor** (un margen del 20 % por lado), en lugar de llenarlo, para que no domine el fondo. Vale para el acceso y para el fondo de la ventana; el tamaño del PNG no importa, la escala la fija el contenedor. **Escritorio:** constante única (p. ej. `Tema.ESCALA_IMAGEN` = 0,60) que el panel de la imagen aplica sobre el ajuste `contain`. **Web:** variable CSS `--margen-imagen: 20%`, usada como `inset` del `::before` de `.zona-imagen` (acceso) y de la capa fija de `.pagina--aplicacion` (resto), con `background-size: contain`. Si la imagen tiene **transparencia**, el color `fondo` se ve a través de ella y no se exige que sus bordes sean del color `fondo`. **El 60 % es un punto de partida:** se ajusta según la imagen del proyecto (más grande, más pequeña o 100 %) cambiando ese único valor.
- **Columna única (móvil):** la imagen se muestra **completa y centrada** (`fitCenter`, nunca `centerCrop`), sin recortar ni deformar; lo que sobra es `fondo`. **Acceso:** va como **cabecera** del formulario (≈ 160 dp de alto) a opacidad plena, en su propia zona, y los paneles usan `superficie`. **Resto de pantallas:** es el **fondo fijo** de la pantalla (fuera del contenedor que se desplaza), con **opacidad 40 %**, y los paneles usan `panel-sobre-imagen` para que el texto se lea sobre ella. En una imagen horizontal, un teléfono vertical deja bandas de `fondo` arriba y abajo.
- **Escritorio y web, fuera de la pantalla de acceso (§7.0):** la imagen es el **fondo de la ventana**: ocupa toda el área, **completa y centrada** (`contain`, nunca `cover`), sin deformarse; las franjas que sobran son `fondo`. Se dibuja con **opacidad 40 %** sobre `fondo` y se **reescala con la ventana** (al maximizar o redimensionar mantiene su proporción y sigue centrada). Como el contenido queda encima de la imagen, los paneles usan `panel-sobre-imagen`. La opacidad es una constante única y se puede ajustar. En la web es una capa fija (`position: fixed`) detrás del contenido, con `background-size: contain` y `opacity: .4`; bajo 900 px no se muestra (columna única sin imagen) y la presentación clara tampoco la usa.
- **Pantalla ancha con disposición dividida (el acceso, en escritorio y web, §7.0):** la imagen ocupa su propia zona, **completa y centrada** (`contain` / `background-size: contain`), sin recortar ni deformar; el resto de la zona es `fondo`. No se usa `cover`: al maximizar la ventana, una imagen vertical ampliada para cubrir una zona ancha se recorta y parece estirada. Como el contenido no queda sobre la imagen, los paneles usan `superficie`.
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
- Las medidas de componentes (§4 y §5) no cambian; solo cambia dónde se colocan.
- Si el ancho disponible es menor que el mínimo de la plataforma, la zona derecha puede quedar muy estrecha: en escritorio se fija un mínimo de ventana (§7.3); en la web se pasa a columna única sin imagen por debajo de ese ancho (§7.2).

### 7.1 Móvil (Android, vistas XML)
Particularidades de la **columna única**: todos los elementos se apilan a ancho completo con `margen-pantalla` (24) a cada lado; los botones de una acción van **apilados** (el principal arriba, `Cancelar` debajo, separados 16) y nunca en fila; los botones de un menú son primarios de **96 de alto**, separados 16 (la cuadrícula de §5.11 es del escritorio y la web); una tarjeta de resultado (§5.12) ocupa el ancho completo y varias se apilan; una lista de registros se dibuja como **una tarjeta compacta por registro** (no una tabla), apiladas y dentro del mismo desplazamiento de la pantalla. Se oculta el teclado al enviar un formulario para que la zona de estado (§5.14) quede visible.

Tokens en `res/values/colors.xml` y estilos en `res/values/styles.xml`; los layouts **solo referencian estilos** y nunca valores sueltos.

```xml
<!-- colors.xml (presentación oscura) -->
<color name="fondo">#111E2F</color>
<color name="panel_sobre_imagen">#99000000</color>
<color name="texto">#F1F5F9</color>
<color name="acento">#0EA5E9</color>
<color name="primario">#0369A1</color>
<color name="primario_presionado">#075985</color>
<color name="secundario">#475569</color>
<color name="secundario_presionado">#334155</color>
<color name="sobre_accion">#FFFFFF</color>

<!-- styles.xml -->
<style name="Boton.Primario" parent="Widget.Material3.Button">
    <item name="android:minHeight">48dp</item>
    <item name="android:textSize">18sp</item>
    <item name="android:textStyle">bold</item>
    <item name="android:textColor">@color/sobre_accion</item>
    <item name="backgroundTint">@color/primario</item>
    <item name="cornerRadius">8dp</item>
</style>
<style name="Boton.Secundario" parent="Boton.Primario">
    <item name="backgroundTint">@color/secundario</item>
</style>
```
Tema `Theme.Material3.Light.NoActionBar` con `colorPrimary = @color/primario`. Para la presentación clara se redefinen los mismos nombres en `res/values/colors.xml` con los valores de §2.3 (o en `values-notnight`/`values-night` si algún día se quisiera conmutar).
Reglas: `fitsSystemWindows` reemplaza el `padding` del mismo `View`, usar márgenes en los hijos; los `Spinner` usan un fondo propio (blanco, radio 8, flecha) y un ítem propio de 48 de alto.

### 7.2 Web (CSS)
Los tokens son variables CSS; el tema por defecto se fija con **una clase en `<body>`** (`tema-oscuro` o `tema-claro`), sin script ni selector en pantalla.

```css
:root { --radio: 8px; --primario: #0369A1; --primario-p: #075985; --secundario: #475569;
        --secundario-p: #334155; --acento: #0EA5E9; --sobre-accion: #FFFFFF;
        --campo-fondo: #FFFFFF; --campo-texto: #0F172A; --campo-borde: #64748B;
        --margen-imagen: 20%;                        /* imagen horizontal compacta (§6); 0 para una vertical */ }
.tema-oscuro { --fondo: #111E2F; --superficie: #1B2B42; --texto: #F1F5F9; --texto-sec: #94A3B8;
               --borde: #2D3F5A; --error: #F87171; --exito: #4ADE80; --foco: var(--acento); }
.tema-claro  { --fondo: #F4F7FB; --superficie: #FFFFFF; --texto: #0F172A; --texto-sec: #475569;
               --borde: #CBD5E1; --error: #B91C1C; --exito: #15803D; --foco: #0369A1; }
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
.zona-imagen::before { content: ""; position: absolute; inset: var(--margen-imagen);   /* imagen compacta (§6) */
                       background: url("imagen-decorativa.png") center / contain no-repeat; }
@media (max-width: 899px) {                   /* bajo el mínimo del escritorio: columna única, sin imagen */
  .pagina { grid-template-columns: 1fr; }
  .zona-imagen { display: none; }
}
```
`.contenedor { max-width: 480px }` queda solo para la variante de columna única (ancho < 900). La imagen usa `contain` (§6), nunca `cover`.

**Fuera del acceso** (§7.0): la página usa `ui:pantalla modo="aplicacion"`, sin zona derecha; el contenido va centrado y la imagen es una capa de fondo.

```css
.pagina--aplicacion { position: relative; min-height: 100vh; }
.pagina--aplicacion::before {                  /* imagen de fondo: fija, completa, centrada, con opacidad */
  content: ""; position: fixed; inset: var(--margen-imagen); z-index: -1; opacity: .4;
  background: url("imagen-decorativa.png") center / contain no-repeat; }
.pagina--aplicacion .contenido { width: 100%; max-width: 480px; margin: 0 auto; padding: 24px; }
.contenido--amplio { max-width: 640px !important; }      /* menú, transferencia */
.contenido--completo { max-width: none !important; }     /* tabla */
.pagina--aplicacion .encabezado, .pagina--aplicacion .mensaje, .pagina--aplicacion .tabla { background: rgba(0,0,0,.60); } /* panel-sobre-imagen */
@media (max-width: 899px) { .pagina--aplicacion::before { display: none; } }
.tema-claro .pagina--aplicacion::before { display: none; }
```
Tarjeta, cuadrícula y tabla (§5.11 a §5.13): tokens propios `--exito-claro: #15803D`, `--error-claro: #B91C1C` y `--divisor-tarjeta: #CBD5E1`; el título de la tarjeta usa `font-weight: 600`. La tabla va dentro de un contenedor con `overflow-x: auto`, de modo que **la página nunca se desplaza en horizontal**.

Cada componente de §5 es un tag file (de `WEB-INF/tags`); en un JSP se declara una vez `<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>`.

| Etiqueta | Componente (§) | Atributos principales |
|---|---|---|
| `<ui:pantalla titulo modo ancho>` | Estructura de página, disposición dividida o de aplicación, fondo (§5.10, §7.0) | `titulo` (obligatorio), `modo` (`acceso`, por defecto: dividida; `aplicacion`: ancho completo con imagen de fondo), `ancho` (`formulario`, `amplio`, `completo`). Incluye CSS, imagen y `app.js` |
| `<ui:formulario accion>` | Formulario POST + indicador de progreso (§5.8) | `accion` (ruta del contexto), `clase` |
| `<ui:encabezado>` | Encabezado de pantalla (§5.1) | `aplicacion="true"` en el acceso |
| `<ui:campo id>` | Etiqueta + campo de texto (§5.3, §5.4) | `etiqueta`, `tipo`, `valor`, `placeholder`, `autocomplete`, `inputmode`, `invalido`, `autofocus` |
| `<ui:selector id opciones>` | Etiqueta + selector (§5.5) | `etiqueta`, `opciones` (colección), `seleccionada` |
| `<ui:mensaje mensaje>` | Panel de mensaje o resultado (§5.2); no se dibuja vacío | `tipo`: `error` (defecto), `exito`, `resultado` |
| `<ui:boton>` | Botón primario / secundario (§5.6, §5.7) | `tipo`, `href` (enlace; sin él envía el formulario), `ancho`, `centrado`, `salida`, `deshabilitado` |
| `<ui:lista-acciones elementos destino parametro>` | Lista de acciones (§5.9) con datos del servidor | un botón primario por elemento |
| `<ui:cerrar-sesion/>` | Botón secundario de salida separado 32 (§5.7) | — |
| `<ui:cuadricula-acciones>` | Cuadrícula de acciones (§5.11) | un `<ui:boton>` por acción; 2 columnas, 1 bajo 600 px |
| `<ui:fila-campos>` | Dos campos en horizontal con una flecha entre ellos | cuerpo: dos `<ui:campo>`; bajo 600 px se apilan y la flecha apunta hacia abajo |
| `<ui:tarjeta>` | Tarjeta de resultado (§5.12) | `titulo`, `suma`, `monto`, `variacion`, `pie` |
| `<ui:tabla>` | Tabla (§5.13) | `columnas`, `filas`, `alDerecha` |
| `<ui:fila-botones>` | Fila de botones: la acción principal y Cancelar, centrados, separados 16; apilados bajo 600 px | cuerpo: `<ui:boton>`; el de Cancelar lleva `cancelar="true"` (la tecla Esc lo activa) |
| `<ui:campo-con-acciones>` | Un campo con sus botones a la derecha | fragmentos `campo` y `acciones` |
| `<ui:tarjetas>` | Fila de tarjetas de resultado | cuerpo: `<ui:tarjeta>`; se ocultan mientras hay una petición en curso |
| `<ui:progreso texto>` | Indicador de progreso con su texto (§5.8); lo incluye `<ui:formulario>` | `texto` (`Procesando…`, `Consultando…`, `Iniciando sesión…`) |

### 7.3 Escritorio (Swing + FlatLaf)
Dependencia: `com.formdev:flatlaf`. Presentación oscura → `FlatDarkLaf`; clara → `FlatLightLaf`. Un único punto de configuración al arrancar, **antes** de crear ventanas:

```java
FlatDarkLaf.setup();                       // o FlatLightLaf.setup()
UIManager.put("Component.arc", 8);         // radio
UIManager.put("Button.arc", 8);
UIManager.put("TextComponent.arc", 8);
UIManager.put("Component.focusColor", new Color(0x0EA5E9));
UIManager.put("Panel.background",  new Color(0x111E2F));   // claro: 0xF4F7FB
```
Los componentes se crean con una **fábrica** (botones primario/secundario, campos, selector, encabezados, etiquetas) que fija colores, fuente y medidas; **no** se colorean ni dimensionan componentes sueltos en las vistas. Los colores y medidas viven en **una clase de constantes**.

- **Ventana:** se abre en **1100 × 720** y el mínimo es **900 × 640**. Muestra una pantalla a la vez (`CardLayout`) en dos modos (§7.0):
  - **Acceso (dividido):** izquierda, panel de **560** con la pantalla en una columna de **420** centrada; derecha, un panel de imagen que la dibuja **completa y centrada**, a opacidad plena (§6).
  - **Aplicación (ancho completo):** un panel de fondo con la imagen **completa, centrada y con opacidad 40 %** (§6) y, encima, la pantalla centrada con el ancho de §7.0. Al cambiar el tamaño de la ventana la imagen se recalcula; no se estira ni se recorta.
- **Selector:** `JComboBox` con el aspecto de un campo (fondo blanco, radio 8, flecha `#424242`, `padding` 12).
- **Resultado:** una línea de texto plano, centrada, altura mínima 72 (§5.2). Los mensajes de error son HTML con ancho fijo (pueden ser largos).
- **Campo con error:** borde de 2 en `error` (`FlatClientProperties.OUTLINE`).
- Las operaciones lentas (red) no se ejecutan en el hilo de eventos (`SwingWorker`).
- **Recursos:** las imágenes deben quedar en el empaquetado. Con Maven, ubicarlas en `src/main/resources`; si se dejan junto al código, declarar `src/main/java` como recurso excluyendo `**/*.java`.

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
- [ ] Todos los colores salen de **tokens** (sin hex sueltos en vistas/layouts).
- [ ] Botón primario = `primario` + blanco; botón secundario **sólido** = `secundario` + blanco.
- [ ] Un solo radio (**8**) y la altura mínima de control de la plataforma.
- [ ] Texto principal y de botones con contraste **≥ 4,5:1** en la presentación elegida.
- [ ] Una sola acción principal por pantalla; las acciones de salida son botones secundarios.
- [ ] Mensajes sin códigos técnicos; cargas con progreso y controles deshabilitados.
- [ ] Sin sombras; sin más de dos pesos de fuente por pantalla.
- [ ] Si hay imagen decorativa: sin deformar, `fondo` de respaldo; en columna única completa y centrada (`fitCenter`, nunca recortada): cabecera a opacidad plena en el acceso y fondo fijo al 40 % con paneles `panel-sobre-imagen` en el resto; en disposición dividida completa (`contain`) y paneles `superficie`.
- [ ] En disposición dividida: columna de contenido centrada, imagen en la ventana (no en cada pantalla) y mínimo de ventana definido.
- [ ] Escritorio y web: dividida solo en el acceso; en el resto, imagen de fondo centrada (`contain`) con opacidad, que se reescala con la ventana, y paneles `panel-sobre-imagen`.
- [ ] Tarjetas de resultado: signo y color (`exito`/`error` de la presentación clara) juntos, y textos con contraste ≥ 4,5:1 sobre blanco.
