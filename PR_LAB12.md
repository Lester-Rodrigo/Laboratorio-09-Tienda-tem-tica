# Laboratorio 12 — Evidencias para el PR

Este documento reúne la información que debe copiarse o enlazarse en la descripción del PR de `feature/lab-12-local-persistence` hacia la rama principal que contiene el Laboratorio 11.

## Integrantes y versión evaluable

- Integrantes: **PENDIENTE**
- Rama base: **PENDIENTE: confirmar la rama principal usada por el equipo**
- PR del Laboratorio 11: **PENDIENTE**
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

El `ViewModel` sobrevive a un cambio de configuración, por lo que un dato que existe únicamente en memoria puede continuar visible después de rotar. El recorrido válido es marcar favoritos, armar un pedido y seleccionar el orden; detener el proceso con **Stop** o **Forzar detención**; y abrir la app nuevamente desde su ícono. Solo entonces se comprueba que Room y DataStore reconstruyen el estado desde disco. El resultado observado debe completarse después de ejecutar esta prueba en un dispositivo o emulador.

### Dato que no se guarda y costo de guardarlo

El catálogo no se guarda porque puede regenerarse exactamente con la semilla fija y sus IDs estables. Persistir sus 500 elementos duplicaría información, aumentaría el tamaño de la base y obligaría a decidir cómo actualizar o migrar registros cuando cambie el generador.

## Comprobaciones manuales

No completar la columna **Observado** hasta ejecutar cada recorrido con el APK correspondiente al SHA evaluable.

| Recorrido | Resultado esperado | Observado |
|---|---|---|
| Favoritos | Marcar dos libros, detener y reabrir: ambos siguen marcados en catálogo y detalle. | **PENDIENTE EN DISPOSITIVO** |
| Desmarcar | Desmarcar uno, detener y reabrir: permanece desmarcado. | **PENDIENTE EN DISPOSITIVO** |
| Pedido | Agregar dos libros, uno con cantidad 2, detener y reabrir: mismas líneas, cantidades y total. Superar existencias se rechaza y no modifica Room. | **PENDIENTE EN DISPOSITIVO** |
| Confirmación | Confirmar, detener y reabrir: el pedido permanece en 0 unidades y los favoritos no cambian. | **PENDIENTE EN DISPOSITIVO** |
| Preferencia | Elegir orden por precio, detener y reabrir: el catálogo abre ordenado por precio. | **PENDIENTE EN DISPOSITIVO** |

## Evidencias pendientes de adjuntar

- Captura del Database Inspector mostrando `favorites` y `order_lines` con filas; añadir otra evidencia de las filas eliminadas tras desmarcar o confirmar.
- Video continuo de máximo tres minutos con los recorridos exigidos, incluida la detención visible del proceso y la reapertura desde el ícono.
- URL del PR abierto y sin fusionar.
- SHA final evaluable.

## Declaración de herramientas y fuentes

Se utilizó Codex como apoyo para revisar la implementación, detectar faltantes, refactorizar el estado persistido y preparar pruebas y documentación. El equipo revisó el resultado y es responsable de validarlo en Android Studio y en un dispositivo o emulador. Fuentes: consigna y presentación del Laboratorio 12; documentación oficial de Android sobre Room 3, Preferences DataStore y Database Inspector.
