package com.example.testappbt.fbot

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiService {
    @POST("/predict")
    fun getPrediction(@Body request: StringRequest): Call<StringResponse>
}


