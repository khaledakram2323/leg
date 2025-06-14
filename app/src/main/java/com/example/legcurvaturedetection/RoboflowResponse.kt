package com.example.legcurvaturedetection



data class RoboflowResponse(
    val predictions: List<Prediction>? = emptyList(),
    val image: ImageData? = null
)

data class ImageData(
    val width: Int,
    val height: Int,
    val url: String
)
