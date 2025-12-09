package com.example.bloom.controllers

import com.example.bloom.datamodels.AIFeatureDataModel
import com.example.bloom.retrofitapi.RetrofitInstance

class BloomAIController {

    private val api = RetrofitInstance.instance

    /**
     * Generate AI-powered insights from financial summary
     */
    suspend fun generateInsights(summary: String): Result<String> {
        return try {
            val request = AIFeatureDataModel.ChatRequest(message = summary)
            val response = api.generateInsights(request)
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
