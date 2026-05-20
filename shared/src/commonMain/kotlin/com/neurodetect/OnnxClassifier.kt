package com.neurodetect

expect class OnnxClassifier(weightsBytes: ByteArray? = null) {

    fun load(modelBytes: ByteArray)

    fun predict(inputTensor: FloatArray): ModelOutput

    fun close()
}
