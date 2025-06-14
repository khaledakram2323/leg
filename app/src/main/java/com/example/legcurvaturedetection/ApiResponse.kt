package com.example.legcurvaturedetection

data class ApiResponse(
    val filename: String,
    val prediction: String,
    val confidence: Double,
    val all_probabilities: Map<String, Double>
)
