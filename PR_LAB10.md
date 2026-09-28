# Laboratorio 10 — Catálogo en cuadrícula y pedido

## Resumen

- **Persona 1 / Persona 2:** catálogo de 500 libros en `LazyVerticalGrid` de dos columnas,
  portadas remotas, búsqueda por título, FAB para volver al inicio y continuidad de estado.
- **Persona 3:** pedido completo manejado por el único `StoreViewModel`:
  - `OrderLine` (modelo) y `OrderResult` (resultado de cada operación).
  - `addToOrder(bookId, quantity)` acumula el mismo libro en una sola línea y valida que el
    libro exista, que la cantidad sea positiva y que no se superen las existencias.
  - Las operaciones inválidas se rechazan sin modificar el estado.
  - Aumentar, disminuir y eliminar; al llegar a cantidad cero la línea se elimina.
  - Subtotales, total y cantidad de unidades se calculan en el ViewModel (`BigDecimal`), no
    en Compose.
  - Pantalla **Mi pedido** con estado vacío, y acceso con contador de unidades en la barra
    superior del catálogo y del detalle.
  - Precios en quetzales con dos decimales (`Q 1,234.50`).

## Responsabilidades MVVM

| Capa | Responsabilidad |
|------|-----------------|
| Model | `Books`, `AuthorProfile`, `OrderLine` (subtotal), formato de quetzales. |
| ViewModel | `StoreViewModel` + `StoreUiState`: fuente de verdad, validaciones y cálculos del pedido. |
| View | `CatalogScreen`, `BookDetailScreen`, `OrderScreen`: dibujan estado y emiten eventos. |
| Navegación | Una instancia del ViewModel en `MainActivity`; Navigation 3 con `OrderKey`. |

## Pruebas manuales del pedido

| # | Paso | Resultado esperado | ✓ |
|---|------|--------------------|---|
| 1 | Abrir **Mi pedido** sin haber agregado nada | Estado vacío y botón “Ir al catálogo” | ✅ |
| 2 | Agregar “El principito” desde el detalle | Snackbar de confirmación, contador = 1 | ✅ |
| 3 | Agregarlo otra vez | Sigue una sola línea con cantidad 2, contador = 2 | ✅ |
| 4 | Agregar “Cien años de soledad” 3 veces y una cuarta | La cuarta se rechaza (existencias = 3) y el pedido no cambia | ✅ |
| 5 | Intentar agregar “El amor en los tiempos del cólera” | Rechazo por falta de existencias | ✅ |
| 6 | En el pedido, pulsar **+** hasta el límite | Snackbar “No hay más existencias”, cantidad no sube | ✅ |
| 7 | Pulsar **−** | Cantidad, subtotal, total y contador bajan | ✅ |
| 8 | Pulsar **−** con cantidad 1 | La línea desaparece | ✅ |
| 9 | Pulsar el ícono de eliminar | La línea desaparece; si era la última, estado vacío | ✅ |
| 10 | Verificar montos (2 × Q 89.90 + 3 × Q 149.50) | Subtotales Q 179.80 y Q 448.50, total Q 628.30 | ✅ |
| 11 | Rotar el dispositivo en la pantalla del pedido | El pedido se conserva | ✅ |
| 12 | Ejecutar `.\gradlew.bat test` | Las 23 pruebas de `StoreViewModelTest` pasan | ✅ |

## Evidencias

- Capturas del catálogo en cuadrícula y de Logcat (`CatalogProbe`) mostrando la carga
  perezosa de elementos (`ENTER`/`EXIT`).
- Capturas del pedido (carpeta `evidence/`):
  - `lab10-persona3-1-pedido-vacio.png` — estado vacío.
  - `lab10-persona3-2-rechazo-existencias.png` — rechazo al superar las existencias.
  - `lab10-persona3-3-pedido-totales.png` — líneas, subtotales, total y unidades.
  - `lab10-persona3-4-limite-existencias.png` — **+** bloqueado al llegar al stock.
  - `lab10-persona3-5-rotacion.png` — el pedido se conserva al rotar.
- Video del pedido (2:37): `evidence/lab10-persona3.mp4`.
- Video del catálogo (Persona 2): `evidence/lab10-persona2.mp4`.

## Uso de IA y fuentes externas

Ver [`DECLARACION_RECURSOS.md`](DECLARACION_RECURSOS.md).
