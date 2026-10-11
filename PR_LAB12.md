# Laboratorio 12 — Evidencias para el PR

Este documento reúne la información que debe copiarse o enlazarse en la descripción del PR de `feature/lab-12-local-persistence` hacia la rama principal que contiene el Laboratorio 11.

## Integrantes y versión evaluable

- Integrantes: **PENDIENTE**
- Rama base: `main`
- PR del Laboratorio 11: [PR #3](https://github.com/Lester-Rodrigo/Laboratorio-09-Tienda-tem-tica/pull/3)
- SHA final evaluable: **PENDIENTE: completar después del último commit**

## Tabla de decisiones

| Dato de la tienda | Dónde vive | Justificación |
|---|---|---|
| Favoritos | Room (`FavoriteEntity`) | Es una colección modificable por elemento. El identificador estable del libro funciona como clave primaria y permite insertar o borrar cada favorito sin guardar el catálogo completo. |
| Líneas del pedido | Room (`OrderLineEntity`) | Son varias filas que cambian de forma independiente. Se conserva únicamente `bookId` y cantidad; título y precio se reconstruyen desde el catálogo determinista. |
| Orden del catálogo | Preferences DataStore | Es una sola preferencia clave-valor (`name` o `price`) con valor predeterminado seguro. |
| Catálogo de 500 libros | No se guarda | Se regenera de forma idéntica con IDs estables y una semilla fija; persistirlo duplicaría datos y exigiría sincronización innecesaria. |
| Consulta de búsqueda | ViewModel | Debe mantenerse durante la sesión y la rotación, pero no aporta valor al volver a abrir la aplicación. |
| Posición del scroll | UI | Es estado visual gestionado por `LazyGridState`; no pertenece al modelo ni necesita sobrevivir al proceso. |
| Nombre y teléfono del checkout | No se guarda | Son datos personales de una operación todavía no confirmada. Guardarlos aumentaría la exposición de información sin necesidad funcional. |
| Errores e `isTouched` del formulario | ViewModel | Son estado transitorio de validación, útil durante la edición actual pero no después de reiniciar la app. |
| Recibo del último pedido | ViewModel | Solo respalda la pantalla de confirmación de la sesión. El laboratorio no solicita historial de pedidos y guardarlo ampliaría el alcance. |

## Respuestas requeridas

### ¿Por qué rotar no demuestra persistencia?

El `ViewModel` sobrevive a un cambio de configuración, por lo que un dato que existe únicamente en memoria puede continuar visible después de rotar. El recorrido válido es marcar favoritos, armar un pedido y seleccionar el orden; detener el proceso con **Forzar detención**; y abrir la app nuevamente desde su ícono. En el emulador `Pixel_10a` se comprobó que Room restauró favoritos y las líneas con cantidades 2 y 1, mientras DataStore aplicó nuevamente el orden por precio. Después de confirmar y repetir el recorrido, el pedido abrió vacío y el favorito restante continuó marcado.

### Dato que no se guarda y costo de guardarlo

El catálogo no se guarda porque puede regenerarse exactamente con la semilla fija y sus IDs estables. Persistir sus 500 elementos duplicaría información, aumentaría el tamaño de la base y obligaría a decidir cómo actualizar o migrar registros cuando cambie el generador.

## Comprobaciones manuales

| Recorrido | Resultado esperado | Observado |
|---|---|---|
| Favoritos | Marcar dos libros, detener y reabrir: ambos siguen marcados en catálogo y detalle. | **Cumple.** Se marcaron “Cien años de soledad” y “El amor en los tiempos del cólera”; después de forzar la detención y abrir desde el ícono, Room los restauró. |
| Desmarcar | Desmarcar uno, detener y reabrir: permanece desmarcado. | **Cumple.** Se buscó y desmarcó “Cien años de soledad”; después de otra detención y reapertura apareció con estrella vacía. La inspección final de SQLite conservó únicamente `book-3`. |
| Pedido | Agregar dos libros, uno con cantidad 2, detener y reabrir: mismas líneas, cantidades y total. Superar existencias se rechaza y no modifica Room. | **Cumple.** Se restauraron “Cien años de soledad” con cantidad 2 y “El principito” con cantidad 1, total `Q 388.90`. Al intentar superar el stock de 3, la cantidad permaneció en 3 y se mostró el rechazo “No hay más existencias…”. |
| Confirmación | Confirmar, detener y reabrir: el pedido permanece en 0 unidades y los favoritos no cambian. | **Cumple.** Tras confirmar, detener y abrir desde el ícono, “Mi pedido” mostró el estado vacío y el favorito restante siguió almacenado. |
| Preferencia | Elegir orden por precio, detener y reabrir: el catálogo abre con la preferencia aplicada. | **Cumple.** En cada reapertura apareció seleccionado “Por precio”; el archivo de DataStore contiene `sort_order = price`. |

## Verificación automatizada

- `test`: 19 pruebas JVM aprobadas.
- `assembleDebug`: compilación correcta del APK correspondiente a `b65dfe5`.
- `connectedDebugAndroidTest`: 3 pruebas aprobadas en `Pixel_10a` (API 36), incluidas inserción/observación/borrado de Room y persistencia de DataStore.
- `lintDebug`: correcto, sin errores.
- Inspección final de SQLite: un favorito (`book-3`) y cero líneas del pedido después de confirmar.

## Evidencias pendientes de adjuntar

- **PENDIENTE:** captura de Android Studio Database Inspector mostrando `favorites` y `order_lines`. La base ya fue validada mediante pruebas instrumentadas y consulta directa, pero esta captura debe realizarse desde Android Studio para cumplir el formato solicitado.
- Video continuo: [`evidence/lab12-local-persistence.mp4`](evidence/lab12-local-persistence.mp4), duración `02:53`. Incluye favoritos, desmarcado, pedido persistido, orden por precio, detenciones y reaperturas desde el ícono, confirmación, pedido vacío y rotación.
- URL del PR abierto y sin fusionar.
- SHA final evaluable.

## Declaración de herramientas y fuentes

Se utilizó Codex como apoyo para revisar la implementación, detectar faltantes, refactorizar el estado persistido y preparar pruebas y documentación. El equipo revisó el resultado y es responsable de validarlo en Android Studio y en un dispositivo o emulador. Fuentes: consigna y presentación del Laboratorio 12; documentación oficial de Android sobre Room 3, Preferences DataStore y Database Inspector.
