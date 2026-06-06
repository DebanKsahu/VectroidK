package com.github.debanksahu.vectroidk.index.quantization.binary

import com.github.debanksahu.vectroidk.index.quantization.QuantizationOutput

data class BinaryOutput(
    val quantizedOutput: LongArray,
    val originalSize: Int,
    val blockSize: Int,
    val variation: BinaryVariation
): QuantizationOutput {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BinaryOutput

        if (!quantizedOutput.contentEquals(other.quantizedOutput)) return false

        return true
    }

    override fun hashCode(): Int {
        return quantizedOutput.contentHashCode()
    }
}
