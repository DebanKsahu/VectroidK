package io.github.debanksahu.vectroidk.storage.model.metadata

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class MetadataKeyTable(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
