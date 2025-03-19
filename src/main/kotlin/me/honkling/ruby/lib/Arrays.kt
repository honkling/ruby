package me.honkling.ruby.lib

import kotlin.math.max
import kotlin.math.min

fun <T> List<T>.nullIfEmpty(): List<T>? {
    if (isEmpty())
        return null

    return this
}

@Suppress("ReplaceRangeStartEndInclusiveWithFirstLast", "DuplicatedCode")
fun <T> List<T>.safeSlice(range: IntRange): List<T> {
    val isInclusive = range.last == range.endInclusive
    val min = min(range.start, range.endInclusive)
    val max = max(range.start, range.endInclusive)
    val start = max(min, 0)
    val end = min(max(0, size - 1), max)
    return slice(if (isInclusive) start..end else start..<end)
}

fun <T> MutableList<T>.removeFirstOrNull(): T? {
    if (isEmpty())
        return null

    return removeFirst()
}