package io.github.debanksahu.vectroidk.storage.model

data class EmbeddingDetailOutput(
    val id: Long,
    val embedding: ByteArray,
    val quantizationUtilBytes: ByteArray
)

data class EmbeddingDetailInput(
    val collection: String,
    val embedding: ByteArray,
    val quantizationUtilBytes: ByteArray,
)
