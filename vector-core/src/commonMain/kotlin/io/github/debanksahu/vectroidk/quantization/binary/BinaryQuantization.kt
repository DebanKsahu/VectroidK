package io.github.debanksahu.vectroidk.quantization.binary

import io.github.debanksahu.vectroidk.quantization.Quantization
import kotlin.math.min

class BinaryQuantization(
    override val config: BinaryQuantizationConfiguration
) : Quantization<BinaryQuantizationConfiguration, BinaryOutput> {

    init {
        require(config.blockSize>0) { "blockSize must be 0 or greater" }
    }

    /**
     *
     */
    override fun quantize(inputVector: FloatArray): BinaryOutput {
        require(inputVector.isNotEmpty()) {
            "input vector is empty"
        }

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
     *
     */
    override fun calculateSimilarityScore(input1: BinaryOutput, input2: BinaryOutput): Float {
        require(input1.originalSize == input2.originalSize) {
            "Original size of input1 and input2 is mismatched"
        }

        require(input1.blockSize == input2.blockSize) {
            "Different block sizes is used for quantization of input1 and input2"
        }

        require(input1.variation == input2.variation) {
            "Different variations are used for quantization of input1 and input2"
        }

        val hammingDistance = calculateHammingDistance(input1, input2)
        return 1 - ((2f * hammingDistance) / (input1.originalSize))
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
     *
     */
    private fun calculateHammingDistance(input1: BinaryOutput, input2: BinaryOutput): Int {
        var distance = 0
        for (i in input1.quantizedOutput.indices) {
            distance += (input1.quantizedOutput[i] xor input2.quantizedOutput[i]).countOneBits()
        }
        return distance
    }

}