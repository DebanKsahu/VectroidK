package com.github.debanksahu.vectroidk.index.quantization.blockwise

import com.github.debanksahu.vectroidk.index.quantization.Configuration
import com.github.debanksahu.vectroidk.utils.enums.NormalizationMethods

data class BlockwiseConfiguration(
    val blockSize: Int = 32,
    val normalizationMethod: NormalizationMethods? = null,
): Configuration