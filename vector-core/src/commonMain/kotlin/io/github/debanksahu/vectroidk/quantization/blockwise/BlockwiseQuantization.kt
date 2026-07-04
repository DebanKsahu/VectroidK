package io.github.debanksahu.vectroidk.quantization.blockwise

import io.github.debanksahu.vectroidk.quantization.Quantization
import io.github.debanksahu.vectroidk.storage.QuantizationUtils
import io.github.debanksahu.vectroidk.utils.enums.NormalizationMethod
import io.github.debanksahu.vectroidk.utils.extension.toByteArray
import kotlin.math.*

/**
 * # About #
 * - This class provide utils for **Blockwise Quantization** algorithm.
 */
class BlockwiseQuantization(
    override val config: BlockwiseQuantizationConfiguration
) : Quantization<BlockwiseQuantizationConfiguration, BlockwiseOutput> {

    init {
        require(config.blockSize > 0) { "blockSize must be 0 or greater" }
        require(config.inputSize > 0) { "input size must be 0 or greater" }
    }

    /**
     * # About #
     * - This function quantize the input [FloatArray] to IntX (X = 8,16,32 Based on requirement)
     * @param inputVector The input embedding vector
     * @return This return a [BlockwiseOutput] object which contain necessary
     * values, which required in future operations like **similarity score calculation**,
     * **dequantization** etc.
     */
    override fun quantize(inputVector: FloatArray): BlockwiseOutput {
        require(inputVector.size == config.inputSize) { "input vectors size must be equal to ${config.inputSize}" }

        val numberOfBlocks = (config.inputSize + config.blockSize - 1) / config.blockSize
        val outputVector = ByteArray(config.inputSize, init = { 0 })
        val scaleVector = FloatArray(size = numberOfBlocks, init = { 0f })
        applyQuantization(inputVector = inputVector, outputVector = outputVector, scaleVector = scaleVector)
        return BlockwiseOutput(
            quantizedOutput = outputVector,
            scaleVector = scaleVector,
            originalSize = config.inputSize,
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
        var start = 0
        var currMaxAbs = 0.0f
        var currScale: Float
        when (config.normalizationMethod) {
            null -> {
                for (end in 0..<config.inputSize) {
                    currMaxAbs = max(currMaxAbs, abs(inputVector[end]))
                    if (end - start + 1 == config.blockSize || end == config.inputSize - 1) {
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

            NormalizationMethod.L2_NORMALIZATION -> {
                var squareSum = 0.0
                for (inputNum in inputVector) {
                    squareSum += inputNum.toDouble() * inputNum.toDouble()
                }
                val norm = sqrt(squareSum).toFloat()
                if (norm == 0f) return

                for (end in 0..<config.inputSize) {
                    currMaxAbs = max(currMaxAbs, abs(inputVector[end]))
                    if (end - start + 1 == config.blockSize || end == config.inputSize - 1) {
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
        require(input1.originalSize == config.inputSize) {
            "The input(1) vectors original size does not match with ${config.inputSize}"
        }
        require(input2.originalSize == config.inputSize) {
            "The input(2) vectors original size does not match with ${config.inputSize}"
        }
        require(input1.blockSize == input2.blockSize) {
            "The input vectors block size does not match"
        }

        var similarityScore = 0.0f
        for (i in input1.scaleVector.indices) {
            val scaleMul = input1.scaleVector[i] * input2.scaleVector[i]
            val start = i * input1.blockSize
            val end = min(((i + 1) * input1.blockSize) - 1, input1.originalSize - 1)

            var currSum = 0
            for (j in start..end) {
                currSum += input1.quantizedOutput[j].toInt() * input2.quantizedOutput[j].toInt()
            }

            similarityScore += currSum * scaleMul
        }

        return similarityScore
    }

    /**
     * # About #
     * This function convert the stored embedding (in form of [ByteArray]) to [BlockwiseOutput]
     * which is used as input type in other functions like **calculateSimilarityScore**.
     * @param inputVector The stored embedding
     * @param kwargs Additional keyword arguments. Supported keys includes:
     * - `"scaleVector"` ([FloatArray]): A FloatArray which contain scale for each block.
     * @return Return a [BlockwiseOutput] object.
     */
    override fun convertToQuantizedOutput(
        inputVector: ByteArray,
        kwargs: Map<String, Any>
    ): BlockwiseOutput {
        require("scaleVector" in kwargs) { "scaleVector is not provided" }
        val scaleVector = kwargs["scaleVector"]

        require(scaleVector is FloatArray) { "scaleVector should be a FloatArray" }

        return BlockwiseOutput(
            quantizedOutput = inputVector,
            scaleVector = scaleVector,
            originalSize = config.inputSize,
            blockSize = config.blockSize
        )
    }

    override fun extractQuantizationUtils(input: BlockwiseOutput): QuantizationUtils {
        return QuantizationUtils.BlockwiseQuantizationUtils(
            scaleVector = input.scaleVector.toByteArray()
        )
    }
}