package io.github.debanksahu.vectroidk.storage.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import io.github.debanksahu.vectroidk.storage.model.EmbeddingDetailInput
import io.github.debanksahu.vectroidk.storage.model.EmbeddingDetailOutput
import io.github.debanksahu.vectroidk.storage.model.FlatIndexTable
import io.github.debanksahu.vectroidk.storage.model.RootTable

@Dao
interface FlatIndexDao {

    @Query(
        """
            SELECT f.id AS id, f.embedding AS embedding, f.quantizationUtilBytes AS quantizationUtilBytes
            FROM FlatIndexTable AS f
            INNER JOIN RootTable AS r ON r.id = f.id
        """
    )
    suspend fun getAllEmbeddingDetail(): List<EmbeddingDetailOutput>

    @Query(
        """
            SELECT f.id AS id, f.embedding AS embedding, f.quantizationUtilBytes AS quantizationUtilBytes
            FROM FlatIndexTable AS f
            INNER JOIN RootTable AS r ON r.id = f.id
            WHERE r.collection = :collection
        """
    )
    suspend fun getEmbeddingDetailByCollection(collection: String): List<EmbeddingDetailOutput>

    @Query(
        """
            SELECT id FROM RootTable
        """
    )
    suspend fun getAllEmbeddingIds(): List<Long>

    @Query(
        """
            SELECT id FROM RootTable
            WHERE collection = :collection
        """
    )
    suspend fun getEmbeddingIdsByCollection(collectionName: String): List<Long>

    @Transaction
    suspend fun insertEmbeddingDetail(input: EmbeddingDetailInput): Long {
        var embeddingId = insertEmbeddingDetailToRootTable(data = RootTable(collection = input.collection))
        embeddingId = insertEmbeddingDetailToFlatIndexTable(
            data = FlatIndexTable(
                id = embeddingId,
                embedding = input.embedding,
                quantizationUtilBytes = input.quantizationUtilBytes
            )
        )
        return embeddingId
    }

    @Insert
    suspend fun insertEmbeddingDetailToRootTable(data: RootTable): Long

    @Insert
    suspend fun insertEmbeddingDetailToFlatIndexTable(data: FlatIndexTable): Long
}