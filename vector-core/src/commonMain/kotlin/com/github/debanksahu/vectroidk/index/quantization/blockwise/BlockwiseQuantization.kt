package com.github.debanksahu.vectroidk.index.quantization.blockwise

import com.github.debanksahu.vectroidk.index.quantization.Quantization
import com.github.debanksahu.vectroidk.utils.enums.NormalizationMethods
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 *
 */
class BlockwiseQuantization(
    override val config: BlockwiseConfiguration
) : Quantization<BlockwiseConfiguration, BlockwiseOutput> {

    /**
     * # About #
     * - This function quantize the input [FloatArray] to IntX (X = 8,16,32 Based on requirement)
     * @param inputVector The input embedding vector
     * @return This return a [BlockwiseOutput] object which contain necessary
     * values, which required in future operations like **similarity score calculation**,
     * **dequantization** etc.
     */
    override fun quantize(inputVector: FloatArray): BlockwiseOutput {
        val inputSize = inputVector.size
        val numberOfBlocks = (inputSize + config.blockSize - 1) / config.blockSize
        val outputVector = ByteArray(inputSize, init = { 0 })
        val scaleVector = FloatArray(size = numberOfBlocks, init = { 0f })
        applyQuantization(inputVector = inputVector, outputVector = outputVector, scaleVector = scaleVector)
        return BlockwiseOutput(
            quantizedOutput = outputVector,
            scaleVector = scaleVector,
            originalSize = inputSize,
            blockSize = config.blockSize
        )
    }

    /**
     * # About #
     * - This function take the input array, perform necessary computation based on the normalization
     * technique and change the output array and scale array inplace.
     * @param inputVector The input embedding vector
     * @param outputVector The placeholder for our final quantized output vector
     * @param scaleVector The placeholder for our block level scale
     */
    private fun applyQuantization(inputVector: FloatArray, outputVector: ByteArray, scaleVector: FloatArray) {
        val inputSize = inputVector.size
        var start = 0
        var currMaxAbs = 0.0f
        var currScale: Float
        when (config.normalizationMethod) {
            null -> {
                for (end in 0..<inputSize) {
                    currMaxAbs = max(currMaxAbs, abs(inputVector[end]))
                    if (end - start + 1 == config.blockSize || end == inputSize - 1) {
                        currScale = if (currMaxAbs != 0f) currMaxAbs / 127 else 1f
                        scaleVector[start / config.blockSize] = currScale
                        while (start <= end) {
                            outputVector[start] =
                                (inputVector[start] / currScale).roundToInt().coerceIn(-127, 127).toByte()
                            start++
                        }
                        currMaxAbs = 0.0f
                    }
                }
            }

            NormalizationMethods.L2_NORMALIZATION -> {
                var squareSum = 0.0
                for (inputNum in inputVector) {
                    squareSum += inputNum.toDouble() * inputNum.toDouble()
                }
                val norm = sqrt(squareSum).toFloat()
                if (norm == 0f) return

                for (end in 0..<inputSize) {
                    currMaxAbs = max(currMaxAbs, abs(inputVector[end]))
                    if (end - start + 1 == config.blockSize || end == inputSize - 1) {
                        currScale = if (currMaxAbs != 0f) (currMaxAbs / norm) / 127 else 1f
                        scaleVector[start / config.blockSize] = currScale
                        while (start <= end) {
                            outputVector[start] =
                                ((inputVector[start] / norm) / currScale).roundToInt().coerceIn(-127, 127).toByte()
                            start++
                        }
                        currMaxAbs = 0.0f
                    }
                }
            }
        }
    }

    /**
     *  # About #
     *  - This function calculate similarity score between 2 vector. Internally it uses dot product to calculate
     *  the cosine similarity, considering the quantized vectors are normalized.
     *
     *  # Suggestions #
     *  - Make sure your input vectors are normalized because this function use dot product for fast
     *  similarity calculation instead of original cosine similarity formula, and normalized vectors
     *  make sure that the results were close to cosine similarity.
     *
     *  @param input1 This is of type [BlockwiseOutput] which contain details like quantized vector,
     *  scale vector, block size used etc. details which is required for this calculation
     *  @param input2 Similar to [input1]
     */
    override fun calculateSimilarityScore(
        input1: BlockwiseOutput,
        input2: BlockwiseOutput
    ): Float {
        // Pre requirements for the function
        require(input1.originalSize==input2.originalSize) {
            "The input vectors original size does match"
        }
        require(input1.blockSize==input2.blockSize) {
            "The input vectors block size does match"
        }

        var similarityScore = 0.0f
        for (i in input1.scaleVector.indices) {
            val scaleMul = input1.scaleVector[i] * input2.scaleVector[i]
            val start = i * input1.blockSize
            val end = min(((i + 1) * input1.blockSize) - 1, input1.originalSize-1)

            var currSum = 0
            for (j in start..end) {
                currSum += input1.quantizedOutput[j].toInt() * input2.quantizedOutput[j].toInt()
            }

            similarityScore += currSum * scaleMul
        }

        return similarityScore
    }
}