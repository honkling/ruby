package me.honkling.ruby.lib

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val units = mapOf(
    listOf("s", "sec", "second", "seconds") to 1,
    listOf("m", "min", "minute", "minutes") to 60,
    listOf("h", "hour", "hours") to 3600,
    listOf("d", "day", "days") to 86400,
    listOf("w", "week", "weeks") to 604800,
    listOf("mo", "month", "months") to 2592000,
    listOf("y", "year", "years") to 31536000
)

fun parseDuration(input: String): Result<Duration> {
    var index = 0
    var seconds = 0
    val cleanInput = input.replace(" ", "")
        .replace(",", "")

    while (index < cleanInput.length) {
        var rest = cleanInput.substring(index)
        var next = rest.indexOfFirst { !it.isDigit() }
        val number = rest.safeSubstring(0..<next).toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Expected number, found nothing"))

        index += next
        rest = cleanInput.substring(index)
        next = rest.indexOfFirst { it.isDigit() }.takeIf { it != -1 } ?: rest.length
        val unit = rest.safeSubstring(0..<next)
        val unitValue = units.entries.find { unit in it.key }?.value
            ?: return Result.failure(IllegalArgumentException("Expected a valid unit, found '$unit'"))

        index += next
        seconds += number * unitValue
    }

    return Result.success(seconds.toDuration(DurationUnit.SECONDS))
}