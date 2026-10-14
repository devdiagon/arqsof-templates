# Estilos compartidos de los clientes gráficos (escritorio, web y móvil)

Define **un solo lenguaje visual** para clientes con interfaz gráfica en **escritorio (Swing), web y móvil (Android)**.
Es **independiente del dominio** (no menciona qué hace la aplicación), del protocolo de comunicación y del lenguaje: sirve para cualquier proyecto cuyas pantallas combinen **acceso con credenciales, listas de acciones, formularios y resultados**.
No aplica a clientes de consola.

Aquí solo se define **cómo se ve**. El flujo, los textos concretos, las imágenes y las reglas de negocio de cada proyecto se documentan aparte (en el `PROJECT.md` del proyecto).

> Verificación: los **contrastes** de §2 se calcularon con la fórmula WCAG 2.x.

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

Reglas: máximo **dos pesos** por pantalla (regular y bold); sin cursiva; sin mayúsculas forzadas; los textos llevan las tildes y caracteres propios del idioma.

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
- **Resultado:** tamaño *Resultado*, centrado, altura mínima 72. Es una línea corta de texto plano.
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

---

## 6. Imagen decorativa (opcional)

- Solo en la **presentación oscura**. La imagen **nunca se deforma**.
- Si la imagen es **vertical** y sus **bordes tienen exactamente el color `fondo`**, puede mostrarse completa sobre `fondo` y fundirse sin costuras. Es la condición para usar el modo `contain` descrito abajo; si una imagen no la cumple, debe usarse con paneles `panel-sobre-imagen` o recortada.
- **Columna única (móvil):** la imagen cubre toda la pantalla, **recortada** (`centerCrop`), sin deformarse. Los paneles usan `panel-sobre-imagen` para que el texto se lea sobre las zonas claras.
- **Pantalla ancha (escritorio y web, §7.0):** la imagen ocupa su propia zona, **completa y centrada** (`contain` / `background-size: contain`), sin recortar ni deformar; el resto de la zona es `fondo`. No se usa `cover`: al maximizar la ventana, una imagen vertical ampliada para cubrir una zona ancha se recorta y parece estirada. Como el contenido no queda sobre la imagen, los paneles usan `superficie`.
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
- Las medidas de componentes (§4 y §5) no cambian; solo cambia dónde se colocan.
- Si el ancho disponible es menor que el mínimo de la plataforma, la zona derecha puede quedar muy estrecha: en escritorio se fija un mínimo de ventana (§7.3); en la web se pasa a columna única sin imagen por debajo de ese ancho (§7.2).

### 7.1 Móvil (Android, vistas XML)
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
        --campo-fondo: #FFFFFF; --campo-texto: #0F172A; --campo-borde: #64748B; }
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
.zona-imagen { background: var(--fondo) url("imagen-decorativa.jpeg") center / contain no-repeat; }
@media (max-width: 899px) {                   /* bajo el mínimo del escritorio: columna única, sin imagen */
  .pagina { grid-template-columns: 1fr; }
  .zona-imagen { display: none; }
}
```
`.contenedor { max-width: 480px }` queda solo para la variante de columna única (ancho < 900). La imagen usa `contain` (§6), nunca `cover`.

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

- **Ventana:** disposición dividida (§7.0). Se abre en **1100 × 720** y el mínimo es **900 × 640**. Izquierda: panel de **560** de ancho que muestra una pantalla a la vez (`CardLayout`), cada una en una columna de **420** centrada. Derecha: un panel de imagen que la dibuja **completa y centrada** (§6).
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
- [ ] Si hay imagen decorativa: sin deformar, `fondo` de respaldo; en columna única recortada con paneles `panel-sobre-imagen`; en disposición dividida completa (`contain`) y paneles `superficie`.
- [ ] En disposición dividida: columna de contenido centrada, imagen en la ventana (no en cada pantalla) y mínimo de ventana definido.
