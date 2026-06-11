package io.github.debanksahu.vectroidk.quantization

interface Quantization<T : QuantizationConfiguration, O : QuantizationOutput> {

    val config: T

    fun quantize(inputVector: FloatArray): O

    fun calculateSimilarityScore(input1: O, input2: O): Float

}