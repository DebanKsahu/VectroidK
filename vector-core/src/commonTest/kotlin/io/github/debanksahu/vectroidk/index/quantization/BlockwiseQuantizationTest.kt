package io.github.debanksahu.vectroidk.index.quantization

import io.github.debanksahu.vectroidk.quantization.blockwise.BlockwiseQuantization
import io.github.debanksahu.vectroidk.quantization.blockwise.BlockwiseQuantizationConfiguration
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BlockwiseQuantizationTest {
    @Test
    fun quantize_creates_expected_number_of_blocks() {
        val config = BlockwiseQuantizationConfiguration(blockSize = 2, normalizationMethod = null)
        val blockwiseQuantization = BlockwiseQuantization(config)

        val output = blockwiseQuantization.quantize(floatArrayOf(1f, 2f, 3f, 4f, 5f))

        assertEquals(3, output.scaleVector.size)
    }

    @Test
    fun quantize_when_block_size_is_larger_than_input_creates_single_block() {
        val config = BlockwiseQuantizationConfiguration(blockSize = 10, normalizationMethod = null)
        val blockwiseQuantization = BlockwiseQuantization(config)

        val output = blockwiseQuantization.quantize(floatArrayOf(1f, 2f, 3f))

        assertContentEquals(
            byteArrayOf(42, 85, 127),
            output.quantizedOutput
        )
        assertEquals(1, output.scaleVector.size)
        assertEquals(3f / 127f, output.scaleVector[0], 0.0001f)
    }

    @Test
    fun quantize_empty_input_returns_empty_output() {
        val config = BlockwiseQuantizationConfiguration(blockSize = 2, normalizationMethod = null)
        val blockwiseQuantization = BlockwiseQuantization(config)

        val output = blockwiseQuantization.quantize(floatArrayOf())

        assertContentEquals(byteArrayOf(), output.quantizedOutput)
        assertContentEquals(floatArrayOf(), output.scaleVector)
        assertEquals(0, output.originalSize)
        assertEquals(2, output.blockSize)
    }

    @Test
    fun quantize_without_normalization() {
        val config = BlockwiseQuantizationConfiguration(blockSize = 2, normalizationMethod = null)
        val blockwiseQuantization = BlockwiseQuantization(config)
        val input = floatArrayOf(1f, 2f, 3f, 4f)
        val output = blockwiseQuantization.quantize(input)

        assertContentEquals(byteArrayOf(64, 127, 95, 127), output.quantizedOutput)
        assertEquals(2f / 127f, output.scaleVector[0], 0.0001f)
        assertEquals(4f / 127f, output.scaleVector[1], 0.0001f)
        assertEquals(4, output.originalSize)
        assertEquals(2, output.blockSize)
    }

    @Test
    fun quantize_with_zero_vector() {
        val config = BlockwiseQuantizationConfiguration(blockSize = 2, normalizationMethod = null)
        val blockwiseQuantization = BlockwiseQuantization(config)
        val input = floatArrayOf(0f, 0f, 0f, 0f)
        val output = blockwiseQuantization.quantize(input)

        assertContentEquals(byteArrayOf(0, 0, 0, 0), output.quantizedOutput)
        assertEquals(1f, output.scaleVector[0], 0.0001f)
        assertEquals(1f, output.scaleVector[1], 0.0001f)
    }

    @Test
    fun quantize_with_l2_normalization() {
        val config = BlockwiseQuantizationConfiguration(
            blockSize = 2,
            normalizationMethod = io.github.debanksahu.vectroidk.utils.enums.NormalizationMethod.L2_NORMALIZATION
        )
        val blockwiseQuantization = BlockwiseQuantization(config)
        val input = floatArrayOf(3f, 4f)
        val output = blockwiseQuantization.quantize(input)

        assertContentEquals(byteArrayOf(95, 127), output.quantizedOutput)
        assertEquals((4f / 5f) / 127f, output.scaleVector[0], 0.0001f)
    }

    @Test
    fun quantize_negative_values() {
        val config = BlockwiseQuantizationConfiguration(blockSize = 2, normalizationMethod = null)
        val blockwiseQuantization = BlockwiseQuantization(config)
        val input = floatArrayOf(-1f, -2f)
        val output = blockwiseQuantization.quantize(input)

        assertContentEquals(byteArrayOf(-63, -127), output.quantizedOutput)
        assertEquals(2f / 127f, output.scaleVector[0], 0.0001f)
    }

    @Test
    fun quantize_partial_block() {
        val config = BlockwiseQuantizationConfiguration(blockSize = 2, normalizationMethod = null)
        val blockwiseQuantization = BlockwiseQuantization(config)
        val input = floatArrayOf(1f, 2f, 3f, 4f, 5f)
        val output = blockwiseQuantization.quantize(input)

        assertContentEquals(byteArrayOf(64, 127, 95, 127, 127), output.quantizedOutput)
        assertEquals(2f / 127f, output.scaleVector[0], 0.0001f)
        assertEquals(4f / 127f, output.scaleVector[1], 0.0001f)
        assertEquals(5f / 127f, output.scaleVector[2], 0.0001f)
    }

    @Test
    fun calculate_similarityscore_identical_vectors() {
        val config = BlockwiseQuantizationConfiguration(
            blockSize = 2,
            normalizationMethod = io.github.debanksahu.vectroidk.utils.enums.NormalizationMethod.L2_NORMALIZATION
        )
        val blockwiseQuantization = BlockwiseQuantization(config)
        val input = floatArrayOf(1f, 1f)
        val output1 = blockwiseQuantization.quantize(input)
        val output2 = blockwiseQuantization.quantize(input)
        val score = blockwiseQuantization.calculateSimilarityScore(output1, output2)
        assertEquals(1.0f, score, 0.0001f)
    }

    @Test
    fun calculate_similarityscore_orthogonal_vectors() {
        val config = BlockwiseQuantizationConfiguration(
            blockSize = 2,
            normalizationMethod = io.github.debanksahu.vectroidk.utils.enums.NormalizationMethod.L2_NORMALIZATION
        )
        val blockwiseQuantization = BlockwiseQuantization(config)
        val output1 = blockwiseQuantization.quantize(floatArrayOf(1f, 0f))
        val output2 = blockwiseQuantization.quantize(floatArrayOf(0f, 1f))
        val score = blockwiseQuantization.calculateSimilarityScore(output1, output2)
        assertEquals(0.0f, score, 0.0001f)
    }

    @Test
    fun calculate_similarityscore_mismatched_originalsize_vectors() {
        val config = BlockwiseQuantizationConfiguration(
            blockSize = 2,
            normalizationMethod = io.github.debanksahu.vectroidk.utils.enums.NormalizationMethod.L2_NORMALIZATION
        )
        val blockwiseQuantization = BlockwiseQuantization(config)
        val output1 = blockwiseQuantization.quantize(floatArrayOf(1f, 2f))
        val output2 = blockwiseQuantization.quantize(floatArrayOf(1f, 2f, 3f))
        assertFailsWith<IllegalArgumentException> {
            blockwiseQuantization.calculateSimilarityScore(output1, output2)
        }
    }

    @Test
    fun calculate_similarityscore_mismatched_blocksize_vectors() {
        val config1 = BlockwiseQuantizationConfiguration(
            blockSize = 2,
            normalizationMethod = io.github.debanksahu.vectroidk.utils.enums.NormalizationMethod.L2_NORMALIZATION
        )
        val config2 = BlockwiseQuantizationConfiguration(
            blockSize = 3,
            normalizationMethod = io.github.debanksahu.vectroidk.utils.enums.NormalizationMethod.L2_NORMALIZATION
        )
        val blockwiseQuantization1 = BlockwiseQuantization(config1)
        val blockwiseQuantization2 = BlockwiseQuantization(config2)
        val output1 = blockwiseQuantization1.quantize(floatArrayOf(1f, 2f, 3f))
        val output2 = blockwiseQuantization2.quantize(floatArrayOf(1f, 2f, 3f))
        assertFailsWith<IllegalArgumentException> {
            blockwiseQuantization1.calculateSimilarityScore(output1, output2)
        }
    }

    @Test
    fun calculate_similarityscore_zero_vectors() {
        val config = BlockwiseQuantizationConfiguration(
            blockSize = 2,
            normalizationMethod = io.github.debanksahu.vectroidk.utils.enums.NormalizationMethod.L2_NORMALIZATION
        )
        val blockwiseQuantization = BlockwiseQuantization(config)
        val output1 = blockwiseQuantization.quantize(floatArrayOf(0f, 0f))
        val output2 = blockwiseQuantization.quantize(floatArrayOf(0f, 0f))
        val score = blockwiseQuantization.calculateSimilarityScore(output1, output2)
        assertEquals(0.0f, score, 0.0001f)
    }
}