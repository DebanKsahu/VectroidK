package io.github.debanksahu.vectroidk.storage

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

object QuantizationUtilSerializer {

    @OptIn(ExperimentalSerializationApi::class)
    private val cbor = Cbor {
        ignoreUnknownKeys = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun serialize(inputData: QuantizationUtils): ByteArray {
        return cbor.encodeToByteArray(inputData)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun deserialize(inputData: ByteArray): QuantizationUtils {
        return cbor.decodeFromByteArray(inputData)
    }
}