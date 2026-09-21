package com.example.laboratorio_09_tienda_temtica.model

import kotlin.math.roundToInt
import kotlin.random.Random

private const val TARGET_CATALOG_SIZE = 500
private const val CATALOG_SEED = 10_2026
private val validBookId = Regex("[A-Za-z0-9-]+")

private data class BookTheme(
    val genre: String,
    val subjects: List<String>,
    val descriptions: List<String>,
    val basePrice: Double
)
private data class BookFormat(
    val name: String,
    val priceAdjustment: Double
)
private val bookThemes = listOf(
    BookTheme(
        genre = "Fantasía",
        subjects = listOf("el reino de cristal", "la torre olvidada", "los guardianes del bosque"),
        descriptions = listOf(
            "Una aventura fantástica sobre secretos antiguos y decisiones capaces de cambiar un reino.",
            "Un viaje entre magia, lealtad y criaturas que protegen un mundo oculto."
        ),
        basePrice = 105.00
    ),
    BookTheme(
        genre = "Ciencia ficción",
        subjects = listOf("la última colonia", "el archivo de Marte", "la estación del mañana"),
        descriptions = listOf(
            "Una expedición espacial enfrenta un descubrimiento que transforma el futuro de la humanidad.",
            "Tecnología, memoria y supervivencia se cruzan lejos de la Tierra."
        ),
        basePrice = 118.00
    ),
    BookTheme(
        genre = "Misterio",
        subjects = listOf("la biblioteca sellada", "el manuscrito perdido", "la casa del reloj"),
        descriptions = listOf(
            "Una investigación literaria revela pistas escondidas durante varias generaciones.",
            "Un enigma conduce a sus protagonistas por archivos, cartas y secretos familiares."
        ),
        basePrice = 98.00
    ),
    BookTheme(
        genre = "Historia",
        subjects = listOf("las rutas del imperio", "la ciudad colonial", "los cronistas del altiplano"),
        descriptions = listOf(
            "Un recorrido documentado por acontecimientos y personajes que marcaron una época.",
            "Relatos históricos acercan al lector a los cambios sociales de una región."
        ),
        basePrice = 125.00
    ),
    BookTheme(
        genre = "Romance",
        subjects = listOf("las cartas de abril", "un verano en Antigua", "el café de los encuentros"),
        descriptions = listOf(
            "Dos vidas vuelven a encontrarse mientras intentan reconciliar el pasado y el presente.",
            "Una historia íntima sobre afecto, distancia y nuevas oportunidades."
        ),
        basePrice = 92.00
    ),
    BookTheme(
        genre = "Aventura",
        subjects = listOf("la ruta del quetzal", "el mapa del navegante", "la montaña escondida"),
        descriptions = listOf(
            "Una travesía llena de desafíos conduce a un hallazgo inesperado.",
            "Exploradores jóvenes siguen un mapa que pone a prueba su valor y amistad."
        ),
        basePrice = 102.00
    )
)
private val bookFormats = listOf(
    BookFormat("Tapa blanda", 0.0),
    BookFormat("Tapa dura", 35.0),
    BookFormat("Edición ilustrada", 58.0)
)
private val titleEndings = listOf(
    "Voces del horizonte",
    "Crónicas de ceniza",
    "El secreto de la memoria",
    "Historias de medianoche",
    "Bajo un cielo distante",
    "El comienzo del viaje",
    "Sombras del pasado",
    "La promesa del amanecer",
    "Ecos de otro tiempo",
    "Los caminos del destino",
    "Una historia inesperada",
    "El umbral de los sueños",
    "Cartas desde el silencio",
    "La búsqueda imposible",
    "Donde nacen las leyendas",
    "El último descubrimiento",
    "Memorias de una aventura",
    "Más allá de las montañas",
    "El rumor de las estrellas",
    "La huella de los viajeros",
    "Secretos entre páginas",
    "El lenguaje del tiempo",
    "Una luz en la distancia",
    "Relatos de un mundo perdido",
    "La puerta de los recuerdos",
    "El regreso de los guardianes",
    "Las señales del camino",
    "El misterio de las palabras"
)

private val titleSeeds = bookThemes.flatMap { theme ->
    theme.subjects.map { subject -> theme to subject }
}

fun stableBookCoverUrl(bookId: String): String {
    require(bookId.matches(validBookId)) {
        "El ID del libro solo puede contener letras, números y guiones."
    }
    return "https://picsum.photos/seed/$bookId/320/480"
}

fun generateBookCatalog(
    originalBooks: List<Books>,
    authorProfiles: List<AuthorProfile>
): List<Books> {
    require(originalBooks.size <= TARGET_CATALOG_SIZE) {
        "El catálogo original no puede superar $TARGET_CATALOG_SIZE libros."
    }
    require(originalBooks.map { it.id }.distinct().size == originalBooks.size) {
        "Los libros originales deben tener IDs únicos."
    }
    require(authorProfiles.isNotEmpty()) {
        "Debe existir al menos un perfil de autor."
    }
    val knownAuthorIds = authorProfiles.map { it.id }.toSet()
    require(originalBooks.all { it.authorId in knownAuthorIds }) {
        "Cada libro original debe apuntar a un perfil de autor existente."
    }
    val random = Random(CATALOG_SEED)
    val generatedCount = TARGET_CATALOG_SIZE - originalBooks.size
    val generatedBooks = List(generatedCount) { offset ->
        val sequence = offset + 1
        val (theme, subject) = titleSeeds[offset % titleSeeds.size]
        val titleEnding = titleEndings[offset / titleSeeds.size]
        val format = bookFormats[random.nextInt(bookFormats.size)]
        val description = theme.descriptions[random.nextInt(theme.descriptions.size)]
        val author = authorProfiles[random.nextInt(authorProfiles.size)]
        val stock = when {
            sequence % 17 == 0 -> 0
            sequence % 11 == 0 -> 3
            else -> random.nextInt(from = 1, until = 13)
        }
        val price = ((theme.basePrice + format.priceAdjustment + random.nextInt(0, 16)) * 100)
            .roundToInt() / 100.0
        val id = "generated-book-${sequence.toString().padStart(3, '0')}"
        Books(
            id = id,
            title = "${subject.replaceFirstChar { it.uppercase() }}: $titleEnding",
            description = description,
            price = price,
            authorId = author.id,
            details = "${author.name} · ${theme.genre} · ${format.name}",
            genre = theme.genre,
            format = format.name,
            stock = stock,
            imageUrl = stableBookCoverUrl(id)
        )
    }
    val completeCatalog = originalBooks + generatedBooks
    validateCatalog(
        catalog = completeCatalog,
        knownAuthorIds = knownAuthorIds
    )
    return completeCatalog
}
private fun validateCatalog(
    catalog: List<Books>,
    knownAuthorIds: Set<String>
) {
    require(catalog.size == TARGET_CATALOG_SIZE) {
        "El catálogo debe contener exactamente $TARGET_CATALOG_SIZE libros."
    }
    require(catalog.all { it.id.matches(validBookId) }) {
        "Todos los IDs deben ser aptos para utilizarse en una URL."
    }
    require(catalog.map { it.id }.distinct().size == catalog.size) {
        "Todos los libros deben tener un ID único."
    }
    require(catalog.map { it.title }.distinct().size == catalog.size) {
        "Todos los libros deben tener un título único."
    }
    require(catalog.all { it.price > 0.0 }) {
        "Todos los precios deben ser positivos."
    }
    require(catalog.all { it.stock >= 0 }) {
        "Las existencias no pueden ser negativas."
    }
    require(catalog.any { it.stock == 0 }) {
        "El catálogo necesita al menos un libro agotado."
    }
    require(catalog.any { it.stock == 3 }) {
        "El catálogo necesita al menos un libro con stock 3."
    }
    require(catalog.any { it.stock > 3 }) {
        "El catálogo necesita al menos un libro con stock mayor que 3."
    }
    require(catalog.all { it.authorId in knownAuthorIds }) {
        "Cada libro debe apuntar a un perfil de autor existente."
    }
    require(catalog.all { it.imageUrl == stableBookCoverUrl(it.id) }) {
        "Cada portada debe derivarse del ID estable del libro."
    }
}

