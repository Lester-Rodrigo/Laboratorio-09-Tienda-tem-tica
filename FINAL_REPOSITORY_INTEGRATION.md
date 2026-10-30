# Integración Repository y Persona 3

Fecha: 29 de octubre de 2026 (Guatemala).
Rama: feature/lab-final-firebase-repository.
Base previa: 0bfb541. SHA final evaluable: PENDIENTE después de los commits; obtener con git rev-parse HEAD.
No se realizaron staging, commits, push ni PR.

## Implementación

Se conserva observeCatalog(): Flow<CatalogResult> y el trabajo anterior.
RemoteOrdersRepository entrega OrderSubmission/RemoteOrder/RemoteOrderLine, nunca DTO. El dominio usa BigDecimal; los mappers envían cadenas de dos posiciones sin redondear silenciosamente. Se conservan ID textual, fecha, facturación, pago, líneas, cantidades e importes históricos. La implementación recibe OrderApi, usa Retrofit existente y solicita createdAt/desc. Los errores se traducen a RemoteOrderException del dominio. La cancelación se propaga.

BookStoreApplication crea AppContainer una vez; sus Repository son lazy. Las Factories obtienen APPLICATION_KEY y pasan interfaces al constructor. MainActivity usa ambas Factories. Se elimina BookRepositoryProvider. StoreViewModel deja de crear base, DAO, preferencias y cliente remoto. OrdersViewModel depende del Repository remoto.
StoreStateMapping recibe CartItem; el ordenamiento puro se mueve a model/CatalogOrdering.kt. Los errores de escritura local muestran un mensaje genérico. El pedido solo se vacía tras éxito remoto; un fallo remoto conserva carrito y formulario.

Loading precede autenticación. Solo después de ensureAnonymousSession exitoso se observa Firestore. Cada reintento cancela el intento previo mediante flatMapLatest. stateIn Eagerly mantiene una colección por ViewModel; viewModelScope cancela al destruirlo. Decisión: el listener continúa durante la vida del ViewModel, incluso sin colectores UI, evitando reinstalaciones por navegación/rotación. El DataSource conserva awaitClose/remove.
El último catálogo se conserva durante carga/error. Available se representa con Ready; Empty, InvalidDocuments y ReadError conservan su distinción. Los documentos parcialmente inválidos mantienen los libros válidos y una advertencia.
Buscar y ordenar son operaciones locales. El stock visible procede directamente de Firestore.
No se implementan reservas, devoluciones, transacciones, reglas ni concurrencia.

## Resultados

| Comprobación | Esperado | Observado |
|---|---|---|
| JVM | Sin fallos | 59 aprobadas: 44 anteriores, 6 de pedidos/mappers y 9 de ViewModels |
| Compilación | APK principal y pruebas | assembleDebug y assembleDebugAndroidTest correctos |
| Instrumentación | Persistencia y estados Compose | 6 aprobadas en Pixel_10a, Android 17 |
| Lint | Sin errores | 0 errores; 19 advertencias, incluida una nueva por versión más reciente de coroutines-test |
| Imports UI/ViewModels | Sin Firebase/Room/DataStore/Retrofit | Cero imports |
| Fronteras de datos | Sin DTO/Entity/DAO en UI y dominio | Cero referencias de producción |
| Suscripciones | Una activa; cierre al destruir | Fake mide máximo 1; al cancelar quedan 0 |
| Git | Sin errores de espacios | git diff --check correcto |
| Commit A aislado | Compila independientemente | 50 pruebas JVM y assembleDebug correctos |
| Commits A+B aislados | Compilan antes de la UI de C | 59 pruebas JVM y assembleDebug correctos |
| Firebase real y cambio de stock desde consola | Lectura autorizada y actualización visible | Pendiente manual |
| MockAPI real | Confirmación, lista y detalle | Pendiente manual; flujo probado con Fakes |
| Rotación, predictive back, navegación, autores | Continuidad | Código conservado; ejecución manual pendiente |

Entorno: copia fuera de OneDrive, JDK 21 completo, Gradle sin configuration cache. Se omitió gradle-daemon-jvm.properties solo en la copia para evitar el runtime VS Code sin jlink.
Se preservan todas las pruebas anteriores. Las aserciones !! existentes del mapper se sustituyen por requireNotNull sin cambiar la intención.
No se detectaron regresiones en lo ejecutado; no equivale a validar servicios reales. No se escribió en Firebase ni se enviaron pedidos reales durante las pruebas.
Pendientes del grupo: comprobar proveedor anónimo en consola, reglas definitivas y evidencias, reservas/devoluciones, concurrencia y video final.

## Decisiones

| Decisión | Solución | Alternativa descartada y motivo | Fuente |
|---|---|---|---|
| Contratos | Flow para catálogo y Result para acciones; dominio sin SDK | DTO/Entity en UI acopla presentación a persistencia | [Data layer](https://developer.android.com/topic/architecture/data-layer) |
| Inyección | Application/AppContainer y Factories con APPLICATION_KEY | Construcción en ViewModels dificulta Fakes y centralización | [Factories](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories) |
| Sesión | Esperar autenticación antes del listener | Escuchar antes puede violar request.auth | [Firebase Auth](https://firebase.google.com/docs/auth/android/anonymous-auth) |
| Suscripción | flatMapLatest y stateIn por ViewModel | Escuchar por recomposición acumula listeners | [Flows](https://developer.android.com/kotlin/flow) |
| Dinero | BigDecimal en dominio y String decimal en red | Double pierde precisión decimal histórica | MOCKAPI_ORDERS_CONTRACT.md |

## Comprobaciones y evidencias pendientes

Verificar favoritos y pedido después de reiniciar; búsqueda y orden; detalle y autor por ID; checkout y confirmación real; Mis pedidos y detalle; rotación y predictive back. Confirmar actualización de stock desde consola y reintento sin conexión. Usar datos ficticios.
Capturas: colección/documento, estados, búsqueda de imports, tres casos de Rules Playground y concurrencia junto a consola. Las dos últimas requieren completar el trabajo de otros integrantes.

## Guion de video (máximo 3 minutos)

No es una grabación ni evidencia ejecutada. Reservas/concurrencia deben completarse antes.
- 0:00–0:35: catálogo y consola; cambiar stock y mostrar actualización, búsqueda y orden.
- 0:35–1:20: reserva y devolución con consola visible (pendiente de otro bloque).
- 1:20–2:00: concurrencia, 20 intentos con 5 unidades: 5 éxitos, 15 rechazos y stock 0 (pendiente).
- 2:00–2:45: confirmar pedido, abrir Mis pedidos y detalle.
- 2:45–3:00: mostrar SHA final.

## Texto para PR

Integra catálogo Firestore con autenticación anónima y Repository, conservando CatalogResult. Inyecta interfaces mediante Application/AppContainer y Factories; oculta Room, DataStore y Retrofit detrás de contratos y mapea pedidos a dominio con BigDecimal.
La UI distingue carga, vacío, documentos inválidos y errores con reintento. Buscar/ordenar no crea listeners y el envío fallido conserva el carrito.
Verificación: 59 pruebas JVM, 6 instrumentadas, compilación y lint correctos. Cero imports de SDK de datos en ViewModels/pantallas.
Pendientes manuales y trabajo de reservas/reglas/concurrencia están identificados arriba; no se declara terminado el laboratorio completo.
Integrantes: PENDIENTE. Base PR: main. SHA final: PENDIENTE. Video/capturas: PENDIENTES.
IA: Codex asistió en implementación, refactorización, Fakes, pruebas y documentación. El equipo debe revisar y explicar el código en la defensa.

## Commits manuales

Ejecutar A → B → C → D desde:
C:\Users\Asus\OneDrive\Desktop\LABPM\Laboratorio-09-Tienda-tem-tica

Pantallas y estados que cambian DTO por dominio van en B para que compile la migración de ViewModels. C solo contiene representación/accesibilidad del catálogo y tests Compose.
Después de cada commit, git status --short mostrará los grupos posteriores todavía sin staging; después de D debe quedar limpio.

### Commit A

Archivos exactos:

- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/model/RemoteOrder.kt`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/model/repository/RemoteOrdersRepository.kt`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/data/remote/RemoteOrderMapping.kt`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/data/remote/RetrofitRemoteOrdersRepository.kt`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/di/AppContainer.kt`
- `app/src/test/java/com/example/laboratorio_09_tienda_temtica/data/remote/RemoteOrdersRepositoryTest.kt`

```powershell
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/model/RemoteOrder.kt"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/model/repository/RemoteOrdersRepository.kt"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/data/remote/RemoteOrderMapping.kt"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/data/remote/RetrofitRemoteOrdersRepository.kt"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/di/AppContainer.kt"
git add -- "app/src/test/java/com/example/laboratorio_09_tienda_temtica/data/remote/RemoteOrdersRepositoryTest.kt"
git diff --cached --check
git diff --cached
git commit -m "feat: add remote orders repository"
```

### Commit B

Archivos exactos:

- `app/build.gradle.kts`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/MainActivity.kt`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/data/BookRepositoryProvider.kt`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/data/preferences/StorePreferences.kt`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/StoreStateMapping.kt`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/StoreViewModel.kt`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/orders/OrderDetailScreen.kt`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/orders/OrderDetailUiState.kt`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/orders/OrdersScreen.kt`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/orders/OrdersUiState.kt`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/orders/OrdersViewModel.kt`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/screens/ConfirmationScreen.kt`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/state/SubmitOrderUiState.kt`
- `app/src/test/java/com/example/laboratorio_09_tienda_temtica/StoreStateMappingTest.kt`
- `app/src/test/java/com/example/laboratorio_09_tienda_temtica/data/remote/FirestoreBookMapperTest.kt`
- `gradle/libs.versions.toml`
- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/model/CatalogOrdering.kt`
- `app/src/test/java/com/example/laboratorio_09_tienda_temtica/ui/RepositoryFakes.kt`
- `app/src/test/java/com/example/laboratorio_09_tienda_temtica/ui/RepositoryViewModelsTest.kt`

```powershell
git add -- "app/build.gradle.kts"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/MainActivity.kt"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/data/BookRepositoryProvider.kt"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/data/preferences/StorePreferences.kt"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/StoreStateMapping.kt"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/StoreViewModel.kt"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/orders/OrderDetailScreen.kt"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/orders/OrderDetailUiState.kt"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/orders/OrdersScreen.kt"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/orders/OrdersUiState.kt"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/orders/OrdersViewModel.kt"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/screens/ConfirmationScreen.kt"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/state/SubmitOrderUiState.kt"
git add -- "app/src/test/java/com/example/laboratorio_09_tienda_temtica/StoreStateMappingTest.kt"
git add -- "app/src/test/java/com/example/laboratorio_09_tienda_temtica/data/remote/FirestoreBookMapperTest.kt"
git add -- "gradle/libs.versions.toml"
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/model/CatalogOrdering.kt"
git add -- "app/src/test/java/com/example/laboratorio_09_tienda_temtica/ui/RepositoryFakes.kt"
git add -- "app/src/test/java/com/example/laboratorio_09_tienda_temtica/ui/RepositoryViewModelsTest.kt"
git diff --cached --check
git diff --cached
git commit -m "refactor: inject repositories into viewmodels"
```

### Commit C

Archivos exactos:

- `app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/screens/CatalogScreen.kt`
- `app/src/androidTest/java/com/example/laboratorio_09_tienda_temtica/CatalogStatesTest.kt`

```powershell
git add -- "app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/screens/CatalogScreen.kt"
git add -- "app/src/androidTest/java/com/example/laboratorio_09_tienda_temtica/CatalogStatesTest.kt"
git diff --cached --check
git diff --cached
git commit -m "feat: complete remote catalog states"
```

### Commit D

Archivos exactos:

- `FINAL_REPOSITORY_INTEGRATION.md`

```powershell
git add -- "FINAL_REPOSITORY_INTEGRATION.md"
git diff --cached --check
git diff --cached
git commit -m "docs: document final repository integration"
```
