# Laboratorio 11 — Checkout con MVVM

## Resumen de cambios

- Se incorporó un formulario de checkout alojado en `StoreViewModel`, con estado inmutable,
  validación reactiva y errores visibles solamente después de la primera edición.
- La facturación inicia en Consumidor Final y permite Factura con NIT con campos animados,
  limpieza de estados `isTouched` al volver a CF y revalidación al regresar a NIT.
- Se agregaron radios accesibles, recorrido de IME, desplazamiento con teclado y botón de
  confirmación calculado mediante `derivedStateOf`.
- La confirmación valida nuevamente, captura un recibo inmutable, genera un folio secuencial,
  vacía el pedido, reinicia el formulario y conserva el recibo.
- Navigation 3 enlaza Pedido → Checkout → Confirmación y limpia el historial al volver al
  catálogo.
- Se ampliaron las pruebas JVM de validadores, transiciones del formulario, rechazo seguro,
  captura del total, reinicio y secuencia de folios.

## Matriz de auditoría

| Criterio | Estado inicial | Evidencia actual | Qué faltaba | Acción aplicada | Estado final |
|---|---|---|---|---|---|
| Estado inmutable y validación pura (25) | Parcial | [`CheckoutUiState`](app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/CheckoutUiState.kt), [`CheckoutValidators.kt`](app/src/main/java/com/example/laboratorio_09_tienda_temtica/model/CheckoutValidators.kt), `checkoutUiState.asStateFlow()` | Hacer explícito `trim()` en nombre y completar cobertura de estado inicial/corrección | Se ajustó el validador y se agregaron pruebas | Completo |
| Facturación condicional y limpieza en cascada (25) | Completo con cobertura parcial | `updateBillingType`, `AnimatedVisibility`, errores fiscales derivados | Probar conservación de texto, reinicio de `isTouched` y revalidación al regresar a NIT | Se agregó prueba de ida CF↔NIT con datos inválidos | Completo |
| Ergonomía móvil, radios y foco (20) | Completo | [`CheckoutScreen`](app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/screens/CheckoutScreen.kt): `verticalScroll`, `imePadding`, `FocusRequester`, `selectableGroup`, filas de 48 dp | No faltaba implementación; quedaba verificación estática | Se auditó y se corrigió la convención de `Modifier` detectada por lint en confirmación | Completo |
| Confirmación y reinicio del pedido (20) | Parcial | `confirmOrder`, [`OrderReceipt`](app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/OrderReceipt.kt), [`ConfirmationScreen`](app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/screens/ConfirmationScreen.kt) | Reforzar atomicidad y pruebas de total, pago, datos fiscales, pedido vacío e intento inválido | `confirmOrder` se sincronizó y la secuencia solo se publica tras crear recibo y reiniciar; se ampliaron pruebas | Completo |
| Justificación en PR y convenciones (10) | Ausente | Este documento y nombres en inglés del dominio | Faltaba descripción lista para PR y matriz verificable | Se agregó este documento con decisiones, recorridos, IA, fuentes y pruebas | Completo |

## Elementos y responsabilidades MVVM

| Categoría | Elemento y enlace al código | Dónde vive / quién lo controla | Justificación |
|---|---|---|---|
| Modelo | [`OrderReceipt`](app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/OrderReceipt.kt) | Modelo inmutable conservado por `StoreViewModel` | Congela nombre, teléfono, facturación, pago y total antes de vaciar el pedido; la confirmación no recalcula desde un carrito vacío. |
| Regla de negocio | [`validateFullName`](app/src/main/java/com/example/laboratorio_09_tienda_temtica/model/CheckoutValidators.kt) | Kotlin puro, sin Android, Compose, `Context` ni estado | Permite probar la regla de manera determinista y reutilizarla sin acoplarla a la interfaz. |
| Estado de pantalla | [`CheckoutUiState`](app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/CheckoutUiState.kt) | `MutableStateFlow` privado y `StateFlow` público de solo lectura en `StoreViewModel` | Valores, selecciones, campos tocados, errores derivados y validez sobreviven a recreaciones de la Activity mientras vive el ViewModel. |
| Regla o estado visual | [`CheckoutScreen`](app/src/main/java/com/example/laboratorio_09_tienda_temtica/ui/screens/CheckoutScreen.kt) | Composable | Conserva foco, teclado, animación y `derivedStateOf` en UI; emite eventos y no duplica validación de negocio. |

## Decisiones de arquitectura

### ¿Por qué los validadores son funciones puras?

Porque el mismo valor siempre produce el mismo resultado, sin depender del ciclo de vida, recursos,
estado de Compose ni `Context`. Esto permite pruebas JVM rápidas, evita fugas de responsabilidades
hacia la UI y mantiene las reglas disponibles para cualquier consumidor Kotlin.

### ¿Qué cambia al pasar de Factura con NIT a CF?

El ViewModel cambia `billingType`, reinicia `isNitTouched` e `isBusinessNameTouched`, y los errores
fiscales derivados pasan a `null`; por ello dejan de mostrarse y de participar en `isFormValid`.
Nombre y teléfono no cambian. Los textos fiscales se conservan internamente. Si se vuelve a NIT,
los dos campos vuelven a participar de inmediato en la validez, pero sus mensajes permanecen ocultos
hasta una nueva edición.

## Comprobaciones manuales

La evidencia de video entregada está en `evidence/lab11-persona2.mp4`. En esta sesión no se
repitieron recorridos interactivos en emulador; por honestidad, no se marcan como aprobados a partir
de inspección estática o pruebas JVM.

| Recorrido | Resultado esperado | Resultado observado en esta sesión | Estado |
|---|---|---|---|
| 1. Formulario inicial | CF, fiscales ocultos, sin errores rojos y confirmar deshabilitado | Arquitectura y pruebas JVM verificadas; recorrido visual no repetido | No ejecutable en esta sesión |
| 2. NIT, validación y rotación | Animación, error/corrección y estado conservado tras rotar | Estado en ViewModel y validación cubiertos; rotación visual no repetida | No ejecutable en esta sesión |
| 3. Limpieza en cascada | CF elimina errores/tocados fiscales; volver a NIT bloquea sin mostrar errores | Comportamiento aprobado por pruebas JVM | No ejecutable visualmente en esta sesión |
| 4. Teclado y radios | Recorridos IME correctos, teclado oculto, fila seleccionable y scroll | Implementación auditada; interacción no repetida | No ejecutable en esta sesión |
| 5. Compra y reinicio | Recibo/folio/total, pedido en cero e historial limpio | Dominio aprobado por pruebas JVM; navegación visual no repetida | No ejecutable visualmente en esta sesión |

## Pruebas ejecutadas

- `testDebugUnitTest`: 44 pruebas, 0 fallos, 0 errores.
- `assembleDebug`: compilación exitosa.
- `lintDebug`: 0 errores. Las advertencias restantes son preexistentes y no pertenecen al checkout
  (versiones disponibles, recursos de plantilla, etiqueta redundante y chequeo de SDK del tema).
- `git diff --check`: sin errores de espacios; Git solo informa la conversión esperada LF→CRLF.

Las tareas se ejecutaron con un directorio de salida temporal porque otro proceso mantenía
bloqueado `app/build` dentro de OneDrive. No se agregaron artefactos generados al repositorio.

## Uso de herramientas de IA

Se utilizó Codex para auditar la rúbrica, contrastar archivos y símbolos reales, completar pruebas,
aplicar ajustes puntuales, ejecutar Gradle y redactar esta descripción. La salida se verificó mediante
pruebas JVM, compilación, lint y revisión del diff. No se usó IA para generar cobros, datos fiscales,
consultas SAT ni persistencia externa.

## Fuentes oficiales

- [`derivedStateOf`](https://developer.android.com/develop/ui/compose/side-effects#derivedstateof)
- [State hoisting](https://developer.android.com/develop/ui/compose/state-hoisting)
- [Conservación del estado](https://developer.android.com/develop/ui/compose/state-saving)
- [Campos de texto y teclado](https://developer.android.com/develop/ui/compose/text/user-input)
- [Manejo del foco](https://developer.android.com/develop/ui/compose/touch-input/focus/change-focus-behavior)
- [Control del teclado](https://developer.android.com/reference/kotlin/androidx/compose/ui/platform/SoftwareKeyboardController)
- [Radios accesibles](https://developer.android.com/develop/ui/compose/components/radio-button)
- [Insets y teclado](https://developer.android.com/develop/ui/compose/system/insets-ui)

## Guion sugerido del video (máximo 3 minutos)

Mostrar un pedido con unidades y total, pulsar **Continuar al checkout** y señalar que inicia en CF,
sin campos fiscales ni errores visibles, con el botón deshabilitado. Editar nombre y teléfono para
enseñar un error y su corrección. Cambiar a Factura con NIT, mostrar la animación, escribir un NIT
inválido y corregirlo. Volver a CF para demostrar la limpieza de errores y regresar a NIT para
mostrar que los datos fiscales inválidos siguen bloqueando sin errores prematuros. Recorrer Nombre →
Teléfono → NIT → Razón social con el teclado, tocar el texto de un radio y comprobar que el teclado
se oculta. Completar el formulario, confirmar, mostrar folio, cliente, facturación, pago y total.
Pulsar **Volver al catálogo**, enseñar el contador en cero, abrir el pedido vacío y pulsar Atrás para
confirmar que Checkout y Confirmación no reaparecen.
