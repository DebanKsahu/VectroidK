package io.github.debanksahu.vectroidk.index.flat

import io.github.debanksahu.vectroidk.index.IndexingConfiguration
import io.github.debanksahu.vectroidk.quantization.Quantization
import io.github.debanksahu.vectroidk.quantization.QuantizationConfiguration
import io.github.debanksahu.vectroidk.quantization.QuantizationMethod
import io.github.debanksahu.vectroidk.quantization.QuantizationOutput

data class FlatIndexConfiguration<QUANTIZATION_CONFIG_TYPE : QuantizationConfiguration, QUANTIZATION_OUTPUT_TYPE : QuantizationOutput>(
    override val quantizationTool: Quantization<QUANTIZATION_CONFIG_TYPE, QUANTIZATION_OUTPUT_TYPE>,
    override val quantizationType: QuantizationMethod,
    val topK: Int = 5,
) : IndexingConfiguration
