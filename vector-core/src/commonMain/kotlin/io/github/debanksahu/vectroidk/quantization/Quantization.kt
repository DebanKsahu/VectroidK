package io.github.debanksahu.vectroidk.quantization

interface Quantization<CONFIG_TYPE : QuantizationConfiguration, OUTPUT_TYPE : QuantizationOutput> {

    val config: CONFIG_TYPE

    fun quantize(inputVector: FloatArray): OUTPUT_TYPE

    fun calculateSimilarityScore(input1: OUTPUT_TYPE, input2: OUTPUT_TYPE): Float

    fun convertToQuantizedOutput(inputVector: ByteArray, kwargs: Map<String, Any> = emptyMap()): OUTPUT_TYPE

}