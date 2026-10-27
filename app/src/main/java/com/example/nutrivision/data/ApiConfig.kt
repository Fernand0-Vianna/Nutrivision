package com.example.nutrivision.data

import android.content.Context
import com.example.nutrivision.BuildConfig

/**
 * Configuração da API do Google (Gemini).
 *
 * A chave pode ser informada de duas formas (sem precisar editar código):
 *   1. local.properties do projeto:
 *        GEMINI_API_KEY=SUA_CHAVE
 *      (lida em tempo de build para BuildConfig.GEMINI_API_KEY)
 *   2. Tela Perfil → Integração com a IA (fica salva só no aparelho).
 *
 * Obtenha a chave em https://aistudio.google.com/apikey
 * Enquanto não houver chave, a análise automática fica indisponível e o
 * usuário pode registrar a refeição manualmente.
 */
object ApiConfig {
    const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    /** Modelo com visão e saída JSON. */
    const val MODEL = "gemini-2.5-flash"

    private const val PREFS = "nutrivision_prefs"
    private const val KEY_API = "gemini_api_key"

    /** Chave salva no aparelho ou, na ausência, a do build (local.properties). */
    fun resolveKey(context: Context): String {
        val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_API, null)
            ?.trim()
            .orEmpty()
        if (saved.isNotEmpty()) return saved

        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Throwable) {
            ""
        }
    }

    fun isConfigured(context: Context): Boolean = resolveKey(context).isNotEmpty()

    fun saveKey(context: Context, key: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_API, key.trim())
            .apply()
    }

    fun clearKey(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_API)
            .apply()
    }
}
