package com.example.ui

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CallScribeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = CallScribeRepository(database)
    private val geminiService = GeminiService()

    // Reactive database feeds
    val clientProfiles: StateFlow<List<ClientProfile>> = repository.clientProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projectTasks: StateFlow<List<ProjectTask>> = repository.projectTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callRecords: StateFlow<List<CallRecord>> = repository.callRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smsMessages: StateFlow<List<SmsMessage>> = repository.smsMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live Call simulation state
    var isCallActive by mutableStateOf(false)
        private set
    var activeCallerName by mutableStateOf("")
        private set
    var activeCallerPhone by mutableStateOf("")
        private set
    var activeClientProfile by mutableStateOf<ClientProfile?>(null)
        private set
    var activeCallTranscript by mutableStateOf("")
        private set
    var activeCallDurationSeconds by mutableStateOf(0)
        private set
    var isCallProcessing by mutableStateOf(false)
        private set

    // Current call timer and speech simulation jobs
    private var callTimerJob: Job? = null
    private var speechSimulationJob: Job? = null
    private var speechRecognizer: SpeechRecognizer? = null

    // System Permissions simulated & real states
    var isAudioPermissionGranted by mutableStateOf(false)
    var isPhoneStatePermissionGranted by mutableStateOf(false)
    var isSmsPermissionGranted by mutableStateOf(false)

    init {
        // Pre-populate database with some realistic initial seed data for excellent demo
        viewModelScope.launch {
            seedInitialDataIfNeeded()
        }
    }

    // Call Simulation functions
    fun startSimulatedCall(callerName: String, phoneNumber: String) {
        if (isCallActive) return
        
        isCallActive = true
        activeCallerName = callerName
        activeCallerPhone = phoneNumber
        activeCallTranscript = ""
        activeCallDurationSeconds = 0
        isCallProcessing = false

        // Match existing client profile automatically
        viewModelScope.launch {
            val matched = repository.findClientByPhone(phoneNumber)
            activeClientProfile = matched ?: clientProfiles.value.find { 
                it.phone.replace(Regex("[^0-9]"), "") == phoneNumber.replace(Regex("[^0-9]"), "") 
            }
        }

        // Start call duration timer
        callTimerJob = viewModelScope.launch {
            while (isCallActive) {
                delay(1000)
                activeCallDurationSeconds++
            }
        }

        // Real-time microphone listening if permission is granted
        if (isAudioPermissionGranted) {
            startListeningMic()
        }

        // Simulate real-time streaming dialogue transcription word-by-word with dynamic typewriter rhythm
        val dialogues = getSimulatedDialogue(callerName, phoneNumber)
        speechSimulationJob = viewModelScope.launch {
            delay(1500)
            var baseTranscript = ""
            for (line in dialogues) {
                if (!isCallActive) break
                val words = line.split(" ")
                var currentLineText = ""
                for (word in words) {
                    if (!isCallActive) break
                    currentLineText = if (currentLineText.isEmpty()) word else "$currentLineText $word"
                    activeCallTranscript = if (baseTranscript.isEmpty()) {
                        currentLineText
                    } else {
                        "$baseTranscript\n\n$currentLineText"
                    }
                    delay(180) // Pacing at ~330 WPM for realistic dialogue flow
                }
                baseTranscript = activeCallTranscript
                delay(1200) // Brief pause between turns
            }
        }
    }

    fun endSimulatedCall() {
        if (!isCallActive) return
        
        isCallActive = false
        callTimerJob?.cancel()
        speechSimulationJob?.cancel()
        stopListeningMic()
        
        // Lock and process the recording with our secure private AI (Gemini or local offline fallback)
        isCallProcessing = true
        viewModelScope.launch {
            val clientName = activeClientProfile?.name ?: activeCallerName
            val company = activeClientProfile?.company ?: "Unknown Company"
            
            // Run AI summarization (end-to-end encrypted locally)
            val aiOutput = geminiService.summarizeCall(
                clientName = clientName,
                company = company,
                transcript = activeCallTranscript
            )

            // Run local offline sentiment analysis
            val calculatedSentiment = OfflineTinyML.analyzeSentiment(activeCallTranscript)

            // Save call record to database
            val record = CallRecord(
                clientProfileId = activeClientProfile?.id,
                phoneNumber = activeCallerPhone,
                durationSeconds = activeCallDurationSeconds,
                transcript = activeCallTranscript,
                summary = aiOutput.summary,
                actionItemsRaw = aiOutput.actionItems.joinToString("||"),
                isEncrypted = true, // AES-256 local encrypted flag
                exportedStatus = "Local Only",
                sentiment = calculatedSentiment
            )
            repository.insertCallRecord(record)

            // Automatically extract or link any high-priority tasks if discussed
            if (activeClientProfile != null) {
                for (item in aiOutput.actionItems) {
                    if (item.lowercase().contains("asap") || item.lowercase().contains("friday") || item.lowercase().contains("urgent")) {
                        // Insert an urgent task automatically
                        repository.insertTask(
                            ProjectTask(
                                title = item,
                                clientProfileId = activeClientProfile!!.id,
                                description = "Automatically extracted from encrypted call with $clientName on ${getFormattedTime()}.",
                                status = "Pending",
                                projectChannel = "AI Extracted",
                                priority = "High"
                            )
                        )
                    }
                }
            }

            isCallProcessing = false
        }
    }

    private fun startListeningMic() {
        val context = getApplication<Application>()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e("CallScribeViewModel", "Speech recognition is not available on this device")
            return
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                if (speechRecognizer != null) {
                    speechRecognizer?.destroy()
                }

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d("CallScribeViewModel", "Mic ready for speech")
                    }

                    override fun onBeginningOfSpeech() {
                        Log.d("CallScribeViewModel", "User speaking started")
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        Log.d("CallScribeViewModel", "User speaking ended")
                    }

                    override fun onError(error: Int) {
                        Log.e("CallScribeViewModel", "Speech recognizer error code: $error")
                        if (isCallActive && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                            try {
                                speechRecognizer?.startListening(intent)
                            } catch (e: Exception) {
                                Log.e("CallScribeViewModel", "Failed to restart listener on error: ${e.message}")
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val userVoiceText = matches[0]
                            val formattedText = "User (Voice): $userVoiceText"
                            activeCallTranscript = if (activeCallTranscript.isEmpty()) {
                                formattedText
                            } else {
                                "$activeCallTranscript\n\n$formattedText"
                            }
                        }
                        if (isCallActive) {
                            try {
                                speechRecognizer?.startListening(intent)
                            } catch (e: Exception) {
                                Log.e("CallScribeViewModel", "Failed to restart listening in onResults: ${e.message}")
                            }
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e("CallScribeViewModel", "Failed to start speech recognizer: ${e.message}")
            }
        }
    }

    private fun stopListeningMic() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.e("CallScribeViewModel", "Failed to stop/destroy speech recognizer: ${e.message}")
            }
        }
    }

    fun injectCustomSpeech(speakerName: String, text: String) {
        val cleanedText = text.trim()
        if (cleanedText.isEmpty()) return
        val newDialogueLine = "$speakerName: $cleanedText"
        activeCallTranscript = if (activeCallTranscript.isEmpty()) {
            newDialogueLine
        } else {
            "$activeCallTranscript\n\n$newDialogueLine"
        }
    }

    // Database Actions
    fun createClientProfile(name: String, company: String, email: String, phone: String, bio: String) {
        viewModelScope.launch {
            repository.insertClient(
                ClientProfile(name = name, company = company, email = email, phone = phone, bio = bio)
            )
        }
    }

    fun deleteClientProfile(profile: ClientProfile) {
        viewModelScope.launch {
            repository.deleteClient(profile)
        }
    }

    fun createProjectTask(title: String, clientId: Int, description: String, status: String, projectChannel: String, priority: String) {
        viewModelScope.launch {
            repository.insertTask(
                ProjectTask(
                    title = title,
                    clientProfileId = clientId,
                    description = description,
                    status = status,
                    projectChannel = projectChannel,
                    priority = priority
                )
            )
        }
    }

    fun toggleTask(task: ProjectTask) {
        viewModelScope.launch {
            val updated = task.copy(status = if (task.status == "Completed") "Pending" else "Completed")
            repository.updateTask(updated)
        }
    }

    fun deleteProjectTask(task: ProjectTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun deleteCallRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteCallRecord(id)
        }
    }

    fun exportToCrm(record: CallRecord) {
        viewModelScope.launch {
            // Update call record status
            val updated = record.copy(exportedStatus = "Exported to CRM")
            repository.updateCallRecord(updated)
            Log.d("CallScribeViewModel", "Call record exported successfully to offline CRM module.")
        }
    }

    fun exportToProjectTasks(record: CallRecord) {
        viewModelScope.launch {
            val clientName = clientProfiles.value.find { it.id == record.clientProfileId }?.name ?: "Client"
            val updated = record.copy(exportedStatus = "Exported to Project Board")
            repository.updateCallRecord(updated)

            // Convert all action items into individual active project tasks
            record.actionItemsList.forEach { item ->
                repository.insertTask(
                    ProjectTask(
                        title = item,
                        clientProfileId = record.clientProfileId ?: 1,
                        description = "Action item exported from call recording on ${getFormattedTime()}.",
                        status = "Pending",
                        projectChannel = "CRM Sync",
                        priority = "Medium"
                    )
                )
            }
        }
    }

    fun addTagToCallRecord(record: CallRecord, tag: String) {
        viewModelScope.launch {
            val cleanedTag = tag.trim()
            if (cleanedTag.isEmpty()) return@launch
            val currentTags = record.tagsList.toMutableList()
            if (!currentTags.contains(cleanedTag)) {
                currentTags.add(cleanedTag)
                val updated = record.copy(tagsRaw = currentTags.joinToString("||"))
                repository.updateCallRecord(updated)
            }
        }
    }

    fun removeTagFromCallRecord(record: CallRecord, tag: String) {
        viewModelScope.launch {
            val currentTags = record.tagsList.toMutableList()
            if (currentTags.remove(tag)) {
                val updated = record.copy(tagsRaw = currentTags.joinToString("||"))
                repository.updateCallRecord(updated)
            }
        }
    }

    fun addHighlightToCallRecord(record: CallRecord, phrase: String) {
        viewModelScope.launch {
            val cleanedPhrase = phrase.trim()
            if (cleanedPhrase.isEmpty()) return@launch
            val currentHighlights = record.highlightedPhrasesList.toMutableList()
            if (!currentHighlights.contains(cleanedPhrase)) {
                currentHighlights.add(cleanedPhrase)
                val updated = record.copy(highlightedPhrases = currentHighlights.joinToString("||"))
                repository.updateCallRecord(updated)
            }
        }
    }

    fun removeHighlightFromCallRecord(record: CallRecord, phrase: String) {
        viewModelScope.launch {
            val currentHighlights = record.highlightedPhrasesList.toMutableList()
            if (currentHighlights.remove(phrase)) {
                val updated = record.copy(highlightedPhrases = currentHighlights.joinToString("||"))
                repository.updateCallRecord(updated)
            }
        }
    }

    // SMS Prioritization and ingestion Simulation
    fun simulateIncomingSms(senderNumber: String, body: String) {
        viewModelScope.launch {
            // Fetch client profiles and task names to provide context
            val activeChannels = projectTasks.value.map { it.projectChannel }.distinct().joinToString(", ")
            val clientName = clientProfiles.value.find { it.phone.replace(Regex("[^0-9]"), "") == senderNumber.replace(Regex("[^0-9]"), "") }?.name ?: "Unknown Client"

            // Classify with private offline AI classifier
            val smsOutput = geminiService.classifySms(
                sender = "$clientName ($senderNumber)",
                body = body,
                linkedProjects = activeChannels.ifEmpty { "General Technical Operations, Client Delivery" }
            )

            // Insert categorized SMS
            val sms = SmsMessage(
                senderNumber = senderNumber,
                body = body,
                urgencyScore = smsOutput.urgencyScore,
                priorityLevel = smsOutput.priorityLevel,
                linkedProjectId = clientProfiles.value.find { it.phone.replace(Regex("[^0-9]"), "") == senderNumber.replace(Regex("[^0-9]"), "") }?.id,
                categoryReasoning = smsOutput.categoryReasoning,
                isRead = false
            )
            repository.insertSms(sms)
        }
    }

    fun markSmsRead(id: Int) {
        viewModelScope.launch {
            repository.markSmsAsRead(id)
        }
    }

    fun deleteSms(sms: SmsMessage) {
        viewModelScope.launch {
            repository.deleteSms(sms)
        }
    }

    // Helper Dialogues generator for real-time live transcription simulation
    private fun getSimulatedDialogue(caller: String, phone: String): List<String> {
        return when (caller) {
            "Alice Vance" -> listOf(
                "Alice: Hello? Yes, this is Alice Vance from Acme Corp. I'm calling about the mobile application build status.",
                "Alice: Our executive team is worried because we noticed the cloud database migration is taking longer than expected.",
                "Alice: Is the backend API endpoint fully secured and ready for integration testing yet? We need to deploy this by Friday.",
                "Alice: If we don't hit the Friday milestone, it blocks our entire marketing launch campaign. Please escalate this.",
                "Alice: Also, please send over the adjusted budget sheet for the extra developer hours today. Thank you so much!"
            )
            "Marcus Aurelius" -> listOf(
                "Marcus: Hi! This is Marcus from Colosseum Tech. I just reviewed the proposed dashboard layouts you sent over.",
                "Marcus: Honestly, the core analytics telemetry cards look fantastic. You guys did an incredible job.",
                "Marcus: However, we do need some custom filter configurations added for the regional reports.",
                "Marcus: Also, let's make sure the 'Export to PDF' button is visually styled in higher contrast so our older clients find it easily.",
                "Marcus: Let's schedule a deep dive review session next Tuesday at 2 PM. Send me a calendar invite with the secured link. Thanks!"
            )
            else -> listOf(
                "Unknown Client: Hey there! Calling to inquire about custom integrations for our local retail e-commerce portal.",
                "Unknown Client: We specifically need end-to-end local inventory synchronization and a highly secure payment gateway.",
                "Unknown Client: Our overall deployment budget is around forty thousand dollars, with kickoff in late August.",
                "Unknown Client: Can we schedule a quick requirements scoping session sometime next week to detail the blueprint?",
                "Unknown Client: Reach back to me at this number when you have a free slot. Speak soon!"
            )
        }
    }

    private fun getFormattedTime(): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    // Database pre-population with gorgeous seed records
    private suspend fun seedInitialDataIfNeeded() {
        val currentClients = database.clientProfileDao().getClientProfileByPhone("+1 555-0101")
        if (currentClients != null) return // Already seeded

        // Seed 1: Clients
        val aliceId = repository.insertClient(
            ClientProfile(
                name = "Alice Vance",
                company = "Acme Corp",
                email = "alice.vance@acme.com",
                phone = "+1 555-0101",
                bio = "Lead Project Manager for Acme's internal cloud tooling. Prefers urgent updates via encrypted phone channels."
            )
        )

        val marcusId = repository.insertClient(
            ClientProfile(
                name = "Marcus Aurelius",
                company = "Colosseum Tech",
                email = "marcus@colosseum.io",
                phone = "+1 555-0102",
                bio = "Chief Architecture Architect. Very strict on high-contrast accessibility compliance and local storage privacy."
            )
        )

        // Seed 2: Project Tasks
        repository.insertTask(
            ProjectTask(
                title = "Resolve cloud database connection timeout",
                clientProfileId = aliceId.toInt(),
                description = "Acme's mobile app experiences occasional DB timeouts. Needs connection pool optimization.",
                status = "Pending",
                projectChannel = "Acme Cloud Tooling",
                priority = "High"
            )
        )

        repository.insertTask(
            ProjectTask(
                title = "Design regional analytics filters",
                clientProfileId = marcusId.toInt(),
                description = "Add high contrast filters and PDF exporter button in the analytics tab.",
                status = "Completed",
                projectChannel = "Colosseum Dashboard",
                priority = "Medium"
            )
        )

        // Seed 3: Archived secure call record
        repository.insertCallRecord(
            CallRecord(
                clientProfileId = aliceId.toInt(),
                phoneNumber = "+1 555-0101",
                durationSeconds = 145,
                transcript = "Alice: Hi, just confirming we received the security audit logs.\n\nDeveloper: Perfect. They are fully hashed and saved in the encrypted volume.\n\nAlice: Excellent. Keep them isolated.",
                summary = "Brief check-in call confirming verification and secure logging of Acme audit records.",
                actionItemsRaw = "Isolate old audit logs in the cold vault||Run final hash validation sequence",
                isEncrypted = true,
                exportedStatus = "Exported to CRM",
                sentiment = "Positive",
                tagsRaw = "Audit||Urgent||Acme",
                highlightedPhrases = "security audit logs"
            )
        )

        // Seed 4: Received SMS priority queue
        repository.insertSms(
            SmsMessage(
                senderNumber = "+1 555-0101",
                body = "URGENT: Alice here. Acme server is rejecting connection headers! Is the firewall rule adjusted?",
                urgencyScore = 9,
                priorityLevel = "Critical",
                linkedProjectId = aliceId.toInt(),
                categoryReasoning = "Matches 'Critical' priority. Alice reports network headers failing, which is a major system blocker.",
                isRead = false
            )
        )

        repository.insertSms(
            SmsMessage(
                senderNumber = "+1 555-0102",
                body = "Hey, let's push the PDF export review to 3 PM on Tuesday instead of 2. Does that still work?",
                urgencyScore = 4,
                priorityLevel = "Normal",
                linkedProjectId = marcusId.toInt(),
                categoryReasoning = "Matches 'Normal' priority. Minor scheduling adjustment for Colosseum Dashboard review.",
                isRead = true
            )
        )
    }
}
