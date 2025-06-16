package com.example.legcurvaturedetection

data class HistoryItem(
    val documentId: String = "",
    val name: String = "",
    val date: String = "",
    val result: String = "",
    val imageUri: String? = null,
    val userId: String? = null
)