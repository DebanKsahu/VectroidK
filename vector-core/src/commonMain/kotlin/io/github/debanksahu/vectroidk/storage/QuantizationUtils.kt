package io.github.debanksahu.vectroidk.storage

import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString

@Serializable
sealed class QuantizationUtils {

    @Serializable
    object None : QuantizationUtils()

    @Serializable
    data class BlockwiseQuantizationUtils(
        @ByteString val scaleVector: ByteArray
    ) : QuantizationUtils()

}