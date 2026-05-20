package com.neurodetect

data class ClinicalState(
    val totalScore: Int,
    val recallScore: Int,
    val educationYears: Int
) {
    init {
        require(totalScore in 0..30) { "totalScore must be 0..30" }
        require(recallScore in 0..3) { "recallScore must be 0..3" }
        require(educationYears in 0..30) { "educationYears must be 0..30" }
    }
}
