package com.example.nutrivision.data

import android.graphics.Bitmap
import android.util.Base64
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/** Falha de análise que deve ser mostrada ao usuário (sem inventar valores). */
class FoodAnalysisException(message: String) : Exception(message)

/**
 * Cliente de análise de imagem usando a API Google Generative Language (Gemini).
 * A chave é resolvida por [ApiConfig].
 *
 * Importante: quando não há chave ou a API falha, [analyze] lança
 * [FoodAnalysisException]. O app nunca gera macros fictícios; o usuário pode
 * registrar a refeição manualmente na tela de scan.
 */
object FoodAnalyzer {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /** Última mensagem de erro da API (exibida na tela de scan). */
    @Volatile
    var lastError: String? = null
        private set

    private const val PROMPT = """
        Você é um nutricionista especializado em estimar refeições por foto.
        Analise a imagem e responda SOMENTE com um objeto JSON válido, sem markdown,
        no formato exato:
        {"mealName":"nome curto da refeição em português","calories":000,"protein":00,"carbs":00,"fat":00}
        Regras:
        - Estime a porção visível inteira (não por 100 g).
        - calories em kcal; protein, carbs e fat em gramas.
        - Use números inteiros.
        - Se não houver comida na imagem, use valores zeros e mealName "Não identificado".
    """

    suspend fun analyze(bitmap: Bitmap, apiKey: String): FoodRecord {
        lastError = null
        if (apiKey.isBlank()) {
            val message = "Chave Gemini não configurada. Configure no Perfil ou registre a refeição manualmente."
            lastError = message
            throw FoodAnalysisException(message)
        }
        return try {
            parseResponse(callGemini(bitmap, apiKey))
        } catch (e: FoodAnalysisException) {
            lastError = e.message
            throw e
        } catch (e: Exception) {
            val message = e.message?.takeIf { it.isNotBlank() } ?: "Falha ao consultar a API"
            lastError = message
            throw FoodAnalysisException(message)
        }
    }

    private fun callGemini(bitmap: Bitmap, apiKey: String): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)

        val inline = JSONObject().apply {
            put("mime_type", "image/jpeg")
            put("data", base64)
        }
        val parts = JSONArray()
            .put(JSONObject().put("text", PROMPT))
            .put(JSONObject().put("inline_data", inline))
        val content = JSONObject().put("parts", parts)
        val generationConfig = JSONObject().apply {
            put("temperature", 0.2)
            put("responseMimeType", "application/json")
        }
        val payload = JSONObject()
            .put("contents", JSONArray().put(content))
            .put("generationConfig", generationConfig)

        val url = "${ApiConfig.BASE_URL}${ApiConfig.MODEL}:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val apiMessage = runCatching {
                    JsonParser().parse(body).asJsonObject
                        .getAsJsonObject("error")
                        .get("message")?.asString
                }.getOrNull()
                throw IllegalStateException(
                    "API ${response.code}: ${apiMessage ?: "erro ao analisar a imagem"}"
                )
            }
            return body
        }
    }

    private fun parseResponse(raw: String): FoodRecord {
        val root = JsonParser().parse(raw).asJsonObject
        val text = root["candidates"]
            ?.asJsonArray?.firstOrNull()
            ?.asJsonObject?.get("content")
            ?.asJsonObject?.get("parts")
            ?.asJsonArray?.firstOrNull()
            ?.asJsonObject?.get("text")
            ?.asString ?: throw FoodAnalysisException("A IA não devolveu nenhum conteúdo para esta foto.")

        val obj = runCatching { JsonParser().parse(extractJson(text)).asJsonObject }
            .getOrElse { throw FoodAnalysisException("A IA devolveu uma resposta inválida. Tente outra foto.") }

        fun number(key: String, max: Double): Double =
            (obj[key]?.takeIf { !it.isJsonNull }?.asDouble ?: 0.0).coerceIn(0.0, max)

        val name = obj["mealName"]?.takeIf { !it.isJsonNull }?.asString?.trim().orEmpty()
        val calories = number("calories", 5000.0)
        val protein = number("protein", 1000.0)
        val carbs = number("carbs", 1000.0)
        val fat = number("fat", 1000.0)

        val unidentified = name.isEmpty() || name.equals("Não identificado", ignoreCase = true) ||
            name.equals("Nao identificado", ignoreCase = true)
        if (unidentified || (calories <= 0.0 && protein <= 0.0 && carbs <= 0.0 && fat <= 0.0)) {
            throw FoodAnalysisException(
                "Não foi possível identificar comida nesta foto. Tente enquadrar melhor o prato ou registre manualmente."
            )
        }

        return FoodRecord(
            mealName = name,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            source = "gemini"
        )
    }

    private fun extractJson(text: String): String {
        val cleaned = text
            .replace("```json", "")
            .replace("```", "")
            .trim()
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        return if (start >= 0 && end > start) cleaned.substring(start, end + 1) else "{}"
    }
}
