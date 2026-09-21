# Laboratorio 09/10 — Tienda temática

Aplicación Android de una librería creada con Kotlin, Jetpack Compose, Material 3,
Navigation 3 y Coil 3.

## Funcionalidades

- Catálogo determinista de 500 libros en una cuadrícula de dos columnas.
- Búsqueda por título que ignora mayúsculas, minúsculas y espacios exteriores.
- Contador de resultados, limpieza de consulta y estado sin coincidencias.
- Botón flotante para volver suavemente al inicio después de desplazarse por el catálogo.
- Navegación `Catálogo → Detalle → Autor` mediante IDs serializables.
- Conservación de la búsqueda y de la posición del catálogo al navegar o rotar.
- Portadas remotas con estados de carga y error.
- Favoritos en memoria.
- Validación de “Agregar al pedido” según las existencias, con confirmación mediante Snackbar.
- Transiciones breves de avance, regreso y predictive back.
- Pedido en memoria: agregar, acumular el mismo libro en una sola línea, aumentar,
  disminuir y eliminar, con validación de existencias.
- Subtotales y total calculados en el ViewModel y mostrados en quetzales con dos decimales.
- Pantalla “Mi pedido” con estado vacío y acceso desde la barra superior con la cantidad
  total de unidades.

## Arquitectura

`StoreViewModel` mantiene un `StateFlow<StoreUiState>` como fuente de verdad del catálogo,
la consulta, los resultados filtrados y los favoritos. Las pantallas reciben estado y
callbacks; no modifican directamente datos globales. Navigation 3 conserva una pila con
claves que contienen únicamente `bookId` o `authorId`. El catálogo guarda su
`LazyGridState` mediante el mecanismo saveable de Compose para preservar índice y offset.

### Responsabilidades MVVM

| Capa | Responsabilidad | Archivos |
|------|-----------------|----------|
| Model | Datos del dominio y reglas puras: libros, autores, líneas del pedido, subtotal y formato de quetzales. | `model/Books.kt`, `model/AuthorProfile.kt`, `model/OrderLine.kt`, `model/BookCatalogGenerator.kt` |
| ViewModel | Única fuente de verdad (`StateFlow<StoreUiState>`). Valida y ejecuta `addToOrder`, aumentar, disminuir y eliminar; calcula total y unidades; rechaza operaciones inválidas sin modificar el estado. | `ui/StoreViewModel.kt`, `ui/StoreUiState.kt` |
| View | Pantallas Compose que solo dibujan el estado recibido y emiten eventos mediante callbacks; no calculan totales ni modifican el pedido. | `ui/screens/CatalogScreen.kt`, `ui/screens/BookDetailScreen.kt`, `ui/screens/OrderScreen.kt` |
| Navegación / estado de UI | Una sola instancia de `StoreViewModel` creada en `MainActivity`; Navigation 3 decide qué pantalla mostrar con claves serializables (`CatalogKey`, `BookDetailKey`, `OrderKey`, `AuthorProfileKey`). | `MainActivity.kt`, `navigation/NavigationStorage.kt`, `navigation/NavKeyStorage.kt` |

## Ejecución

1. Abre el proyecto con una versión de Android Studio compatible con AGP 9.3.
2. Verifica que el SDK de Android configurado permita compilar con API 37.
3. Ejecuta la configuración `app` en un emulador o dispositivo con Android 14 (API 34) o superior.

Desde PowerShell también puedes compilar con:

```powershell
$env:GRADLE_USER_HOME = "$PWD\.gradle-local"
.\gradlew.bat assembleDebug
```

## Pruebas

Las pruebas unitarias cubren el catálogo, favoritos, búsqueda, resolución por ID y la
reglas del pedido: acumulación en una línea, validación de libro, cantidad y existencias,
rechazo sin cambios, eliminación al llegar a cero, subtotales, total y formato en quetzales.

```powershell
$env:GRADLE_USER_HOME = "$PWD\.gradle-local"
.\gradlew.bat test
```

Las transiciones siguen la guía oficial de Android para
[animar destinos con Navigation 3](https://developer.android.com/guide/navigation/navigation-3/animate-destinations).

## Guion de evidencia en video

1. Mostrar el catálogo inicial y sus 500 resultados.
2. Buscar un título usando mayúsculas y espacios exteriores, conservando el texto escrito.
3. Mostrar el contador de coincidencias.
4. Escribir una consulta sin resultados y limpiar la búsqueda desde el estado vacío.
5. Desplazarse hasta que aparezca el FAB y pulsarlo para volver suavemente al inicio.
6. Abrir un libro y comprobar portada, título, descripción, precio, stock y autor.
7. Agregar al pedido un libro con stock y mostrar la confirmación.
8. Abrir un libro agotado y mostrar el rechazo del pedido.
9. Entrar al perfil del autor y regresar al detalle.
10. Regresar al catálogo y comprobar que conserva consulta y posición exacta.
11. Rotar el dispositivo y comprobar nuevamente la continuidad del estado.

La grabación debe realizarse manualmente en un emulador o dispositivo; no forma parte del
proceso automatizado de compilación.
