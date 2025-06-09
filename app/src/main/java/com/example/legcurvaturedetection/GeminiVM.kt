package com.example.legcurvaturedetection

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiVM : ViewModel() {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyDQCYZ8XsogCOPyHpm1hO_2CfMN2kSgHfU"
    )

    // Suspending function for chatbot
    suspend fun getResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val query = "Act as a chatbot that only answers questions related to leg . $prompt"
        try {
            val response = generativeModel.generateContent(query)
            val text = response.text ?: "No response text"
            Log.d("GeminiResponse", "Response: $text")
            return@withContext text
        } catch (e: Exception) {
            Log.e("GeminiResponse", "Error: ${e.localizedMessage}", e)
            return@withContext "Failed to connect to Gemini API."
        }
    }


    // Suspending function for translation
    suspend fun translate(query: String): String = withContext(Dispatchers.IO) {
        val prompt = "Translate this text from Arabic to English and return the answer only. $query"
        val response = generativeModel.generateContent(prompt)
        response.text ?: "Translation error."
    }
}
