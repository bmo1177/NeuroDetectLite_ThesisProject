package com.neurodetect

enum class AlertLevel {
    NONE,
    INFO,
    REVIEW,
    ESCALATE
}

data class GateResult(
    val alertLevel: AlertLevel,
    val message: String,
    val triggerMriPipeline: Boolean,
    val clinicalNote: String
)
