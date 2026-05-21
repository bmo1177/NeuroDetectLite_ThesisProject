package com.neurodetect

expect class OnnxClassifier(weightsBytes: ByteArray?) {

    fun load(modelBytes: ByteArray)

    fun predict(inputTensor: FloatArray): ModelOutput

    fun close()
}
