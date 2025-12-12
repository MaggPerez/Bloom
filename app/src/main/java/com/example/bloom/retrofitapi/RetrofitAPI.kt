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

    @Multipart
    @POST("importCSV")
    suspend fun importCSV(
        @Part file: MultipartBody.Part
    ): com.example.bloom.aifeatures.CsvImportResponse


}