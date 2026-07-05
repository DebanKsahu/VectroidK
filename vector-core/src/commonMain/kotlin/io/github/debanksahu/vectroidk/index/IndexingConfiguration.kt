package io.github.debanksahu.vectroidk.index

import io.github.debanksahu.vectroidk.quantization.Quantization
import io.github.debanksahu.vectroidk.quantization.QuantizationMethod

interface IndexingConfiguration {
    val quantizationTool: Quantization<*, *>
    val quantizationType: QuantizationMethod
    val collection: String
}