package io.github.debanksahu.vectroidk.storage.dao

import androidx.room3.*
import io.github.debanksahu.vectroidk.storage.MetadataSerializer
import io.github.debanksahu.vectroidk.storage.model.metadata.MetadataKeyTable
import io.github.debanksahu.vectroidk.storage.model.metadata.indexed.MetadataFloatTypeView
import io.github.debanksahu.vectroidk.storage.model.metadata.indexed.MetadataIntTypeView
import io.github.debanksahu.vectroidk.storage.model.metadata.indexed.MetadataLongTypeView
import io.github.debanksahu.vectroidk.storage.model.metadata.indexed.MetadataTextTypeView
import io.github.debanksahu.vectroidk.storage.model.metadata.notIndexed.MetadataTable

@Dao
interface MetadataDao {
    @Transaction
    suspend fun getAllMetadata(embeddingId: Long, fieldIds: List<Int> = emptyList()): Map<String, Any> {
        val resultMetadata = mutableMapOf<String, Any>()
        val nonIndexedMetadataBytes = getNonIndexedMetadataByEmbeddingId(embeddingId = embeddingId)
        if (nonIndexedMetadataBytes != null) {
            val nonIndexedMetadata = MetadataSerializer.deserialize(nonIndexedMetadataBytes)
            resultMetadata += nonIndexedMetadata
        }

        if (fieldIds.isEmpty()) {
            val indexedTextMetadata = getTextTypeMetadataByEmbeddingId(embeddingId = embeddingId)
            val indexedFloatMetadata = getFloatTypeMetadataByEmbeddingId(embeddingId = embeddingId)
            val indexedIntMetadata = getIntTypeMetadataByEmbeddingId(embeddingId = embeddingId)
            val indexedLongMetadata = getLongTypeMetadataByEmbeddingId(embeddingId = embeddingId)

            for ((keyId, value) in indexedTextMetadata) {
                val keyName = getKeyNameById(keyId = keyId) ?: continue
                resultMetadata[keyName] = value
            }
            for ((keyId, value) in indexedFloatMetadata) {
                val keyName = getKeyNameById(keyId = keyId) ?: continue
                resultMetadata[keyName] = value
            }
            for ((keyId, value) in indexedIntMetadata) {
                val keyName = getKeyNameById(keyId = keyId) ?: continue
                resultMetadata[keyName] = value
            }
            for ((keyId, value) in indexedLongMetadata) {
                val keyName = getKeyNameById(keyId = keyId) ?: continue
                resultMetadata[keyName] = value
            }
        } else {
            val indexedTextMetadata =
                getTextTypeMetadataByEmbeddingIdAndKeyIds(embeddingId = embeddingId, keyIds = fieldIds)
            val indexedFloatMetadata =
                getFloatTypeMetadataByEmbeddingIdAndKeyIds(embeddingId = embeddingId, keyIds = fieldIds)
            val indexedIntMetadata =
                getIntTypeMetadataByEmbeddingIdAndKeyIds(embeddingId = embeddingId, keyIds = fieldIds)
            val indexedLongMetadata =
                getLongTypeMetadataByEmbeddingIdAndKeyIds(embeddingId = embeddingId, keyIds = fieldIds)

            for ((keyId, value) in indexedTextMetadata) {
                val keyName = getKeyNameById(keyId = keyId) ?: continue
                resultMetadata[keyName] = value
            }
            for ((keyId, value) in indexedFloatMetadata) {
                val keyName = getKeyNameById(keyId = keyId) ?: continue
                resultMetadata[keyName] = value
            }
            for ((keyId, value) in indexedIntMetadata) {
                val keyName = getKeyNameById(keyId = keyId) ?: continue
                resultMetadata[keyName] = value
            }
            for ((keyId, value) in indexedLongMetadata) {
                val keyName = getKeyNameById(keyId = keyId) ?: continue
                resultMetadata[keyName] = value
            }
        }

        return resultMetadata
    }

    /*
        Metadata field operations
     */

    suspend fun insertKeyByName(keyName: String): Int {
        return insertKey(key = MetadataKeyTable(name = keyName))
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertKey(key: MetadataKeyTable): Int

    @Query(
        """
            SELECT name FROM MetadataKeyTable
            WHERE id = :keyId
        """
    )
    suspend fun getKeyNameById(keyId: Int): String?

    /*
        Nonindexed metadata operations
     */

    @Query(
        """
            SELECT metadataJson FROM MetadataTable
            WHERE embeddingId = :embeddingId
        """
    )
    suspend fun getNonIndexedMetadataByEmbeddingId(embeddingId: Long): ByteArray?

    @Transaction
    suspend fun insertNonIndexedMetadataByMap(metadata: Map<String, Any>, embeddingId: Long): Long {
        val metadataBytes = MetadataSerializer.serialize(metadataMap = metadata)
        return insertNonIndexedMetadata(
            data = MetadataTable(
                embeddingId = embeddingId,
                metadataBytes = metadataBytes
            )
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNonIndexedMetadata(data: MetadataTable): Long

    /*
        Indexed metadata operations
        - Text type
        - Float type
        - Int type
        - Long type
     */

    @Query(
        """
            SELECT keyId, value FROM IndexedMetadataTextType
            WHERE embeddingId = :embeddingId
        """
    )
    suspend fun getTextTypeMetadataByEmbeddingId(embeddingId: Long): List<MetadataTextTypeView>

    @Query(
        """
            SELECT keyId, value FROM MetadataTextType
            WHERE embeddingId = :embeddingId 
            AND keyId IN (:keyIds)
        """
    )
    suspend fun getTextTypeMetadataByEmbeddingIdAndKeyIds(
        embeddingId: Long,
        keyIds: List<Int>
    ): List<MetadataTextTypeView>

    @Query(
        """
            INSERT INTO MetadataTextType
            (embeddingId, keyId, value) VALUES (:embeddingId, :keyId, :value)
        """
    )
    suspend fun insertTextTypeMetadata(embeddingId: Long, keyId: Int, value: String)

    @Query(
        """
            SELECT keyId, value FROM MetadataFloatType
            WHERE embeddingId = :embeddingId
        """
    )
    suspend fun getFloatTypeMetadataByEmbeddingId(embeddingId: Long): List<MetadataFloatTypeView>

    @Query(
        """
            SELECT keyId, value FROM MetadataFloatType
            WHERE embeddingId = :embeddingId 
            AND keyId IN (:keyIds)
        """
    )
    suspend fun getFloatTypeMetadataByEmbeddingIdAndKeyIds(
        embeddingId: Long,
        keyIds: List<Int>
    ): List<MetadataFloatTypeView>

    @Query(
        """
            INSERT INTO MetadataFloatType
            (embeddingId, keyId, value) VALUES (:embeddingId, :keyId, :value)
        """
    )
    suspend fun insertFloatTypeMetadata(embeddingId: Long, keyId: Int, value: Float)

    @Query(
        """
            SELECT keyId, value FROM MetadataIntType
            WHERE embeddingId = :embeddingId
        """
    )
    suspend fun getIntTypeMetadataByEmbeddingId(embeddingId: Long): List<MetadataIntTypeView>

    @Query(
        """
            SELECT keyId, value FROM MetadataIntType
            WHERE embeddingId = :embeddingId 
            AND keyId IN (:keyIds)
        """
    )
    suspend fun getIntTypeMetadataByEmbeddingIdAndKeyIds(
        embeddingId: Long,
        keyIds: List<Int>
    ): List<MetadataIntTypeView>

    @Query(
        """
            INSERT INTO MetadataIntType
            (embeddingId, keyId, value) VALUES (:embeddingId, :keyId, :value)
        """
    )
    suspend fun insertIntTypeMetadata(embeddingId: Long, keyId: Int, value: Int)

    @Query(
        """
            SELECT keyId, value FROM MetadataLongType
            WHERE embeddingId = :embeddingId
        """
    )
    suspend fun getLongTypeMetadataByEmbeddingId(embeddingId: Long): List<MetadataLongTypeView>

    @Query(
        """
            SELECT keyId, value FROM MetadataLongType
            WHERE embeddingId = :embeddingId 
            AND keyId IN (:keyIds)
        """
    )
    suspend fun getLongTypeMetadataByEmbeddingIdAndKeyIds(
        embeddingId: Long,
        keyIds: List<Int>
    ): List<MetadataLongTypeView>

    @Query(
        """
            INSERT INTO MetadataLongType
            (embeddingId, keyId, value) VALUES (:embeddingId, :keyId, :value)
        """
    )
    suspend fun insertLongTypeMetadata(embeddingId: Long, keyId: Int, value: Long)

}