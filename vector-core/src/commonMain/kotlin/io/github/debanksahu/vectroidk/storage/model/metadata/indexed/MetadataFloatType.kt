package io.github.debanksahu.vectroidk.storage.model.metadata.indexed

import androidx.room3.Entity
import androidx.room3.ForeignKey
import io.github.debanksahu.vectroidk.storage.model.RootTable

@Entity(
    primaryKeys = ["embeddingId", "keyId"],
    foreignKeys = [
        ForeignKey(
            entity = RootTable::class,
            parentColumns = ["id"],
            childColumns = ["embeddingId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MetadataFloatType(
    val embeddingId: Long,
    val keyId: Int,
    val value: Float,
)

data class MetadataFloatTypeView(
    val keyId: Int,
    val value: Float,
)
