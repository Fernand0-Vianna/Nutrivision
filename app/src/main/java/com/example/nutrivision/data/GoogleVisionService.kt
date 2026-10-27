package com.example.nutrivision.data

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Placeholder para a API do Google de análise de imagem (Gemini Vision / Cloud Vision).
 * A chave da API será configurada depois em BuildConfig ou no próprio código.
 */
interface GoogleVisionService {
    @POST("v1/models/gemini-1.5-flash:generateContent")
    suspend fun analyzeImage(
        @Query("key") apiKey: String,
        @Body body: GeminiRequest
    ): ResponseBody
}

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

data class InlineData(
    val mimeType: String,
    val data: String
)
