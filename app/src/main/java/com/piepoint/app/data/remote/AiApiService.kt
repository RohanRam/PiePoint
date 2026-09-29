package com.piepoint.app.data.remote

import com.piepoint.app.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Retrofit API service for the PiePoint AI backend.
 */
interface AiApiService {

    @POST("create-pizza")
    suspend fun createPizza(@Body request: CreatePizzaRequest): Response<CreatePizzaResponse>

    @POST("chat")
    suspend fun chat(@Body request: ChatRequest): Response<ChatResponse>

    @GET("menu")
    suspend fun getMenu(): Response<Map<String, Any>>

    companion object {
        fun create(baseUrl: String = AiConfig.baseUrl): AiApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(35, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

            return Retrofit.Builder()
                .baseUrl(sanitizedUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AiApiService::class.java)
        }
    }
}
