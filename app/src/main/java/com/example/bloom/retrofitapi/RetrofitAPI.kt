package com.example.bloom.retrofitapi

import com.example.bloom.datamodels.AIFeatureDataModel
import okhttp3.Call
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RetrofitAPI {

    @POST("processFile")
    fun postFile(@Body aIGenerativeDataModel: AIFeatureDataModel.AIGenerativeDataModel?):
            retrofit2.Call<AIFeatureDataModel.AIGenerativeDataModel?>?

    @GET("geminiResponse")
    suspend fun getGeminiResponse(): AIFeatureDataModel.AIGenerativeDataModel

}