package com.example.legcurvaturedetection

data class Prediction(
    val x: Float, val y: Float,
    val width: Float, val height: Float,
    val `class`: String, val confidence: Float
)