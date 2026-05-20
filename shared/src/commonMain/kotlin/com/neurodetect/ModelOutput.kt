package com.neurodetect

data class ModelOutput(
    val logits: FloatArray,
    val heatmap: FloatArray? = null
)
