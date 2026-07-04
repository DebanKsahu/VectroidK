package io.github.debanksahu.vectroidk.storage.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import io.github.debanksahu.vectroidk.storage.model.metadata.type.MetadataType

@Entity
data class GlobalConfigTable(
    @PrimaryKey val collection: String,
    val dimension: Int,
    val indexedMetadataFields: List<String>,
    val otherMetadataFields: Set<String>,
    val metadataFieldType: Map<String, MetadataType>
)
