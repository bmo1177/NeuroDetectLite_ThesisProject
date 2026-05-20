package com.neurodetect

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create

/**
 * iOS OnnxClassifier implementation using onnxruntime-objc.
 *
 * The CocoaPods plugin (configured in shared/build.gradle.kts) generates
 * Kotlin bindings for the onnxruntime-mobile-objc pod. The generated
 * bindings are available as the "cocoapods.Onnxruntime" package after
 * running `pod install` in iosApp/ and building the shared framework.
 *
 * Key ObjC classes used (via generated Kotlin/Native bindings):
 *   - ORTEnv          → ONNX Runtime environment
 *   - ORTSession      → Loaded model session
 *   - ORTValue        → Input/output tensor wrapper
 *   - ORTSessionOptions → Session configuration
 *
 * Build dependencies:
 *   1. cd iosApp && pod install
 *   2. Open iosApp.xcworkspace in Xcode
 *   3. Build the shared framework via Gradle
 */
actual class OnnxClassifier {

    private var session: Any? = null

    actual fun load(modelBytes: ByteArray) {
        val data = modelBytes.toNSData()
        // CocoaPods plugin will generate:
        //   ORTEnv   → https://microsoft.github.io/onnxruntime/api/objc/Interface/ORTEnv.html
        //   ORTSession → https://microsoft.github.io/onnxruntime/api/objc/Interface/ORTSession.html
        //
        // Example of generated API usage:
        //   val env = ORTEnv(loggingLevel = ORTLoggingLevel.WARNING, name = "neurodetect")
        //   val opts = ORTSessionOptions()
        //   opts.setGraphOptimizationLevel(ORTGraphOptimizationLevel.ALL)
        //   session = env.createSession(data, opts)
        TODO("Replace with CocoaPods-generated Kotlin bindings after pod install")
    }

    actual fun predict(inputTensor: FloatArray): FloatArray {
        val sess = session ?: throw IllegalStateException("Model not loaded")

        // Example of generated API usage:
        //   val inputName = sess.inputNames.first() as String
        //   val inputValue = ORTValue(
        //       tensorData = inputTensor.toNSData(),
        //       elementType = ORTTensorElementDataType.float32,
        //       shape = [1, 7, 224, 224]
        //   )
        //   val outputs = sess.run(
        //       withInputs = mapOf(inputName to inputValue),
        //       outputNames = nil,
        //       runOptions = nil
        //   )
        //   val outputData = outputs.values.first().tensorData as NSData
        //   return outputData.toFloatArray()
        TODO("Replace with CocoaPods-generated Kotlin bindings after pod install")
    }

    private fun ByteArray.toNSData(): NSData {
        return usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = this.size.toULong()
            )
        }
    }
}
