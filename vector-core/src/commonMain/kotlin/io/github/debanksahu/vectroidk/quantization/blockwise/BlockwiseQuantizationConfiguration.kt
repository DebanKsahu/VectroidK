package io.github.debanksahu.vectroidk.quantization.blockwise

import io.github.debanksahu.vectroidk.quantization.QuantizationConfiguration
import io.github.debanksahu.vectroidk.utils.enums.NormalizationMethod

data class BlockwiseQuantizationConfiguration(
    val inputSize: Int = 256,
    val blockSize: Int = 32,
    val normalizationMethod: NormalizationMethod? = null,
) : QuantizationConfiguration