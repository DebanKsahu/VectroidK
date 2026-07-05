package io.github.debanksahu.vectroidk.storage.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = RootTable::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FlatIndexTable(
    @PrimaryKey val id: Long,
    val embedding: ByteArray,
    val quantizationUtilBytes: ByteArray
)