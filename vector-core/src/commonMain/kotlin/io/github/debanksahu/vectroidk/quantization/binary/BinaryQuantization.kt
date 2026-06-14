package io.github.debanksahu.vectroidk.quantization.binary

import io.github.debanksahu.vectroidk.quantization.Quantization
import io.github.debanksahu.vectroidk.utils.extension.toLongArray
import kotlin.math.min

/**
 * # About #
 * This class provide utils for **Binary Quantization** algorithm
 */
class BinaryQuantization(
    override val config: BinaryQuantizationConfiguration
) : Quantization<BinaryQuantizationConfiguration, BinaryOutput> {

    init {
        require(config.blockSize > 0) { "blockSize must be 0 or greater" }
        require(config.inputSize > 0) { "input size must be 0 or greater" }
    }

    /**
     * # About #
     * - This function quantize the input [FloatArray] to a **BinaryArray** but stored in a
     * [LongArray] using **Bit Masking**
     * @param inputVector The input embedding vector
     * @return This return a [BinaryOutput] object which contain necessary
     * values, which required in future operations like **similarity score calculation**,
     * **dequantization** etc.
     */
    override fun quantize(inputVector: FloatArray): BinaryOutput {
        require(inputVector.size == config.inputSize) { "input vectors size must be equal to ${config.inputSize}" }

        val thresholdArr = calculateThreshold(inputVector = inputVector)
        val outputVector = LongArray((inputVector.size + 63) / 64, init = { 0 })

        for (i in inputVector.indices) {
            if (inputVector[i] > thresholdArr[i / config.blockSize]) {
                val longPos = i / 64
                val bitPos = i % 64
                outputVector[longPos] = outputVector[longPos] or (1L shl bitPos)
            }
        }

        return BinaryOutput(
            quantizedOutput = outputVector,
            originalSize = inputVector.size,
            blockSize = config.blockSize,
            variation = config.variation
        )
    }

    /**
     *  # About #
     *  - This function calculate similarity score between 2 vector. Internally it uses hamming distance to calculate
     *  the cosine similarity.
     *
     *  @param input1 This is of type [BinaryOutput] which contain details like quantized vector,
     *  scale vector, block size used etc. details which is required for this calculation
     *  @param input2 Similar to [input1]
     */
    override fun calculateSimilarityScore(input1: BinaryOutput, input2: BinaryOutput): Float {
        require(input1.originalSize == config.inputSize) {
            "The input(1) vectors original size does not match with ${config.inputSize}"
        }

        require(input2.originalSize == config.inputSize) {
            "The input(2) vectors original size does not match with ${config.inputSize}"
        }

        require(input1.blockSize == input2.blockSize) {
            "The input vectors block size does not match"
        }

        require(input1.variation == input2.variation) {
            "The input vectors quantized with different variations"
        }

        val hammingDistance = calculateHammingDistance(input1, input2)
        return 1 - ((2f * hammingDistance) / (input1.originalSize))
    }

    /**
     * # About #
     * This function convert the stored embedding (in form of [ByteArray]) to [BinaryOutput]
     * which is used as input type in other functions like **calculateSimilarityScore**.
     * @param inputVector The stored embedding
     * @param kwargs Additional keyword arguments. Supported keys includes:
     * - None (All parameter required apart from [inputVector] available in [config])
     * @return Return a [BinaryOutput] object.
     */
    override fun convertToQuantizedOutput(inputVector: ByteArray, kwargs: Map<String, Any>): BinaryOutput {
        val outputVector = inputVector.toLongArray()
        return BinaryOutput(
            quantizedOutput = outputVector,
            originalSize = config.inputSize,
            blockSize = config.blockSize,
            variation = config.variation
        )
    }

    /**
     * # About #
     * This function calculate threshold for binary quantization
     * based on which type of variation the user using
     * @param inputVector The input embedding vector.
     * @return This return a [FloatArray] because threshold calculation happen blockwise
     * mean each block will get separate threshold.
     */
    private fun calculateThreshold(inputVector: FloatArray): FloatArray {
        val numBlocks = (inputVector.size + config.blockSize - 1) / config.blockSize
        val outputVector = FloatArray(size = numBlocks, init = { 0f })
        var start = 0
        when (config.variation) {
            BinaryVariation.ZERO_THRESHOLD -> {}
            BinaryVariation.MEAN_THRESHOLD -> {
                var currSum = 0f
                for (i in inputVector.indices) {
                    currSum += inputVector[i]
                    if (i == inputVector.size - 1 || (i - start + 1 == config.blockSize)) {
                        outputVector[start / config.blockSize] = currSum / min(config.blockSize, i - start + 1)
                        start = i + 1
                        currSum = 0f
                    }
                }
            }
        }
        return outputVector
    }

    /**
     * # About #
     * This function calculate the **hamming distance** between 2 **binary quantized vector**
     * @param input1 The first input
     * @param input2 Same as [input1]
     * @return This function return an integer, which represent the hamming distance
     * (Number of different set bits)
     */
    private fun calculateHammingDistance(input1: BinaryOutput, input2: BinaryOutput): Int {
        var distance = 0
        for (i in input1.quantizedOutput.indices) {
            distance += (input1.quantizedOutput[i] xor input2.quantizedOutput[i]).countOneBits()
        }
        return distance
    }

}