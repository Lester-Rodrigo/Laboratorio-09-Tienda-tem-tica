# Contrato de MockAPI para `orders`

Este documento define el contrato remoto del recurso `orders` del Laboratorio 13.
La estructura se verificó contra respuestas reales de MockAPI el 8 de octubre de 2026.

## URL base

La URL base canónica es:

```text
https://6ac7e57875a4ce3fe7225859.mockapi.io/api/v1/
```

Debe conservar la `/` final. El recurso completo está disponible en:

```text
https://6ac7e57875a4ce3fe7225859.mockapi.io/api/v1/orders
```

## Rutas utilizadas

| Método | Ruta relativa | Respuesta esperada | Propósito |
|---|---|---|---|
| `POST` | `orders` | `201 Created` y un pedido completo | Crear un pedido; MockAPI asigna el `id` |
| `GET` | `orders?sortBy=createdAt&order=desc` | `200 OK` y una lista de pedidos | Obtener los pedidos del más reciente al más antiguo |
| `GET` | `orders/{id}` | `200 OK` y un pedido completo | Obtener un pedido mediante su ID textual |

La aplicación no genera IDs remotos. El campo `id` solo aparece en la representación
devuelta por MockAPI.

## Estructura del pedido

| Campo | Tipo JSON | Obligatorio | Reglas |
|---|---|---|---|
| `id` | String | Solo en respuestas | ID textual asignado por MockAPI |
| `createdAt` | String | Sí | Fecha y hora ISO 8601 en UTC |
| `billingType` | String | Sí | `CF` o `NIT` |
| `total` | String | Sí | Importe decimal fijo con dos posiciones |
| `lines` | Array de objetos | Sí | Instantáneas de los libros comprados |
| `paymentMethod` | String | Sí | `CASH_ON_DELIVERY` o `BANK_TRANSFER` |

El arreglo `lines` debe contener al menos una línea. El `total` debe coincidir con la
suma de los subtotales de todas las líneas.

## Estructura de cada línea

| Campo | Tipo JSON | Obligatorio | Reglas |
|---|---|---|---|
| `bookId` | String | Sí | ID estable del libro |
| `title` | String | Sí | Título guardado como instantánea histórica |
| `unitPrice` | String | Sí | Precio unitario al confirmar la compra |
| `quantity` | Number entero | Sí | Mayor que cero |
| `subtotal` | String | Sí | `unitPrice` multiplicado por `quantity` |

Guardar el ID, título y precio en cada línea permite reconstruir un pedido histórico
sin depender del catálogo actual.

## Representación monetaria

Todos los importes remotos son cadenas decimales sin símbolo de moneda, con punto como
separador y exactamente dos posiciones, por ejemplo `"45.00"` o `"125.50"`.

Esta representación evita introducir `Double` como fuente de verdad y permite convertir
los valores de forma explícita hacia y desde `BigDecimal` sin perder precisión. En el
futuro mapeo se debe validar que:

- `unitPrice`, `subtotal` y `total` sean decimales válidos no negativos.
- Cada importe tenga escala de dos posiciones antes de enviarse.
- `subtotal` sea igual a `unitPrice * quantity`.
- `total` sea igual a la suma de los subtotales.

## Representación de fecha

`createdAt` usa texto ISO 8601 con milisegundos y zona UTC:

```text
2026-10-08T18:54:00.000Z
```

Las consultas de lista solicitan al servidor ordenar por este campo en forma descendente
mediante `sortBy=createdAt&order=desc`.

## Tratamiento de datos personales

El contrato no incluye nombre, teléfono, NIT, razón social, credenciales, cuentas
bancarias ni datos de tarjetas. Aunque algunos de esos valores existan en el formulario
local, no deben formar parte del cuerpo remoto. Los ejemplos contienen únicamente datos
ficticios de libros.

## Respuesta real de `POST /orders`

MockAPI respondió `201 Created` con el siguiente JSON:

```json
{
  "createdAt": "2026-10-08T18:54:00.000Z",
  "billingType": "CF",
  "total": "125.50",
  "lines": [
    {
      "bookId": "book-001",
      "title": "Cien años de soledad",
      "unitPrice": "45.00",
      "quantity": 2,
      "subtotal": "90.00"
    },
    {
      "bookId": "book-002",
      "title": "El principito",
      "unitPrice": "35.50",
      "quantity": 1,
      "subtotal": "35.50"
    }
  ],
  "paymentMethod": "CASH_ON_DELIVERY",
  "id": "1"
}
```

## Respuesta real de `GET /orders`

La consulta del recurso respondió `200 OK` con el siguiente JSON:

```json
[
  {
    "createdAt": "2026-10-08T18:54:00.000Z",
    "billingType": "CF",
    "total": "125.50",
    "lines": [
      {
        "bookId": "book-001",
        "title": "Cien años de soledad",
        "unitPrice": "45.00",
        "quantity": 2,
        "subtotal": "90.00"
      },
      {
        "bookId": "book-002",
        "title": "El principito",
        "unitPrice": "35.50",
        "quantity": 1,
        "subtotal": "35.50"
      }
    ],
    "paymentMethod": "CASH_ON_DELIVERY",
    "id": "1"
  }
]
```

## Validación del ejemplo real

- El ID remoto es el texto `"1"` y fue asignado por MockAPI.
- La fecha usa el formato ISO 8601 acordado.
- Las dos líneas contienen una instantánea suficiente para reconstruir el pedido.
- Los subtotales son `45.00 * 2 = 90.00` y `35.50 * 1 = 35.50`.
- El total es `90.00 + 35.50 = 125.50`.
- Todos los importes conservan dos posiciones decimales.
- No se envían datos personales ni financieros.
