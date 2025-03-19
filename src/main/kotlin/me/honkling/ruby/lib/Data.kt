package me.honkling.ruby.lib

import java.nio.ByteBuffer
import java.util.*

fun UUID.toByteArray(): ByteArray {
    val buffer = ByteBuffer.wrap(ByteArray(16))
    buffer.putLong(mostSignificantBits)
    buffer.putLong(leastSignificantBits)
    return buffer.array()
}