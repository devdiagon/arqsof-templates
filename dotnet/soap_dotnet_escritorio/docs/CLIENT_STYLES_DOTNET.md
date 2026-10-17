# Estilos compartidos de los clientes gráficos .NET (escritorio WPF, web MVC y móvil MAUI)

Define **un solo lenguaje visual** para clientes con interfaz gráfica en **escritorio (WPF), web (ASP.NET Core MVC con vistas Razor) y móvil (.NET MAUI)**.
Es la versión .NET de `CLIENT_STYLES_JAVA.md`: **misma estructura, mismas medidas, mismos componentes y mismas reglas**; cambian la **tecnología de implementación** (§7) y la **temática de color**, que pasa de azul a **morado**.
Es **independiente del dominio** (no menciona qué hace la aplicación), del protocolo de comunicación y del lenguaje: sirve para cualquier proyecto cuyas pantallas combinen **acceso con credenciales, listas de acciones, formularios y resultados**.
No aplica a clientes de consola.

Aquí solo se define **cómo se ve**. El flujo, los textos concretos, las imágenes y las reglas de negocio de cada proyecto se documentan aparte (en este repositorio: `soap/CLIENTES_CONSTRUCT.md`).

> Verificación: los **contrastes** de §2 se calcularon con la fórmula WCAG 2.x (luminancia relativa) sobre los valores exactos de las tablas. **Estado:** este documento es de diseño; ningún cliente .NET lo aplica todavía, así que las secciones de implementación (§7) no se han probado en pantalla.

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
| `error` | `#F87171` | Texto o indicador de error | 6,5:1 sobre `fondo` |
| `exito` | `#4ADE80` | Texto o indicador de éxito | 10,3:1 sobre `fondo` |

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

Notas de contraste:
- `primario` sobre `fondo` oscuro da solo 2,5:1: el botón se reconoce por su **etiqueta blanca (7,1:1)** y su forma, no solo por el relleno. Lo mismo vale para `secundario` sobre `fondo` oscuro (2,0:1).
- `acento` (`#A78BFA`) es el color de marca sobre fondo oscuro (6,6:1); no se usa como relleno de botón porque con texto blanco no alcanza contraste, ni como texto sobre fondo claro.
- Diferencias frente a `CLIENT_STYLES_JAVA.md`: el `primario` morado tiene **más** contraste con el texto blanco que el azul original (7,1:1 frente a 5,9:1), por lo que los botones quedan más legibles; el resto de los tokens (`error`, `exito`, `campo-*`) conserva su función con ajustes de matiz.

---

## 3. Tipografía

| Plataforma | Familia | Notas |
|---|---|---|
| **Móvil (MAUI)** | La del sistema (`Roboto` en Android, `Segoe UI` en Windows) | No se incrusta ninguna fuente: eliminar `OpenSans` de la plantilla de MAUI (`ConfigureFonts` en `MauiProgram.cs`) o dejarla sin usar en los estilos. |
| **Web (MVC)** | `"Inter", "Segoe UI", Roboto, system-ui, -apple-system, Arial, sans-serif` | `Inter` es opcional (Google Fonts); la pila siempre cae a fuentes del sistema. |
| **Escritorio (WPF)** | `Segoe UI` (la predeterminada de Windows) | No forzar otra familia; fijarla una vez en el estilo base de `Window` / `TextBlock`. |

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
- Si la imagen es **vertical** y sus **bordes tienen exactamente el color `fondo`** (`#1A1426`), puede mostrarse completa sobre `fondo` y fundirse sin costuras. Es la condición para usar el modo `contain` descrito abajo; si una imagen no la cumple, debe usarse con paneles `panel-sobre-imagen` o recortada. *Una imagen preparada para el `fondo` azul de `CLIENT_STYLES_JAVA.md` (`#111E2F`) no cumple esta condición: hay que regenerarla con el color morado.*
- **Columna única (móvil):** la imagen cubre toda la pantalla, **recortada** (`AspectFill`), sin deformarse. Los paneles usan `panel-sobre-imagen` para que el texto se lea sobre las zonas claras.
- **Pantalla ancha (escritorio y web, §7.0):** la imagen ocupa su propia zona, **completa y centrada** (`Stretch="Uniform"` en WPF; `background-size: contain` en CSS), sin recortar ni deformar; el resto de la zona es `fondo`. No se usa `UniformToFill`/`cover`: al maximizar la ventana, una imagen vertical ampliada para cubrir una zona ancha se recorta y parece estirada. Como el contenido no queda sobre la imagen, los paneles usan `superficie`.
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

### 7.1 Móvil (.NET MAUI, XAML)
Los tokens son recursos en `Resources/Styles/Colors.xaml`; los estilos, en `Resources/Styles/Styles.xaml`. Las páginas (`ContentPage`) **solo referencian estilos** con `{StaticResource …}` o `Style="{StaticResource …}"` y nunca valores sueltos. La plantilla de MAUI trae colores y estilos de ejemplo (`Primary`, `Gray…`): **reemplazarlos** por estos tokens para que no haya dos paletas.

```xml
<!-- Colors.xaml (presentación oscura) -->
<Color x:Key="Fondo">#1A1426</Color>
<Color x:Key="Superficie">#271D3A</Color>
<Color x:Key="PanelSobreImagen">#99000000</Color>
<Color x:Key="Texto">#F5F3FF</Color>
<Color x:Key="TextoSecundario">#B4A9CE</Color>
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
```

- **Campo y selector:** `Entry` y `Picker` no tienen radio ni borde propios. Se envuelven en un `Border` (`StrokeShape="RoundRectangle 8"`, `BackgroundColor="{StaticResource CampoFondo}"`, `Padding="12,0"`, `MinimumHeightRequest="48"`) y el control interior va con fondo transparente y `TextColor="{StaticResource CampoTexto}"`. El foco y el error cambian el `Stroke` del `Border` (2 en `Acento` / `Error`).
- **Aplicación:** `Application.UserAppTheme = AppTheme.Dark` (o `Light`) fijo en `App.xaml.cs`, sin `AppThemeBinding`: no hay selector de tema (§1). La presentación clara redefine los mismos nombres en `Colors.xaml` con los valores de §2.3.
- **Indicador de progreso:** `ActivityIndicator` con `Color="{StaticResource Acento}"`, deshabilitando los controles mientras `IsRunning`.
- **Imagen:** `Image` con `Aspect="AspectFill"` a pantalla completa tras el contenido, con `BackgroundColor="{StaticResource Fondo}"`. Los paneles usan `PanelSobreImagen`.
- **Zonas seguras y barra de estado:** en Android, fijar el color de la barra de estado al de `Fondo`; el contenido respeta las zonas seguras (`SafeAreaEdges`/`Padding`), con márgenes en los hijos y no en el contenedor ya acolchado.
- Reglas MVVM: la `ContentPage` no tiene lógica; el ViewModel expone propiedades y comandos (`CommunityToolkit.Mvvm` es opcional).

### 7.2 Web (ASP.NET Core MVC con vistas Razor)
Los tokens son variables CSS; el tema por defecto se fija con **una clase en `<body>`** (`tema-oscuro` o `tema-claro`), sin script ni selector en pantalla. El CSS vive en `wwwroot/css/estilos.css` (reemplaza `site.css` de la plantilla; **quitar Bootstrap** o no usar sus clases en las vistas, para que no haya dos sistemas de estilos).

```css
:root { --radio: 8px; --primario: #6D28D9; --primario-p: #5B21B6; --secundario: #4B4563;
        --secundario-p: #3A3550; --acento: #A78BFA; --sobre-accion: #FFFFFF;
        --campo-fondo: #FFFFFF; --campo-texto: #1E1033; --campo-borde: #6B6280; }
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
.zona-imagen { background: var(--fondo) url("../img/imagen-decorativa.jpeg") center / contain no-repeat; }
@media (max-width: 899px) {                   /* bajo el mínimo del escritorio: columna única, sin imagen */
  .pagina { grid-template-columns: 1fr; }
  .zona-imagen { display: none; }
}
```
`.contenedor { max-width: 480px }` queda solo para la variante de columna única (ancho < 900). La imagen usa `contain` (§6), nunca `cover`.

**Estructura Razor.** El esqueleto de página (disposición dividida, `<body class="tema-oscuro">`, hoja de estilos, imagen) vive en `Views/Shared/_Layout.cshtml`; cada pantalla solo aporta su contenido. Cada componente de §5 es un **componente reutilizable** en `Views/Shared/Componentes/` (vistas parciales con modelo tipado o *tag helpers*; el proyecto elige una de las dos formas y la usa en todas las vistas). Nombres propuestos como *tag helpers* (`@addTagHelper *, <Proyecto>` en `_ViewImports.cshtml`):

| Etiqueta | Componente (§) | Atributos principales |
|---|---|---|
| `_Layout` | Estructura de página, disposición dividida, fondo (§5.10, §7.0) | `ViewData["Titulo"]` (obligatorio). Incluye CSS e imagen |
| `<ui-formulario accion>` | Formulario POST + indicador de progreso (§5.8) | `accion` (acción del controlador), `clase`. Incluye el token anti-falsificación |
| `<ui-encabezado>` | Encabezado de pantalla (§5.1) | `aplicacion="true"` en el acceso |
| `<ui-campo id>` | Etiqueta + campo de texto (§5.3, §5.4) | `etiqueta`, `tipo`, `valor`, `placeholder`, `autocomplete`, `inputmode`, `invalido`, `autofocus` |
| `<ui-selector id opciones>` | Etiqueta + selector (§5.5) | `etiqueta`, `opciones` (colección), `seleccionada` |
| `<ui-mensaje mensaje>` | Panel de mensaje o resultado (§5.2); no se dibuja vacío | `tipo`: `error` (defecto), `exito`, `resultado` |
| `<ui-boton>` | Botón primario / secundario (§5.6, §5.7) | `tipo`, `href` (enlace; sin él envía el formulario), `ancho`, `centrado`, `salida`, `deshabilitado` |
| `<ui-lista-acciones elementos destino parametro>` | Lista de acciones (§5.9) con datos del servidor | un botón primario por elemento |
| `<ui-cerrar-sesion/>` | Botón secundario de salida separado 32 (§5.7) | — |

Todo texto dinámico se escribe con el escape predeterminado de Razor (`@Modelo.Mensaje`); **no** usar `@Html.Raw` con datos del servidor. El JavaScript, si hace falta (deshabilitar el botón y mostrar el indicador al enviar), va en un único `wwwroot/js/app.js`.

### 7.3 Escritorio (WPF)
No requiere bibliotecas de terceros: el aspecto sale de **estilos y plantillas XAML propios**, definidos una vez. Los colores y medidas viven en `Estilos/Tema.xaml` (un `ResourceDictionary` con pinceles y números) y los estilos de controles en `Estilos/Controles.xaml`; ambos se cargan en `App.xaml` (`Application.Resources` → `ResourceDictionary.MergedDictionaries`). Las vistas **solo referencian recursos** (`{StaticResource …}`); no colorean ni dimensionan componentes sueltos.

```xml
<!-- Tema.xaml (presentación oscura) -->
<Color x:Key="ColorFondo">#1A1426</Color>
<SolidColorBrush x:Key="Fondo" Color="{StaticResource ColorFondo}" />
<SolidColorBrush x:Key="Superficie" Color="#271D3A" />
<SolidColorBrush x:Key="Texto" Color="#F5F3FF" />
<SolidColorBrush x:Key="TextoSecundario" Color="#B4A9CE" />
<SolidColorBrush x:Key="Acento" Color="#A78BFA" />
<SolidColorBrush x:Key="Primario" Color="#6D28D9" />
<SolidColorBrush x:Key="PrimarioPresionado" Color="#5B21B6" />
<SolidColorBrush x:Key="Secundario" Color="#4B4563" />
<SolidColorBrush x:Key="SecundarioPresionado" Color="#3A3550" />
<SolidColorBrush x:Key="SobreAccion" Color="#FFFFFF" />
<SolidColorBrush x:Key="Error" Color="#F87171" />
<SolidColorBrush x:Key="CampoFondo" Color="#FFFFFF" />
<SolidColorBrush x:Key="CampoTexto" Color="#1E1033" />
<CornerRadius x:Key="Radio">8</CornerRadius>

<!-- Controles.xaml: botón primario con plantilla propia (los estilos por defecto de WPF no admiten radio) -->
<Style x:Key="BotonPrimario" TargetType="Button">
    <Setter Property="Foreground" Value="{StaticResource SobreAccion}" />
    <Setter Property="Background" Value="{StaticResource Primario}" />
    <Setter Property="FontSize" Value="15" />
    <Setter Property="FontWeight" Value="Bold" />
    <Setter Property="MinHeight" Value="36" />
    <Setter Property="Padding" Value="20,8" />
    <Setter Property="Template">
        <Setter.Value>
            <ControlTemplate TargetType="Button">
                <Border x:Name="Raiz" Background="{TemplateBinding Background}" CornerRadius="{StaticResource Radio}"
                        Padding="{TemplateBinding Padding}">
                    <ContentPresenter HorizontalAlignment="Center" VerticalAlignment="Center" />
                </Border>
                <ControlTemplate.Triggers>
                    <Trigger Property="IsMouseOver" Value="True">
                        <Setter TargetName="Raiz" Property="Background" Value="{StaticResource PrimarioPresionado}" />
                    </Trigger>
                    <Trigger Property="IsEnabled" Value="False"><Setter TargetName="Raiz" Property="Opacity" Value="0.6" /></Trigger>
                </ControlTemplate.Triggers>
            </ControlTemplate>
        </Setter.Value>
    </Setter>
</Style>
<!-- BotonSecundario: BasedOn BotonPrimario con Secundario / SecundarioPresionado. -->
```

- **Nombres de recursos únicos:** los pinceles de `Tema.xaml` llevan el nombre del token (`CampoTexto`, `Primario`) y los estilos de `Controles.xaml` llevan el nombre del control (`CajaTexto`, `CajaContrasenia`, `Selector`, `BotonPrimario`). Si un pincel y un estilo comparten clave, WPF usa uno por el otro y la aplicación se cae al cargar la vista (`"System.Windows.Style" no es un valor válido para la propiedad "Foreground"`), sin ningún error de compilación.
- **Campos y selector:** `TextBox`, `PasswordBox` y `ComboBox` con plantilla propia (un `Border` con radio 8, `campo-fondo`, `padding` 12, alto mínimo 36). `ComboBox`: flecha `#424242` (§5.5). Foco → borde 2 en `Acento`; error → borde 2 en `Error`, con un `Trigger` sobre una propiedad adjunta o `Validation.HasError`.
- **Ventana:** disposición dividida (§7.0). Se abre en **1100 × 720** y el mínimo es **900 × 640** (`MinWidth`/`MinHeight`). Un `Grid` de dos columnas: `Width="560"` y `Width="*"`. Izquierda: un `ContentControl` que muestra la vista de la pantalla actual (cada una en una columna de **420** centrada); la navegación cambia su contenido (se asocia cada ViewModel a su vista con `DataTemplate`). Derecha: un `Image` con `Stretch="Uniform"` y `Background="{StaticResource Fondo}"` (§6).
- **Resultado:** una línea de texto plano, centrada, altura mínima 72 (§5.2). Los mensajes de error pueden ser largos: `TextBlock` con `TextWrapping="Wrap"` y ancho fijo.
- **Indicador de progreso:** `ProgressBar IsIndeterminate="True"` (o un `ProgressBar` circular propio) en `Acento`, visible mientras se ejecuta el comando; `IsEnabled` de la pantalla en `false` mientras tanto.
- **Presentación clara:** cambiar los valores de `Tema.xaml` por los de §2.3; los estilos no cambian.
- Las operaciones lentas (red) no se ejecutan en el hilo de la interfaz: comandos asíncronos (`async`/`await`; `AsyncRelayCommand` de `CommunityToolkit.Mvvm` es opcional). Con `await` la continuación vuelve sola al hilo UI.
- **Recursos:** la imagen se agrega al proyecto con *Build Action* `Resource` (o `Content` + copiar al directorio de salida) y se referencia con una URI `pack://`.
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
- [ ] Sin sombras; sin más de dos pesos de fuente por pantalla.
- [ ] Si hay imagen decorativa: sin deformar, `fondo` de respaldo (`#1A1426`) y bordes de la imagen del mismo color; en columna única recortada con paneles `panel-sobre-imagen`; en disposición dividida completa (`contain`/`Uniform`) y paneles `superficie`.
- [ ] En disposición dividida: columna de contenido centrada, imagen en la ventana (no en cada pantalla) y mínimo de ventana definido.
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
| Escritorio | Swing + FlatLaf | WPF (XAML, estilos y plantillas propios) |
| Web | JSP + tag files (`WEB-INF/tags`) | MVC + vistas Razor (`_Layout`, componentes) |
| Móvil | Android XML (`colors.xml`, `styles.xml`) | MAUI XAML (`Colors.xaml`, `Styles.xaml`) |
| Medidas, tipografía por rol, componentes (§5) | — | **idénticos** |
