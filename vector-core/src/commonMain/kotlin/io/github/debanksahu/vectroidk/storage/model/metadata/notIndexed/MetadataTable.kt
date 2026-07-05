package io.github.debanksahu.vectroidk.storage.model.metadata.notIndexed

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import io.github.debanksahu.vectroidk.storage.model.RootTable

/**
 * # About #
 * - This table is for those metadata which are not indexed mean will not
 * be used in filtering (**Metadata Filtering**).
 * - As these metadata not used in filtering purpose they are stored in
 * [String] format while input will be a `Json`.
 * @param embeddingId The id of the embedding this metadata related to.
 * It is also the [androidx.room3.PrimaryKey] of the table.
 * @param metadataBytes The content of metadata stored as [String]
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = RootTable::class,
            parentColumns = ["id"],
            childColumns = ["embeddingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["embeddingId"])
    ]
)
data class MetadataTable(
    @PrimaryKey val embeddingId: Long,
    val metadataBytes: ByteArray
)