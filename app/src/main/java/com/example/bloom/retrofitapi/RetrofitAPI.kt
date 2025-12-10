package com.example.bloom.retrofitapi

import com.example.bloom.datamodels.AIFeatureDataModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RetrofitAPI {

    @Multipart
    @POST("processFile")
    suspend fun processFile(
        @Part file: MultipartBody.Part,
        @Part("user_question") userQuestion: RequestBody?
    ): AIFeatureDataModel.AIGenerativeDataModel

    @POST("chat")
    suspend fun chat(@Body request: AIFeatureDataModel.ChatRequest): AIFeatureDataModel.AIGenerativeDataModel

    @POST("insights")
    suspend fun generateInsights(@Body request: AIFeatureDataModel.ChatRequest): AIFeatureDataModel.AIGenerativeDataModel

    @POST("healthScore")
    suspend fun calculateHealthScore(@Body request: AIFeatureDataModel.ChatRequest): AIFeatureDataModel.AIHealthScoreResponse

    @GET("getGeminiResponse")
    suspend fun getGeminiResponse(): AIFeatureDataModel.AIGenerativeDataModel

    //todo: link health score to supabase (when user generates, it should get stored to supabase so
    //users don't have to regenerate again unless they want a new health insight

    //todo: work on ai feature import CSV file to extract transactions.

    //todo: connect everything, fix UI's, remove two ai features that couldn't make it and you're DONE
}