package dev.mskelton.versly.persistence

import org.json.JSONArray

internal fun isPureStructuralNode(nodeType: String): Boolean = nodeType in listOf("s1", "s2", "s3", "ms", "sp", "d", "iex")

internal fun hasSpans(node: JSONArray): Boolean = node.length() > 1 && node.get(1) is JSONArray

/**
 * Filters nodes by verse range. Assumes range is always a subset of the chapter (never full
 * chapter). If range is null in getPassage(), the full chapter is returned without filtering.
 */
internal fun filterNodesByRange(
    chapterData: JSONArray,
    range: List<String>,
    prefix: String,
): MutableList<Node> {
    val startVerse = range.getOrNull(0)?.toIntOrNull() ?: return mutableListOf()
    val endVerse = range.getOrNull(1)?.toIntOrNull() ?: startVerse

    val result = mutableListOf<Node>()
    var currentVerse = 1

    for (i in 0 until chapterData.length()) {
        val node = chapterData.getJSONArray(i)
        val nodeType = node.getString(0)

        if (isPureStructuralNode(nodeType)) {
            val nextVerse = findNextVerse(chapterData, i + 1, currentVerse)
            if (nextVerse in startVerse..endVerse) {
                result.add(Node("$prefix.${i + 1}", node))
            }
            continue
        }

        if (hasSpans(node)) {
            val spans = node.getJSONArray(1)
            val (filteredSpans, lastVerseInRange) =
                filterSpansByRange(spans, startVerse, endVerse, currentVerse)

            if (filteredSpans.isNotEmpty()) {
                if (lastVerseInRange != null) {
                    currentVerse = lastVerseInRange
                }

                val filteredNode =
                    JSONArray().apply {
                        put(0, nodeType)
                        val filteredSpansArray = JSONArray()
                        for (span in filteredSpans) {
                            when (span) {
                                is String -> filteredSpansArray.put(span)
                                is JSONArray -> filteredSpansArray.put(span)
                            }
                        }
                        put(1, filteredSpansArray)
                    }
                result.add(Node("$prefix.${i + 1}", filteredNode))
            } else {
                updateCurrentVerseFromSpans(spans)?.let { currentVerse = it }
            }
        }
    }

    return result
}

internal fun findNextVerse(
    chapterData: JSONArray,
    startIndex: Int,
    defaultVerse: Int,
): Int {
    for (i in startIndex until chapterData.length()) {
        val node = chapterData.getJSONArray(i)
        if (hasSpans(node)) {
            val spans = node.getJSONArray(1)
            for (j in 0 until spans.length()) {
                val span = spans.get(j)
                if (span !is String) {
                    val spanArray = spans.getJSONArray(j)
                    if (spanArray.getString(0) == "v") {
                        return spanArray.getString(1).toIntOrNull() ?: defaultVerse
                    }
                }
            }
        }
    }
    return defaultVerse
}

internal fun updateCurrentVerseFromSpans(spans: JSONArray): Int? {
    for (j in 0 until spans.length()) {
        val span = spans.get(j)
        if (span !is String) {
            val spanArray = spans.getJSONArray(j)
            if (spanArray.getString(0) == "v") {
                return spanArray.getString(1).toIntOrNull()
            }
        }
    }
    return null
}

internal fun filterSpansByRange(
    spans: JSONArray,
    startVerse: Int,
    endVerse: Int,
    initialVerse: Int = 1,
): Pair<List<Any>, Int?> {
    val filtered = mutableListOf<Any>()
    var currentVerse = initialVerse
    var lastVerseInRange: Int? = null

    for (i in 0 until spans.length()) {
        val span = spans.get(i)

        if (span is String) {
            if (currentVerse in startVerse..endVerse) {
                filtered.add(span)
                if (lastVerseInRange == null || currentVerse > lastVerseInRange) {
                    lastVerseInRange = currentVerse
                }
            }
        } else {
            val spanArray = spans.getJSONArray(i)
            val spanType = spanArray.getString(0)

            if (spanType == "v") {
                val verseNum = spanArray.getString(1).toIntOrNull() ?: continue
                currentVerse = verseNum

                when {
                    verseNum < startVerse -> {}

                    verseNum in startVerse..endVerse -> {
                        filtered.add(spanArray)
                        lastVerseInRange = verseNum
                    }

                    else -> {
                        break
                    }
                }
            } else {
                if (currentVerse in startVerse..endVerse) {
                    filtered.add(spanArray)
                    if (lastVerseInRange == null || currentVerse > lastVerseInRange) {
                        lastVerseInRange = currentVerse
                    }
                }
            }
        }
    }

    return Pair(filtered, lastVerseInRange)
}
