package io.github.debanksahu.vectroidk.quantization.blockwise

import io.github.debanksahu.vectroidk.quantization.QuantizationOutput

data class BlockwiseOutput(
    val quantizedOutput: ByteArray,
    val scaleVector: FloatArray,
    val originalSize: Int,
    val blockSize: Int
) : QuantizationOutput {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BlockwiseOutput

        if (originalSize != other.originalSize) return false
        if (blockSize != other.blockSize) return false
        if (!quantizedOutput.contentEquals(other.quantizedOutput)) return false
        if (!scaleVector.contentEquals(other.scaleVector)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = originalSize
        result = 31 * result + blockSize
        result = 31 * result + quantizedOutput.contentHashCode()
        result = 31 * result + scaleVector.contentHashCode()
        return result
    }
}
