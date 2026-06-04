package com.github.debanksahu.vectroidk.index.quantization

interface Quantization<T : Configuration, O: QuantizationOutput> {

    val config: T

    fun quantize(inputVector: FloatArray): O

    fun calculateSimilarityScore(input1: O, input2: O): Float

}