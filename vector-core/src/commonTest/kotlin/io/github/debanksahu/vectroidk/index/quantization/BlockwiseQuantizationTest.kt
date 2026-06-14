package io.github.debanksahu.vectroidk.index.quantization

import io.github.debanksahu.vectroidk.quantization.blockwise.BlockwiseOutput
import io.github.debanksahu.vectroidk.quantization.blockwise.BlockwiseQuantization
import io.github.debanksahu.vectroidk.quantization.blockwise.BlockwiseQuantizationConfiguration
import io.github.debanksahu.vectroidk.utils.enums.NormalizationMethod
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BlockwiseQuantizationTest {

    @Test
    fun quantize_empty_input_returns_empty_output() {
        val config = BlockwiseQuantizationConfiguration(inputSize = 0, blockSize = 2, normalizationMethod = null)
        val quantization = BlockwiseQuantization(config)

        val output = quantization.quantize(floatArrayOf())

        assertContentEquals(byteArrayOf(), output.quantizedOutput)
        assertContentEquals(floatArrayOf(), output.scaleVector)
        assertEquals(0, output.originalSize)
        assertEquals(2, output.blockSize)
    }

    @Test
    fun quantize_without_normalization_various_values_and_blocks() {
        val config = BlockwiseQuantizationConfiguration(inputSize = 5, blockSize = 2, normalizationMethod = null)
        val quantization = BlockwiseQuantization(config)
        val input = floatArrayOf(1f, 2f, -3f, -4f, 5f)

        val output = quantization.quantize(input)

        assertContentEquals(byteArrayOf(64, 127, -95, -127, 127), output.quantizedOutput)
        assertEquals(3, output.scaleVector.size)
        assertEquals(2f / 127f, output.scaleVector[0], 0.0001f)
        assertEquals(4f / 127f, output.scaleVector[1], 0.0001f)
        assertEquals(5f / 127f, output.scaleVector[2], 0.0001f)
        assertEquals(5, output.originalSize)
        assertEquals(2, output.blockSize)
    }

    @Test
    fun quantize_all_zeros() {
        val config = BlockwiseQuantizationConfiguration(inputSize = 4, blockSize = 2, normalizationMethod = null)
        val quantization = BlockwiseQuantization(config)
        val input = floatArrayOf(0f, 0f, 0f, 0f)

        val output = quantization.quantize(input)

        assertContentEquals(byteArrayOf(0, 0, 0, 0), output.quantizedOutput)
        assertEquals(1f, output.scaleVector[0], 0.0001f)
        assertEquals(1f, output.scaleVector[1], 0.0001f)
    }

    @Test
    fun quantize_when_block_size_larger_than_input_creates_single_block() {
        val config = BlockwiseQuantizationConfiguration(inputSize = 3, blockSize = 10, normalizationMethod = null)
        val quantization = BlockwiseQuantization(config)

        val output = quantization.quantize(floatArrayOf(1f, 2f, 3f))

        assertContentEquals(byteArrayOf(42, 85, 127), output.quantizedOutput)
        assertEquals(1, output.scaleVector.size)
        assertEquals(3f / 127f, output.scaleVector[0], 0.0001f)
    }

    @Test
    fun quantize_with_l2_normalization() {
        val config = BlockwiseQuantizationConfiguration(
            inputSize = 2,
            blockSize = 2,
            normalizationMethod = NormalizationMethod.L2_NORMALIZATION
        )
        val quantization = BlockwiseQuantization(config)
        val input = floatArrayOf(3f, 4f)

        val output = quantization.quantize(input)

        assertContentEquals(byteArrayOf(95, 127), output.quantizedOutput)
        assertEquals((4f / 5f) / 127f, output.scaleVector[0], 0.0001f)
    }

    @Test
    fun calculate_similarityscore_identical_vectors() {
        val config = BlockwiseQuantizationConfiguration(
            inputSize = 2,
            blockSize = 2,
            normalizationMethod = NormalizationMethod.L2_NORMALIZATION
        )
        val quantization = BlockwiseQuantization(config)
        val input = floatArrayOf(1f, 1f)

        val output1 = quantization.quantize(input)
        val output2 = quantization.quantize(input)
        val score = quantization.calculateSimilarityScore(output1, output2)

        assertEquals(1.0f, score, 0.0001f)
    }

    @Test
    fun calculate_similarityscore_orthogonal_vectors() {
        val config = BlockwiseQuantizationConfiguration(
            inputSize = 2,
            blockSize = 2,
            normalizationMethod = NormalizationMethod.L2_NORMALIZATION
        )
        val quantization = BlockwiseQuantization(config)

        val output1 = quantization.quantize(floatArrayOf(1f, 0f))
        val output2 = quantization.quantize(floatArrayOf(0f, 1f))
        val score = quantization.calculateSimilarityScore(output1, output2)

        assertEquals(0.0f, score, 0.0001f)
    }

    @Test
    fun calculate_similarityscore_zero_vectors() {
        val config = BlockwiseQuantizationConfiguration(
            inputSize = 2,
            blockSize = 2,
            normalizationMethod = NormalizationMethod.L2_NORMALIZATION
        )
        val quantization = BlockwiseQuantization(config)

        val output1 = quantization.quantize(floatArrayOf(0f, 0f))
        val output2 = quantization.quantize(floatArrayOf(0f, 0f))
        val score = quantization.calculateSimilarityScore(output1, output2)

        assertEquals(0.0f, score, 0.0001f)
    }

    @Test
    fun calculate_similarityscore_invalid_inputs_throws() {
        val config2 = BlockwiseQuantizationConfiguration(
            inputSize = 2,
            blockSize = 2,
            normalizationMethod = NormalizationMethod.L2_NORMALIZATION
        )
        val config3 = BlockwiseQuantizationConfiguration(
            inputSize = 3,
            blockSize = 2,
            normalizationMethod = NormalizationMethod.L2_NORMALIZATION
        )
        val quantization2 = BlockwiseQuantization(config2)
        val quantization3 = BlockwiseQuantization(config3)

        val outputSize2 = quantization2.quantize(floatArrayOf(1f, 2f))
        val outputSize3 = quantization3.quantize(floatArrayOf(1f, 2f, 3f))

        assertFailsWith<IllegalArgumentException> {
            quantization2.calculateSimilarityScore(outputSize2, outputSize3)
        }

        val configBlock2 = BlockwiseQuantizationConfiguration(inputSize = 3, blockSize = 2)
        val configBlock3 = BlockwiseQuantizationConfiguration(inputSize = 3, blockSize = 3)
        val quantizationBlock2 = BlockwiseQuantization(configBlock2)
        val quantizationBlock3 = BlockwiseQuantization(configBlock3)

        val outputBlock2 = quantizationBlock2.quantize(floatArrayOf(1f, 2f, 3f))
        val outputBlock3 = quantizationBlock3.quantize(floatArrayOf(1f, 2f, 3f))

        assertFailsWith<IllegalArgumentException> {
            quantizationBlock2.calculateSimilarityScore(outputBlock2, outputBlock3)
        }
    }

    @Test
    fun convertToQuantizedOutput_with_valid_input_returns_expected_output() {
        val config = BlockwiseQuantizationConfiguration(inputSize = 4, blockSize = 2)
        val quantization = BlockwiseQuantization(config)

        val quantized = byteArrayOf(1, 2, 3, 4)
        val scales = floatArrayOf(0.5f, 1.0f)
        val output = quantization.convertToQuantizedOutput(quantized, mapOf("scaleVector" to scales))

        val expected = BlockwiseOutput(
            quantizedOutput = quantized,
            scaleVector = scales,
            originalSize = 4,
            blockSize = 2
        )
        assertEquals(expected, output)
    }

    @Test
    fun convertToQuantizedOutput_missing_or_invalid_scaleVector_throws() {
        val config = BlockwiseQuantizationConfiguration(inputSize = 2, blockSize = 2)
        val quantization = BlockwiseQuantization(config)

        val ex1 = assertFailsWith<IllegalArgumentException> {
            quantization.convertToQuantizedOutput(byteArrayOf(1, 2), emptyMap())
        }
        assertEquals("scaleVector is not provided", ex1.message)

        val ex2 = assertFailsWith<IllegalArgumentException> {
            quantization.convertToQuantizedOutput(byteArrayOf(1, 2), mapOf("scaleVector" to "invalid"))
        }
        assertEquals("scaleVector should be a FloatArray", ex2.message)
    }
}
