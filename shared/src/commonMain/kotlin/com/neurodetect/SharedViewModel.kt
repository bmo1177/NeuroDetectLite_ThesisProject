package com.neurodetect

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data class Standby(val gateResult: com.neurodetect.GateResult) : UiState()
    data class Complete(
        val gateResult: com.neurodetect.GateResult,
        val predictionResult: PredictionResult
    ) : UiState()
    data class Error(val message: String) : UiState()
}

class SharedViewModel(
    private val classifier: OnnxClassifier,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _modelLoaded = MutableStateFlow(false)
    val modelLoaded: StateFlow<Boolean> = _modelLoaded.asStateFlow()

    private val _currentModelName = MutableStateFlow("")
    val currentModelName: StateFlow<String> = _currentModelName.asStateFlow()

    private var observationJob: Job? = null

    fun observeState(callback: (UiState) -> Unit) {
        observationJob?.cancel()
        observationJob = scope.launch {
            _uiState.collect { callback(it) }
        }
    }

    fun stopObserving() {
        observationJob?.cancel()
        observationJob = null
    }

    private var customInputTensor: FloatArray? = null

    fun setInputTensor(tensor: FloatArray) {
        customInputTensor = tensor
    }

    fun clearInputTensor() {
        customInputTensor = null
    }

    fun loadModel(modelBytes: ByteArray, modelName: String = "") {
        scope.launch {
            try {
                classifier.close()
                classifier.load(modelBytes)
                _modelLoaded.value = true
                _currentModelName.value = modelName
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load model: ${e.message}")
            }
        }
    }

    fun analyze(clinicalState: ClinicalState) {
        scope.launch {
            _uiState.value = UiState.Loading

            val gateResult = MmseGate.evaluateComputeRoute(clinicalState)

            if (!_modelLoaded.value) {
                _uiState.value = UiState.Error("Model not loaded. Load ONNX model first.")
                return@launch
            }

            try {
                val inputTensor = generateInputTensor()
                val modelOutput = classifier.predict(inputTensor)
                val probabilities = softmax(modelOutput.logits)
                val predictedClass = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
                val confidence = probabilities[predictedClass]

                val predictionResult = PredictionResult(
                    probabilities = probabilities,
                    predictedClass = predictedClass,
                    confidence = confidence,
                    heatmap = modelOutput.heatmap
                )

                _uiState.value = UiState.Complete(
                    gateResult = gateResult,
                    predictionResult = predictionResult
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Inference failed: ${e.message}")
            }
        }
    }

    fun reset() {
        _uiState.value = UiState.Idle
    }

    fun clearModel() {
        _modelLoaded.value = false
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val max = logits.maxOrNull() ?: 0f
        val exp = FloatArray(logits.size) { kotlin.math.exp((logits[it] - max).toDouble()).toFloat() }
        val sum = exp.sum()
        return FloatArray(exp.size) { exp[it] / sum }
    }

    private fun generateInputTensor(): FloatArray {
        return customInputTensor ?: SAMPLE_TENSOR
    }

    companion object {
        private const val SAMPLE_TENSOR_SIZE = 1 * 7 * 224 * 224
        private val SAMPLE_TENSOR: FloatArray by lazy {
            generateSampleTensor()
        }

        private fun generateSampleTensor(): FloatArray {
            val tensor = FloatArray(SAMPLE_TENSOR_SIZE)
            val seed = 42L
            var state = seed
            for (i in tensor.indices) {
                state = state * 6364136223846793005L + 1442695040888963407L
                val r = ((state shr 33).toInt() and 0x7FFFFFFF).toFloat() / 0x7FFFFFFF
                tensor[i] = (r - 0.5f) * 2f
            }
            return tensor
        }
    }
}
