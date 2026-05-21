package com.neurodetect

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create

actual class OnnxClassifier actual constructor(weightsBytes: ByteArray?) {
    constructor() : this(null)

    private var session: Any? = null

    actual fun load(modelBytes: ByteArray) {
        val data = modelBytes.toNSData()
        TODO("Replace with CocoaPods-generated Kotlin bindings after pod install")
    }

    actual fun predict(inputTensor: FloatArray): ModelOutput {
        val sess = session ?: throw IllegalStateException("Model not loaded")
        TODO("Replace with CocoaPods-generated Kotlin bindings after pod install")
    }

    actual fun close() {
        session = null
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ByteArray.toNSData(): NSData {
        return usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = this.size.toULong()
            )
        }
    }
}
