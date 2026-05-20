package com.neurodetect

data class PredictionResult(
    val probabilities: FloatArray,
    val predictedClass: Int,
    val confidence: Float,
    val heatmap: FloatArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PredictionResult) return false
        return probabilities.contentEquals(other.probabilities) &&
                predictedClass == other.predictedClass &&
                confidence == other.confidence &&
                heatmap.contentEquals(other.heatmap)
    }

    override fun hashCode(): Int {
        var result = probabilities.contentHashCode()
        result = 31 * result + predictedClass
        result = 31 * result + confidence.hashCode()
        result = 31 * result + (heatmap?.contentHashCode() ?: 0)
        return result
    }

    companion object {
        val CLASS_NAMES = listOf("Cognitively Normal", "MCI", "Alzheimer's Disease")
    }
}
