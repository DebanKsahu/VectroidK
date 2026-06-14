package io.github.debanksahu.vectroidk.utils.extension

import java.nio.ByteBuffer
import java.nio.ByteOrder

actual fun LongArray.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(this.size * 8).order(ByteOrder.LITTLE_ENDIAN)
    buffer.asLongBuffer().put(this)
    return buffer.array()
}

actual fun IntArray.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(this.size * 4).order(ByteOrder.LITTLE_ENDIAN)
    buffer.asIntBuffer().put(this)
    return buffer.array()
}

actual fun ShortArray.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(this.size * 2).order(ByteOrder.LITTLE_ENDIAN)
    buffer.asShortBuffer().put(this)
    return buffer.array()
}

actual fun FloatArray.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(this.size * 4).order(ByteOrder.LITTLE_ENDIAN)
    buffer.asFloatBuffer().put(this)
    return buffer.array()
}

actual fun ByteArray.toLongArray(): LongArray {
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)
    val longBuffer = buffer.asLongBuffer()
    val longArray = LongArray(longBuffer.remaining())
    longBuffer.get(longArray)
    return longArray
}

actual fun ByteArray.toIntArray(): IntArray {
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)
    val intBuffer = buffer.asIntBuffer()
    val intArray = IntArray(intBuffer.remaining())
    intBuffer.get(intArray)
    return intArray
}

actual fun ByteArray.toShortArray(): ShortArray {
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)
    val shortBuffer = buffer.asShortBuffer()
    val shortArray = ShortArray(shortBuffer.remaining())
    shortBuffer.get(shortArray)
    return shortArray
}

actual fun ByteArray.toFloatArray(): FloatArray {
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)
    val floatBuffer = buffer.asFloatBuffer()
    val floatArray = FloatArray(floatBuffer.remaining())
    floatBuffer.get(floatArray)
    return floatArray
}