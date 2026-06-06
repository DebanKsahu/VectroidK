package com.github.debanksahu.vectroidk.index.quantization.binary

import com.github.debanksahu.vectroidk.index.quantization.Configuration

data class BinaryConfiguration(
    val blockSize: Int = 32,
    val variation: BinaryVariation = BinaryVariation.MEAN_THRESHOLD
): Configuration
