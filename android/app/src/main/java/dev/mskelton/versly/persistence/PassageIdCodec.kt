package dev.mskelton.versly.persistence

fun encodePassageId(id: PassageId): String {
    val range = id.range?.joinToString("-") ?: "*"
    return "${id.book}.${id.chapter}.$range.${id.translation}"
}

fun decodePassageId(encoded: String): PassageId? {
    val parts = encoded.split('.')
    if (parts.size != 4) {
        return null
    }

    val range =
        if (parts[2] == "*") {
            null
        } else {
            parts[2].split("-").takeIf { it.size == 2 }
        }

    return PassageId(
        book = parts[0],
        chapter = parts[1],
        range = range,
        translation = parts[3],
    )
}
