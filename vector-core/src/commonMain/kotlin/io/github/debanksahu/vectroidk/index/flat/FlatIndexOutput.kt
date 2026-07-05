package io.github.debanksahu.vectroidk.index.flat

import io.github.debanksahu.vectroidk.index.IndexingOutput

data class FlatIndexOutput(
    val metadata: Map<String, Any>,
) : IndexingOutput
