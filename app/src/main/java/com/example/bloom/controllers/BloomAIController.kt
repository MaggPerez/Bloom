package com.example.bloom.controllers

import android.util.Log
import com.example.bloom.datamodels.AIFeatureDataModel
import com.example.bloom.retrofitapi.RetrofitInstance

class BloomAIController {

    private val api = RetrofitInstance.instance

    /**
     * Generate AI-powered insights from financial summary
     */
    suspend fun generateInsights(summary: String): Result<String> {
        return try {
            Log.d("BloomAIController", "Generating insights with summary: ${summary.take(100)}...")
            val request = AIFeatureDataModel.ChatRequest(message = summary)
            val response = api.generateInsights(request)
            Log.d("BloomAIController", "Insights response: ${response.message.take(100)}...")
            Result.success(response.message)
        } catch (e: Exception) {
            Log.e("BloomAIController", "Failed to generate insights", e)
            Result.failure(e)
        }
    }


    /**
     * Calculate financial health score based on user data
     * Returns the full AIHealthScoreResponse with score and recommendations
     */
    suspend fun generateHealthScore(data: String): Result<AIFeatureDataModel.AIHealthScoreResponse> {
        return try {
            Log.d("BloomAIController", "Generating health score with data: ${data.take(200)}...")
            val request = AIFeatureDataModel.ChatRequest(message = data)
            val response = api.calculateHealthScore(request)
            Log.d("BloomAIController", "Health score response received - Score: ${response.score}, Recommendations: ${response.recommendations.take(100)}...")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("BloomAIController", "Failed to generate health score", e)
            Result.failure(e)
        }
    }
}
