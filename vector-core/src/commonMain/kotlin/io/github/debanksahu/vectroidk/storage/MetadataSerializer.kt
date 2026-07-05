package io.github.debanksahu.vectroidk.storage

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object MetadataSerializer {
    @OptIn(ExperimentalSerializationApi::class)
    fun serialize(metadataMap: Map<String, Any>): ByteArray {
        return Cbor.encodeToByteArray(serializer = MetadataMapSerializer, value = metadataMap)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun deserialize(metadataBytes: ByteArray): Map<String, Any> {
        return Cbor.decodeFromByteArray(deserializer = MetadataMapSerializer, bytes = metadataBytes)
    }
}

object MetadataMapSerializer : KSerializer<Map<String, Any>> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("MetadataSerializer")

    @Suppress("UNCHECKED_CAST")
    override fun serialize(
        encoder: Encoder,
        value: Map<String, Any>
    ) {
        val composite = encoder.beginCollection(descriptor, value.size * 2)
        var index = 0

        for ((key, item) in value) {
            composite.encodeStringElement(descriptor, index++, key)
            when (item) {

                is String -> composite.encodeStringElement(descriptor, index++, item)
                is Int -> composite.encodeIntElement(descriptor, index++, item)
                is Long -> composite.encodeLongElement(descriptor, index++, item)
                is Boolean -> composite.encodeBooleanElement(descriptor, index++, item)
                is Double -> composite.encodeDoubleElement(descriptor, index++, item)
                is Float -> composite.encodeFloatElement(descriptor, index++, item)

                is Map<*, *> -> {
                    // Recursive call for nested maps
                    composite.encodeSerializableElement(
                        descriptor, index++, MetadataMapSerializer, item as Map<String, Any>
                    )
                }

                is List<*> -> {
                    // Call a helper to stream lists without allocation
                    composite.encodeSerializableElement(
                        descriptor, index++, MetadataListSerializer, item as List<Any>
                    )
                }

                else -> throw IllegalArgumentException("Unsupported type for metadata: ${item::class}")
            }
        }
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Map<String, Any> {
        val composite = decoder.beginStructure(descriptor)
        val resultMap = mutableMapOf<String, Any>()

        while (true) {
            val index = composite.decodeElementIndex(descriptor)
            if (index == CompositeDecoder.DECODE_DONE) break

            // In a map, even indices are always String Keys
            val key = composite.decodeStringElement(descriptor, index)

            // Decode the corresponding value (odd index)
            val valueIndex = composite.decodeElementIndex(descriptor)
            val value = decodeDynamicValue(composite, valueIndex)

            resultMap[key] = value
        }

        composite.endStructure(descriptor)
        return resultMap
    }

    // Safely deduce native types using the Decoder structure features
    private fun decodeDynamicValue(composite: CompositeDecoder, index: Int): Any {
        return try {
            composite.decodeStringElement(descriptor, index)
        } catch (_: Exception) {
            try {
                composite.decodeIntElement(descriptor, index)
            } catch (_: Exception) {
                try {
                    composite.decodeLongElement(descriptor, index)
                } catch (_: Exception) {
                    try {
                        composite.decodeDoubleElement(descriptor, index)
                    } catch (_: Exception) {
                        try {
                            composite.decodeBooleanElement(descriptor, index)
                        } catch (_: Exception) {
                            try {
                                // Attempt nested map decoding
                                composite.decodeSerializableElement(descriptor, index, MetadataMapSerializer)
                            } catch (_: Exception) {
                                // Attempt nested list decoding
                                composite.decodeSerializableElement(descriptor, index, MetadataListSerializer)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Accompanying List Serializer to maintain low allocation pipeline
object MetadataListSerializer : KSerializer<List<Any>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("DirectCborList")

    override fun serialize(encoder: Encoder, value: List<Any>) {
        val composite = encoder.beginCollection(descriptor, value.size)
        value.forEachIndexed { idx, item ->
            when (item) {
                is String -> composite.encodeStringElement(descriptor, idx, item)
                is Int -> composite.encodeIntElement(descriptor, idx, item)
                is Boolean -> composite.encodeBooleanElement(descriptor, idx, item)
                is Double -> composite.encodeDoubleElement(descriptor, idx, item)
                is Map<*, *> -> composite.encodeSerializableElement(
                    descriptor,
                    idx,
                    MetadataMapSerializer,
                    item as Map<String, Any>
                )

                else -> throw IllegalArgumentException("Unsupported list item type: ${item::class}")
            }
        }
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): List<Any> {
        val composite = decoder.beginStructure(descriptor)
        val resultList = mutableListOf<Any>()
        var index = 0
        while (true) {
            val elementIndex = composite.decodeElementIndex(descriptor)
            if (elementIndex == CompositeDecoder.DECODE_DONE) break
            // Reuse the same safe dynamic parsing block
            resultList.add(
                try {
                    composite.decodeStringElement(descriptor, elementIndex)
                } catch (_: Exception) {
                    try {
                        composite.decodeIntElement(descriptor, elementIndex)
                    } catch (_: Exception) {
                        composite.decodeBooleanElement(descriptor, elementIndex)
                    }
                }
            )
            index++
        }
        composite.endStructure(descriptor)
        return resultList
    }
}