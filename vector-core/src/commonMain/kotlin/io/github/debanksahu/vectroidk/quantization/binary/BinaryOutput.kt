package io.github.debanksahu.vectroidk.quantization.binary

import io.github.debanksahu.vectroidk.quantization.QuantizationOutput

data class BinaryOutput(
    override val quantizedOutput: ByteArray,
    val originalSize: Int,
    val blockSize: Int,
    val variation: BinaryVariation
): QuantizationOutput {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BinaryOutput

        return quantizedOutput.contentEquals(other.quantizedOutput)
    }

    override fun hashCode(): Int {
        return quantizedOutput.contentHashCode()
    }
}
