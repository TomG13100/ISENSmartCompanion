package fr.isen.guerrand.isensmartcompanion.network
import fr.isen.guerrand.isensmartcompanion.BuildConfig

import com.google.ai.client.generativeai.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object GeminiApiService {
    private const val API_KEY = BuildConfig.GEMINI_API_KEY

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = API_KEY
    )

    suspend fun getAiResponse(userInput: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = model.generateContent(userInput)
                response.text ?: "Aucune réponse obtenue."
            } catch (e: Exception) {
                "Erreur lors de la communication avec l'IA : ${e.message}"
            }
        }
    }
}