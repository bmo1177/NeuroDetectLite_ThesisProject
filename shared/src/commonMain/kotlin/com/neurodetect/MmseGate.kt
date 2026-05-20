package com.neurodetect

object MmseGate {

    fun evaluateComputeRoute(state: ClinicalState): GateResult {
        val (totalScore, recallScore, educationYears) = state

        if (totalScore < 18) {
            return GateResult(
                alertLevel = AlertLevel.ESCALATE,
                message = "Severe cognitive impairment detected (MMSE < 18). Immediate specialist review required.",
                triggerMriPipeline = true,
                clinicalNote = buildString {
                    append("MMSE $totalScore/30 falls in the moderate-to-severe dementia range. ")
                    append("Structural MRI is required to assess global cortical atrophy, hippocampal volume loss, ")
                    append("and exclude reversible causes (e.g., subdural hematoma, normal pressure hydrocephalus).")
                }
            )
        }

        if (totalScore in 18..23) {
            return GateResult(
                alertLevel = AlertLevel.REVIEW,
                message = "MCI / early dementia range detected (MMSE $totalScore/30). MRI pipeline triggered.",
                triggerMriPipeline = true,
                clinicalNote = buildString {
                    append("MMSE $totalScore/30 falls in the MCI / mild impairment range (18–23). ")
                    append("Structural MRI recommended to evaluate medial temporal lobe atrophy, ")
                    append("hippocampal integrity, and rule out vascular contributions.")
                }
            )
        }

        if (recallScore <= 1) {
            val isHighTotal = totalScore >= 24
            return GateResult(
                alertLevel = AlertLevel.ESCALATE,
                message = if (isHighTotal) {
                    "Critical Delayed Recall deficit (${recallScore}/3) despite normal total score ($totalScore/30). Cognitive compensation suspected."
                } else {
                    "Critical Delayed Recall deficit (${recallScore}/3). High specificity for hippocampal CA1 atrophy."
                },
                triggerMriPipeline = true,
                clinicalNote = buildString {
                    append("Delayed Recall score of $recallScore/3 is the single most sensitive psychometric predictor ")
                    append("of early Alzheimer's pathology. ")
                    if (isHighTotal) {
                        append("Despite a preserved total score ($totalScore/30), this isolated deficit strongly suggests ")
                        append("early hippocampal degeneration that may be masked by cognitive reserve. ")
                    }
                    append("Urgent structural MRI with volumetric hippocampal analysis recommended.")
                }
            )
        }

        if (totalScore in 24..27 && educationYears >= 16) {
            return GateResult(
                alertLevel = AlertLevel.REVIEW,
                message = "Low-normal MMSE ($totalScore/30) with high education ($educationYears years). Cognitive reserve may mask early decline.",
                triggerMriPipeline = true,
                clinicalNote = buildString {
                    append("Patient achieved $totalScore/30 on MMSE with $educationYears years of education. ")
                    append("This score is lower than expected for this educational baseline. ")
                    append("High cognitive reserve may mask early temporal lobe neurodegeneration. ")
                    append("MRI cross-validation recommended to rule out prodromal AD.")
                }
            )
        }

        return GateResult(
            alertLevel = AlertLevel.NONE,
            message = "MMSE $totalScore/30 within normal range. No imaging required at this time.",
            triggerMriPipeline = false,
            clinicalNote = buildString {
                append("MMSE $totalScore/30 with intact Delayed Recall ($recallScore/3) and ")
                append("appropriate education-adjusted performance ($educationYears years). ")
                append("Standard clinical follow-up is sufficient; the deep learning inference pipeline ")
                append("remains in STANDBY to conserve battery and compute resources.")
            }
        )
    }
}
