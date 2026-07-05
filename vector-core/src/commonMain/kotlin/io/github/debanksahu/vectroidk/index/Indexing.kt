package io.github.debanksahu.vectroidk.index

import io.github.debanksahu.vectroidk.index.flat.FlatIndexOutput
import io.github.debanksahu.vectroidk.storage.QuantizationUtils
import io.github.debanksahu.vectroidk.storage.dao.MetadataDao
import io.github.debanksahu.vectroidk.storage.model.GlobalConfigTable
import io.github.debanksahu.vectroidk.storage.model.metadata.type.FloatTypeMetadata
import io.github.debanksahu.vectroidk.storage.model.metadata.type.IntTypeMetadata
import io.github.debanksahu.vectroidk.storage.model.metadata.type.LongTypeMetadata
import io.github.debanksahu.vectroidk.storage.model.metadata.type.TextTypeMetadata
import io.github.debanksahu.vectroidk.utils.extension.toFloatArray

interface Indexing<INDEXING_CONFIG_TYPE : IndexingConfiguration, INDEXING_OUTPUT_TYPE : IndexingOutput, INDEX_DAO_TYPE> {

    val globalConfig: GlobalConfigTable
    val indexConfig: INDEXING_CONFIG_TYPE
    val indexDao: INDEX_DAO_TYPE
    val metadataDao: MetadataDao

    fun build()

    suspend fun search(inputVector: FloatArray): Array<FlatIndexOutput>

    suspend fun insert(inputVector: FloatArray, metadata: Map<String, Any> = emptyMap()): Long

    suspend private fun insertMetadata(metadata: Map<String, Any>, embeddingId: Long) {
        if (metadata.isNotEmpty()) {
            val nonIndexedMetadata = mutableMapOf<String, Any>()

            for (key in globalConfig.indexedMetadataFields) {
                if (metadata.containsKey(key)) {
                    val keyId = metadataDao.insertKeyByName(keyName = key)
                    when (globalConfig.metadataFieldType[key]) {
                        is FloatTypeMetadata if metadata[key] is Float -> {
                            metadataDao.insertFloatTypeMetadata(
                                embeddingId = embeddingId,
                                keyId = keyId,
                                value = metadata[key] as Float
                            )
                        }

                        is IntTypeMetadata if metadata[key] is Int -> {
                            metadataDao.insertIntTypeMetadata(
                                embeddingId = embeddingId,
                                keyId = keyId,
                                value = metadata[key] as Int
                            )
                        }

                        is LongTypeMetadata if metadata[key] is Long -> {
                            metadataDao.insertLongTypeMetadata(
                                embeddingId = embeddingId,
                                keyId = keyId,
                                value = metadata[key] as Long
                            )
                        }

                        is TextTypeMetadata if metadata[key] is String -> {
                            metadataDao.insertTextTypeMetadata(
                                embeddingId = embeddingId,
                                keyId = keyId,
                                value = metadata[key] as String
                            )
                        }
                    }
                }
            }

            for (key in globalConfig.otherMetadataFields) {
                if (metadata[key] != null) {
                    nonIndexedMetadata[key] = metadata[key] as Any
                }
            }

            if (nonIndexedMetadata.isNotEmpty()) {
                metadataDao.insertNonIndexedMetadataByMap(
                    metadata = nonIndexedMetadata.toMap(),
                    embeddingId = embeddingId
                )
            }
        }
    }

    fun buildQuantizationKwargs(input: QuantizationUtils): Map<String, Any> {
        return when (input) {
            is QuantizationUtils.None -> emptyMap()
            is QuantizationUtils.BlockwiseQuantizationUtils -> {
                mapOf(
                    "scaleVector" to input.scaleVector.toFloatArray()
                )
            }
        }
    }

}