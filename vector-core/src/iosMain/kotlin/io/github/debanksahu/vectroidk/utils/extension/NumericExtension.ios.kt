package io.github.debanksahu.vectroidk.utils.extension

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual fun LongArray.toByteArray(): ByteArray {
    val byteArray = ByteArray(this.size * 8)
    if (this.isEmpty()) return byteArray

    byteArray.usePinned { pinnedBytes ->
        this.usePinned { pinnedLongs ->
            memcpy(
                pinnedBytes.addressOf(0),
                pinnedLongs.addressOf(0),
                (this.size * 8).toULong()
            )
        }
    }

    return byteArray
}

@OptIn(ExperimentalForeignApi::class)
actual fun IntArray.toByteArray(): ByteArray {
    val byteArray = ByteArray(this.size * 4)
    if (this.isEmpty()) return byteArray

    byteArray.usePinned { pinnedBytes ->
        this.usePinned { pinnedInts ->
            memcpy(
                pinnedBytes.addressOf(0),
                pinnedInts.addressOf(0),
                (this.size * 4).toULong()
            )
        }
    }

    return byteArray
}

@OptIn(ExperimentalForeignApi::class)
actual fun ShortArray.toByteArray(): ByteArray {
    val byteArray = ByteArray(this.size * 2)
    if (this.isEmpty()) return byteArray

    byteArray.usePinned { pinnedBytes ->
        this.usePinned { pinnedShorts ->
            memcpy(
                pinnedBytes.addressOf(0),
                pinnedShorts.addressOf(0),
                (this.size * 2).toULong()
            )
        }
    }

    return byteArray
}

@OptIn(ExperimentalForeignApi::class)
actual fun FloatArray.toByteArray(): ByteArray {
    val byteArray = ByteArray(this.size * 4)
    if (this.isEmpty()) return byteArray

    byteArray.usePinned { pinnedBytes ->
        this.usePinned { pinnedFloats ->
            memcpy(
                pinnedBytes.addressOf(0),
                pinnedFloats.addressOf(0),
                (this.size * 4).toULong()
            )
        }
    }

    return byteArray
}

@OptIn(ExperimentalForeignApi::class)
actual fun ByteArray.toLongArray(): LongArray {
    val longArray = LongArray(this.size / 8)
    if (this.isEmpty()) return longArray

    this.usePinned { pinnedBytes ->
        longArray.usePinned { pinnedLongs ->
            memcpy(
                pinnedLongs.addressOf(0),
                pinnedBytes.addressOf(0),
                this.size.toULong()
            )
        }
    }

    return longArray
}

@OptIn(ExperimentalForeignApi::class)
actual fun ByteArray.toIntArray(): IntArray {
    val intArray = IntArray(this.size / 4)
    if (this.isEmpty()) return intArray

    this.usePinned { pinnedBytes ->
        intArray.usePinned { pinnedInts ->
            memcpy(
                pinnedInts.addressOf(0),
                pinnedBytes.addressOf(0),
                this.size.toULong()
            )
        }
    }

    return intArray
}

@OptIn(ExperimentalForeignApi::class)
actual fun ByteArray.toShortArray(): ShortArray {
    val shortArray = ShortArray(this.size / 2)
    if (this.isEmpty()) return shortArray

    this.usePinned { pinnedBytes ->
        shortArray.usePinned { pinnedShorts ->
            memcpy(
                pinnedShorts.addressOf(0),
                pinnedBytes.addressOf(0),
                this.size.toULong()
            )
        }
    }

    return shortArray
}

@OptIn(ExperimentalForeignApi::class)
actual fun ByteArray.toFloatArray(): FloatArray {
    val floatArray = FloatArray(this.size / 4)
    if (this.isEmpty()) return floatArray

    this.usePinned { pinnedBytes ->
        floatArray.usePinned { pinnedFloats ->
            memcpy(
                pinnedFloats.addressOf(0),
                pinnedBytes.addressOf(0),
                this.size.toULong()
            )
        }
    }

    return floatArray
}