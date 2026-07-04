package io.github.debanksahu.vectroidk.storage.model

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(indices = [Index(value = ["collection"])])
data class RootTable(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val collection: String,
)
