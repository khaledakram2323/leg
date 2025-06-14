package com.example.legcurvaturedetection

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // Inference via URL
    @GET("leg-detection-ofgjl/2")
    fun inferViaUrl(
        @Query("api_key") apiKey: String,
        @Query("image") imageUrl: String
    ): Call<RoboflowResponse>

    // Inference via File Upload
    @Multipart
    @POST("leg-detection-ofgjl/2")
    fun inferViaFile(
        @Query("api_key") apiKey: String,
        @Part file: MultipartBody.Part
    ): Call<RoboflowResponse>
}
