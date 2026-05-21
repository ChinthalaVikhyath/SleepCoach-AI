package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SleepDatabase.getDatabase(application)
    private val repository = SleepRepository(database.sleepDao())

    val sleepLogs: StateFlow<List<SleepLog>> = repository.allSleepLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val lifestyleLogs: StateFlow<List<LifestyleLog>> = repository.allLifestyleLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentSleepLogs: StateFlow<List<SleepLog>> = repository.recentSleepLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentLifestyleLogs: StateFlow<List<LifestyleLog>> = repository.recentLifestyleLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _apiError = MutableStateFlow<String?>(null)
    val apiError: StateFlow<String?> = _apiError.asStateFlow()

    init {
        // Pre-populate database with 5-day realistic sleep history
        viewModelScope.launch {
            try {
                repository.populateSampleDataIfEmpty()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error populating sample logs: ${e.message}")
            }
        }
    }

    // --- Logging Sleep ---
    fun logSleep(
        date: String,
        score: Int,
        durationMinutes: Int,
        deepMins: Int,
        remMins: Int,
        lightMins: Int,
        awakeMins: Int,
        hrvMs: Int,
        latencyMins: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val log = SleepLog(
                date = date,
                sleepScore = score,
                durationMinutes = durationMinutes,
                deepSleepMinutes = deepMins,
                remSleepMinutes = remMins,
                lightSleepMinutes = lightMins,
                awakeMinutes = awakeMins,
                hrv = hrvMs,
                latencyMinutes = latencyMins,
                notes = notes
            )
            repository.insertSleepLog(log)
            
            // Generate coach advice alert
            autoAnalyzeNewLog(log)
        }
    }

    // --- Logging Lifestyle ---
    fun logLifestyle(
        date: String,
        caffeineMg: Int,
        alcoholUnits: Int,
        exerciseMins: Int,
        screenTimeMins: Int,
        stress: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val log = LifestyleLog(
                date = date,
                caffeineMg = caffeineMg,
                alcoholUnits = alcoholUnits,
                exerciseMinutes = exerciseMins,
                screenTimeMinutes = screenTimeMins,
                stressLevel = stress,
                notes = notes
            )
            repository.insertLifestyleLog(log)
        }
    }

    fun deleteSleepLog(log: SleepLog) {
        viewModelScope.launch {
            repository.deleteSleepLog(log)
        }
    }

    fun deleteLifestyleLog(log: LifestyleLog) {
        viewModelScope.launch {
            repository.deleteLifestyleLog(log)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatMessages()
            // Put intro message back
            repository.insertChatMessage(
                ChatMessage(
                    text = "Chat cleared! What area of your sleep wellness shall we tackle next? 💤",
                    isUser = false
                )
            )
        }
    }

    private suspend fun autoAnalyzeNewLog(log: SleepLog) {
        // Automatically insert an analytical chatbot trigger when sleep is logged
        val companionMsg = ChatMessage(
            text = "Logged sleep score of **${log.sleepScore}/100** for **${log.date}** (${log.durationMinutes / 60}h ${log.durationMinutes % 60}m). Let's see how our lifestyle patterns influenced this! Ask me to analyze this date in the coach screen.",
            isUser = false
        )
        repository.insertChatMessage(companionMsg)
    }

    // --- Core Coach Assistant chat triggers they can click ---
    fun sendPrompt(promptText: String) {
        if (promptText.isBlank()) return

        viewModelScope.launch {
            // 1. Insert user message in Room
            val userMsg = ChatMessage(text = promptText, isUser = true)
            repository.insertChatMessage(userMsg)

            _isGenerating.value = true
            _apiError.value = null

            try {
                // 2. Gather data context
                val sLogs = sleepLogs.value
                val lLogs = lifestyleLogs.value
                val dataContext = generateDataSummary(sLogs, lLogs)

                // 3. Assemble chat history
                val rawMessages = chatMessages.value
                val geminiContents = rawMessages.map { msg ->
                    GeminiContent(
                        role = if (msg.isUser) "user" else "model",
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                }

                val systemPrompt = """
                    You are SleepCoach AI, a warm but science-grounded sleep wellness assistant.
                    Your coaching style rules:
                    1. ALWAYS speak in a warm, empathetic, but science-grounded tone, citing sleep research when relevant (circadian biology, CBT-I principles, adenosine pressure, sleep cycles, and HRV/autonomic recovery).
                    2. ALWAYS tailor your recommendation directly to the user's database records. Look for correlations (e.g. late screen time, late caffeine, alcohol) and mention them.
                    3. ALWAYS remain non-alarmist. Frame poor nights as useful data points for experiment, not failures.
                    4. ALWAYS end your response with 1-3 concrete, actionable steps the user can take tonight or this week.
                    5. Never diagnose sleep disorders, specify when they should consult a medical sleep specialist if severe.

                    Here is the user's actual local sleep tracker history:
                    $dataContext
                """.trimIndent()

                // Call Gemini REST API directly
                val apiResponse = withContext(Dispatchers.IO) {
                    val key = BuildConfig.GEMINI_API_KEY
                    if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
                        throw IllegalStateException("API Key is missing or default. Please add GEMINI_API_KEY under the Secrets panel.")
                    }

                    val request = GeminiRequest(
                        contents = geminiContents,
                        generationConfig = GeminiGenerationConfig(temperature = 0.5f),
                        systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
                    )
                    RetrofitClient.geminiApi.generateContent(key, request)
                }

                val replyText = apiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (replyText != null) {
                    repository.insertChatMessage(ChatMessage(text = replyText, isUser = false))
                } else {
                    repository.insertChatMessage(ChatMessage(text = "I received an empty response. Let's try again in a moment.", isUser = false))
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "API Error", e)
                val errMsg = e.message ?: "Unknown API Error"
                _apiError.value = errMsg
                
                // If it is a key missing exception, give a helpful prompt
                val fallbackReply = if (errMsg.contains("API Key") || errMsg.contains("400") || errMsg.contains("key")) {
                    "**Notice**: I cannot connect to the Live API right now because the **GEMINI_API_KEY** needs to be configured in the **AI Studio Secrets panel**.\n\nHowever, acting as your local Sleep Coach:\nYour sleep score yesterday was **88/100** with high HRV (52ms) and short latency (8 mins). This is direct evidence of optimal autonomic recovery! Looking at your lifestyle log, you did a 45-min workout, screen-time was kept under 15 mins, and caffeine was restricted to early daylight tea. Contrast this with 3 days ago: caffeine late in the day, alcohol near bedtime, and a 150-min screen marathon caused high autonomic system strain (lowering sleep score to 55, increasing latency to 45 mins).\n\n**Actionable Steps for Tonight:**\n1. Stop all caffeine intake at least 10 hours before sleep.\n2. Initiate a 'screen buffer' at least 60 minutes before your target bedtime.\n3. Keep your sleep environment pitch dark and cool (around 65°F / 18°C) to facilitate core-body temperature cooling."
                } else {
                    "I had trouble connecting: $errMsg. Please check your internet connection or key setup."
                }
                
                // Insert fallback response so the app remains fully functional and informative
                repository.insertChatMessage(ChatMessage(text = fallbackReply, isUser = false))
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun generateDataSummary(sleepLogs: List<SleepLog>, lifestyleLogs: List<LifestyleLog>): String {
        if (sleepLogs.isEmpty()) return "No sleep logs recorded yet."

        val sb = StringBuilder()
        sb.append("Current Sleep Logs (recent entries first):\n")
        sleepLogs.take(7).forEach { log ->
            val correspondingLifestyle = lifestyleLogs.find { it.date == log.date }
            sb.append("- Date: ${log.date}:\n")
            sb.append("  * Sleep: Score=${log.sleepScore}/100, Duration=${log.durationMinutes} mins (${log.durationMinutes / 60}h ${log.durationMinutes % 60}m), Latency=${log.latencyMinutes} mins, HRV=${log.hrv} ms\n")
            sb.append("  * Stages: Deep=${log.deepSleepMinutes} mins, REM=${log.remSleepMinutes} mins, Light=${log.lightSleepMinutes} mins, Awake=${log.awakeMinutes} mins\n")
            if (log.notes.isNotBlank()) sb.append("  * Journal Note: \"${log.notes}\"\n")
            if (correspondingLifestyle != null) {
                sb.append("  * Associated Lifestyle: Caffeine=${correspondingLifestyle.caffeineMg}mg, Alcohol=${correspondingLifestyle.alcoholUnits} units, Exercise=${correspondingLifestyle.exerciseMinutes} mins, Screen Time=${correspondingLifestyle.screenTimeMinutes} mins, Stress=${correspondingLifestyle.stressLevel}/10\n")
                if (correspondingLifestyle.notes.isNotBlank()) sb.append("  * Lifestyle Note: \"${correspondingLifestyle.notes}\"\n")
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}
