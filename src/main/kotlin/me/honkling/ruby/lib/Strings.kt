package me.honkling.ruby.lib

import org.bukkit.map.MinecraftFont
import kotlin.math.max
import kotlin.math.min

private val font = MinecraftFont.Font
private val spaceWidth = font.getWidth(" ")

fun String.clampToLength(length: Int): String {
    if (font.getWidth(this) <= length)
        return this

    var clamped = this
    var currentLength = font.getWidth("$clamped...")

    while (currentLength > length) {
        clamped = clamped.safeSubstring(0..<clamped.length - 1)
        currentLength = font.getWidth("$clamped...")
    }

    return "$clamped..."
}

fun String.center(maxLength: Int): String {
    val length = font.getWidth(this)

    if (length > maxLength)
        return clampToLength(maxLength)

    val space = maxLength - length
    return " ".repeat(max(0, space / 3 / spaceWidth)) + this
}

@Suppress("ReplaceRangeStartEndInclusiveWithFirstLast")
fun String.safeSubstring(range: IntRange): String {
    val isInclusive = range.last == range.endInclusive
    val min = min(range.start, range.endInclusive)
    val max = max(range.start, range.endInclusive)
    val start = max(min, 0)
    val end = min(max(0, length - 1), max)
    return substring(if (isInclusive) start..end else start..<end)
}