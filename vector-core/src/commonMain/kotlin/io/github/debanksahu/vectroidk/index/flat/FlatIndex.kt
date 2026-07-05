package io.github.debanksahu.vectroidk.index.flat

import io.github.debanksahu.vectroidk.index.Indexing
import io.github.debanksahu.vectroidk.quantization.QuantizationConfiguration
import io.github.debanksahu.vectroidk.quantization.QuantizationOutput
import io.github.debanksahu.vectroidk.storage.QuantizationUtilSerializer
import io.github.debanksahu.vectroidk.storage.dao.FlatIndexDao
import io.github.debanksahu.vectroidk.storage.dao.MetadataDao
import io.github.debanksahu.vectroidk.storage.model.EmbeddingDetailInput
import io.github.debanksahu.vectroidk.storage.model.GlobalConfigTable
import io.github.debanksahu.vectroidk.utils.datastructure.BoundedMaxHeap

class FlatIndex<QUANTIZATION_CONFIG_TYPE : QuantizationConfiguration, QUANTIZATION_OUTPUT_TYPE : QuantizationOutput>(
    override val indexConfig: FlatIndexConfiguration<QUANTIZATION_CONFIG_TYPE, QUANTIZATION_OUTPUT_TYPE>,
    override val indexDao: FlatIndexDao,
    override val metadataDao: MetadataDao,
    override val globalConfig: GlobalConfigTable
) : Indexing<FlatIndexConfiguration<QUANTIZATION_CONFIG_TYPE, QUANTIZATION_OUTPUT_TYPE>, FlatIndexOutput, FlatIndexDao> {

    override fun build() {

    }

    override suspend fun search(inputVector: FloatArray): Array<FlatIndexOutput> {
        val quantizedInput = indexConfig.quantizationTool.quantize(inputVector)

        val topKHeap = BoundedMaxHeap(size = indexConfig.topK)
        val allEmbeddingDetails = indexDao.getAllEmbeddingDetail()
        for (index in allEmbeddingDetails.indices) {
            val embedding = allEmbeddingDetails[index].embedding
            val quantizationUtils =
                QuantizationUtilSerializer.deserialize(allEmbeddingDetails[index].quantizationUtilBytes)
            val quantizationKwargs = buildQuantizationKwargs(quantizationUtils)
            val quantizedOutput = indexConfig.quantizationTool.convertToQuantizedOutput(
                inputVector = embedding,
                kwargs = quantizationKwargs
            )
            val similarityScore = indexConfig.quantizationTool.calculateSimilarityScore(
                input1 = quantizedInput,
                input2 = quantizedOutput
            )

            topKHeap.add(index = index, value = similarityScore)
        }
        val result = Array(
            size = topKHeap.size,
            init = { FlatIndexOutput(metadata = emptyMap()) }
        )
        var pointer = topKHeap.size - 1
        while (topKHeap.size > 0) {
            val index = topKHeap.popMinEle()
            val embeddingId = allEmbeddingDetails[index].id
            val metadata = metadataDao.getAllMetadata(embeddingId = embeddingId)
            result[pointer] = FlatIndexOutput(metadata = metadata)
        }


        return result
    }

    override suspend fun insert(inputVector: FloatArray, metadata: Map<String, Any>): Long {
        val quantizedVector = indexConfig.quantizationTool.quantize(inputVector)
        val quantizationUtils = indexConfig.quantizationTool.extractQuantizationUtils(quantizedVector)
        val quantizationUtilBytes = QuantizationUtilSerializer.serialize(quantizationUtils)
        val embeddingId = indexDao.insertEmbeddingDetail(
            input = EmbeddingDetailInput(
                collection = "",
                embedding = quantizedVector.quantizedOutput,
                quantizationUtilBytes = quantizationUtilBytes
            )
        )
        return embeddingId
    }

}