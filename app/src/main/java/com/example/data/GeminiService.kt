package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

class GeminiService {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @JsonClass(generateAdapter = true)
    data class SummaryOutput(
        val summary: String,
        val actionItems: List<String>
    )

    @JsonClass(generateAdapter = true)
    data class SmsOutput(
        val urgencyScore: Int,
        val priorityLevel: String,
        val categoryReasoning: String
    )

    suspend fun summarizeCall(
        clientName: String,
        company: String,
        transcript: String
    ): SummaryOutput = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("GeminiService", "API Key is empty/placeholder. Using local secure fallback engine.")
            return@withContext localCallSummarizerFallback(clientName, transcript)
        }

        val prompt = """
            Analyze this transcription of an encrypted private call.
            Client: $clientName ($company)
            Transcript:
            "$transcript"
            
            Provide a direct, concise summary (1 to 2 sentences) and extract 1 to 4 clear action items.
            Return a raw JSON object matching this schema EXACTLY:
            {
              "summary": "string describing what was discussed",
              "actionItems": ["action item 1", "action item 2"]
            }
            Do NOT wrap the response in markdown ```json blocks. Just return raw JSON.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.2f,
                responseMimeType = "application/json"
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            Log.d("GeminiService", "Call Summary JSON: $jsonText")
            val sanitized = sanitizeJson(jsonText)
            
            val adapter = moshi.adapter(SummaryOutput::class.java)
            adapter.fromJson(sanitized) ?: localCallSummarizerFallback(clientName, transcript)
        } catch (e: Exception) {
            Log.e("GeminiService", "Gemini call failed, using fallback: ${e.message}")
            localCallSummarizerFallback(clientName, transcript)
        }
    }

    suspend fun classifySms(
        sender: String,
        body: String,
        linkedProjects: String
    ): SmsOutput = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("GeminiService", "API Key is empty/placeholder. Using local secure fallback SMS engine.")
            return@withContext localSmsFallback(body, linkedProjects)
        }

        val prompt = """
            You are a private offline assistant filtering SMS communications.
            Sender: $sender
            SMS Body: "$body"
            Linked Project Channels: $linkedProjects
            
            Determine:
            1. An Urgency Score from 1 (very low) to 10 (critical blocker).
            2. A Priority Level ("Critical", "Urgent", "High", "Normal").
            3. A short, 1-sentence reasoning outlining why this SMS has this priority, specifically linking it to active project channels or context.
            
            Return a raw JSON object matching this schema EXACTLY:
            {
              "urgencyScore": Int,
              "priorityLevel": "string",
              "categoryReasoning": "string"
            }
            Do NOT wrap the response in markdown ```json blocks. Just return raw JSON.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.2f,
                responseMimeType = "application/json"
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            Log.d("GeminiService", "Sms Classify JSON: $jsonText")
            val sanitized = sanitizeJson(jsonText)
            
            val adapter = moshi.adapter(SmsOutput::class.java)
            adapter.fromJson(sanitized) ?: localSmsFallback(body, linkedProjects)
        } catch (e: Exception) {
            Log.e("GeminiService", "Gemini SMS classification failed: ${e.message}")
            localSmsFallback(body, linkedProjects)
        }
    }

    private fun sanitizeJson(raw: String): String {
        return raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private fun localCallSummarizerFallback(clientName: String, transcript: String): SummaryOutput {
        val summary: String
        val actionItems = mutableListOf<String>()

        if (transcript.contains("delay", ignoreCase = true) || transcript.contains("problem", ignoreCase = true) || transcript.contains("issue", ignoreCase = true)) {
            summary = "Discussed technical blockages and milestones delay with $clientName."
            actionItems.add("Investigate integration connection blockers immediately.")
            actionItems.add("Schedule backup alignment check-in.")
        } else if (transcript.contains("price", ignoreCase = true) || transcript.contains("cost", ignoreCase = true) || transcript.contains("budget", ignoreCase = true)) {
            summary = "Reviewed project financial limits and budget scope details with $clientName."
            actionItems.add("Send updated line-item invoice breakdown.")
            actionItems.add("Verify custom developer rates sheet.")
        } else if (transcript.contains("database", ignoreCase = true) || transcript.contains("api", ignoreCase = true) || transcript.contains("deploy", ignoreCase = true)) {
            summary = "Aligned on cloud database architecture structures and endpoint deployments."
            actionItems.add("Provision secure cloud sandbox profile.")
            actionItems.add("Audit connection permissions keys.")
        } else {
            summary = "Conducted routine roadmap status alignment and project planning check-in."
            actionItems.add("Establish weekly milestones tracker.")
            actionItems.add("Send project brief update newsletter.")
        }

        if (transcript.contains("send", ignoreCase = true)) {
            actionItems.add("Draft and deliver requested resources.")
        }
        if (transcript.contains("tomorrow", ignoreCase = true)) {
            actionItems.add("Follow up on core timeline tomorrow.")
        }
        if (transcript.contains("call", ignoreCase = true) && actionItems.size < 3) {
            actionItems.add("Arrange immediate validation callback session.")
        }

        if (actionItems.isEmpty()) {
            actionItems.add("Verify task details discussed during session.")
        }

        return SummaryOutput(summary, actionItems.distinct())
    }

    private fun localSmsFallback(body: String, linkedProjects: String): SmsOutput {
        val score: Int
        val level: String
        val reasoning: String

        val bodyLower = body.lowercase()

        if (bodyLower.contains("asap") || bodyLower.contains("urgent") || bodyLower.contains("broken") || bodyLower.contains("crash") || bodyLower.contains("stop")) {
            score = 9
            level = "Critical"
            reasoning = "High-urgency blocker keywords identified in secure transmission; immediate response required."
        } else if (bodyLower.contains("delay") || bodyLower.contains("late") || bodyLower.contains("wrong") || bodyLower.contains("cannot")) {
            score = 7
            level = "Urgent"
            reasoning = "Operational bottleneck reported directly impacting current active client milestone delivery."
        } else if (bodyLower.contains("meeting") || bodyLower.contains("discuss") || bodyLower.contains("schedule") || bodyLower.contains("update")) {
            score = 5
            level = "High"
            reasoning = "Scheduling request to adjust client review sync loops; critical for alignment."
        } else {
            score = 3
            level = "Normal"
            reasoning = "General informational SMS received. Action is non-blocking with standard latency."
        }

        return SmsOutput(
            urgencyScore = score,
            priorityLevel = level,
            categoryReasoning = reasoning
        )
    }
}
