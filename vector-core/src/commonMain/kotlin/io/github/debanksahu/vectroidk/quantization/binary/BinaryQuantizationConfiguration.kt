package io.github.debanksahu.vectroidk.quantization.binary

import io.github.debanksahu.vectroidk.quantization.QuantizationConfiguration

data class BinaryQuantizationConfiguration(
    val blockSize: Int = 32,
    val variation: BinaryVariation = BinaryVariation.MEAN_THRESHOLD
) : QuantizationConfiguration
