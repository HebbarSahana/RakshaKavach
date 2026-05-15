package com.example.kavach

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SafetyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirebaseRepository()
    private val safetyDao = SafetyDatabase.getDatabase(application).safetyDao()

    private val allTaskNames = listOf(
        "Welding", "Trenching", "Height Work", "Chemical Handling", "Electrical Work",
        "Confined Space", "Forklift Operation", "Scaffolding", "Machinery Maintenance", "Excavation",
        "Heavy Lifting", "Demolition", "Roofing", "Power Tools", "Painting",
        "Crane Operation", "Foundry Work", "Laboratory Testing", "Woodworking", "Pipeline Inspection",
        "Warehouse Loading", "Blast Cleaning", "Metal Cutting", "Asphalt Paving", "Logging"
    )

    private val _tasks = MutableStateFlow(
        listOf(
            Task("1", "Welding", RiskLevel.HIGH, listOf("Auto-darkening Helmet", "Leather Gloves", "Fire-resistant Apron")),
            Task("2", "Trenching", RiskLevel.EXTREME, listOf("Hard Hat", "Steel-toed Boots", "High-visibility Vest")),
            Task("3", "Height Work", RiskLevel.EXTREME, listOf("Full-body Harness", "Lanyard", "Hard Hat")),
            Task("4", "Chemical Handling", RiskLevel.MEDIUM, listOf("Respirator", "Chemical Gloves", "Goggles")),
            Task("5", "Electrical Work", RiskLevel.HIGH, listOf("Insulated Gloves", "Voltage Tester", "Safety Glasses")),
            Task("6", "Confined Space", RiskLevel.EXTREME, listOf("Gas Monitor", "Tripod", "Rescue Winch")),
            Task("7", "Forklift Operation", RiskLevel.MEDIUM, listOf("Seatbelt", "High-visibility Vest", "Safety Shoes")),
            Task("8", "Scaffolding", RiskLevel.HIGH, listOf("Safety Net", "Toe Boards", "Guardrails")),
            Task("9", "Machinery Maintenance", RiskLevel.MEDIUM, listOf("Lockout Tagout", "Emergency Stop", "Safety Guards")),
            Task("10", "Excavation", RiskLevel.EXTREME, listOf("Shoring", "Ladder", "Barrier Tape")),
            Task("11", "Heavy Lifting", RiskLevel.MEDIUM, listOf("Back Support", "Steel-toed Boots", "Grip Gloves")),
            Task("12", "Demolition", RiskLevel.EXTREME, listOf("Dust Mask", "Ear Plugs", "Impact Goggles")),
            Task("13", "Roofing", RiskLevel.HIGH, listOf("Fall Arrest Kit", "Soft-sole Shoes", "UV Protection")),
            Task("14", "Power Tools", RiskLevel.MEDIUM, listOf("Face Shield", "Hearing Protection", "Cut-resistant Gloves")),
            Task("15", "Painting", RiskLevel.LOW, listOf("Respiratory Mask", "Disposable Coveralls", "Eye Protection")),
            Task("16", "Crane Operation", RiskLevel.HIGH, listOf("Hard Hat", "High-visibility Vest", "Communication Radio")),
            Task("17", "Foundry Work", RiskLevel.EXTREME, listOf("Heat-resistant Suit", "Face Shield", "Insulated Gloves")),
            Task("18", "Laboratory Testing", RiskLevel.LOW, listOf("Lab Coat", "Safety Goggles", "Nitrile Gloves")),
            Task("19", "Woodworking", RiskLevel.MEDIUM, listOf("Dust Mask", "Safety Glasses", "Hearing Protection")),
            Task("20", "Pipeline Inspection", RiskLevel.HIGH, listOf("Gas Detector", "Protective Coveralls", "Boots")),
            Task("21", "Warehouse Loading", RiskLevel.MEDIUM, listOf("Steel-toed Boots", "Back Brace", "High-visibility Vest")),
            Task("22", "Blast Cleaning", RiskLevel.EXTREME, listOf("Blasting Hood", "Air-fed Respirator", "Heavy-duty Gloves")),
            Task("23", "Metal Cutting", RiskLevel.HIGH, listOf("Face Shield", "Apron", "Ear Muffs")),
            Task("24", "Asphalt Paving", RiskLevel.MEDIUM, listOf("Heat-resistant Boots", "Reflective Vest", "Gloves")),
            Task("25", "Logging", RiskLevel.EXTREME, listOf("Chainsaw Chaps", "Logging Helmet", "Steel-toed Boots"))
        )
    )
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()

    private val _ppeChecklist = MutableStateFlow<List<PPEItem>>(emptyList())
    val ppeChecklist: StateFlow<List<PPEItem>> = _ppeChecklist.asStateFlow()

    private val _userStats = MutableStateFlow(UserStats(score = 0, safeStreak = 0, rank = "Bronze"))
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private val _selectedLanguage = MutableStateFlow<String?>(null)
    val selectedLanguage: StateFlow<String?> = _selectedLanguage.asStateFlow()

    private val _currentQuizTopic = MutableStateFlow<String?>(null)
    val currentQuizTopic: StateFlow<String?> = _currentQuizTopic.asStateFlow()

    private val _currentQuizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val currentQuizQuestions: StateFlow<List<QuizQuestion>> = _currentQuizQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _auditResult = MutableStateFlow("")
    val auditResult: StateFlow<String> = _auditResult.asStateFlow()

    private val _isAuditing = MutableStateFlow(false)
    val isAuditing: StateFlow<Boolean> = _isAuditing.asStateFlow()

    private val _quizFeedback = MutableStateFlow<String?>(null)
    val quizFeedback: StateFlow<String?> = _quizFeedback.asStateFlow()

    private val _incidents = MutableStateFlow<List<Incident>>(emptyList())
    val incidents: StateFlow<List<Incident>> = _incidents.asStateFlow()

    private val _sttText = MutableStateFlow("")
    val sttText: StateFlow<String> = _sttText.asStateFlow()

    private val _isAssistantSpeaking = MutableStateFlow(false)
    val isAssistantSpeaking: StateFlow<Boolean> = _isAssistantSpeaking.asStateFlow()

    init {
        viewModelScope.launch {
            safetyDao.getAllIncidents().collectLatest {
                _incidents.value = it
            }
        }
        fetchRemoteTasks()
    }

    private fun fetchRemoteTasks() {
        viewModelScope.launch {
            val remoteTasks = repository.getSafetyTasks()
            if (remoteTasks.isNotEmpty()) {
                _tasks.value = remoteTasks.map { remote ->
                    Task(
                        id = remote.id,
                        name = remote.taskName,
                        riskLevel = try { RiskLevel.valueOf(remote.riskLevel) } catch (e: Exception) { RiskLevel.LOW },
                        requiredPPE = remote.requiredPPE
                    )
                }
            }
        }
    }

    private fun generateCommonQuiz(taskName: String, lang: String): List<QuizQuestion> {
        val qBase = when (lang) {
            "Hindi" -> listOf(
                "क्या %s के लिए पीपीई अनिवार्य है?" to "हाँ",
                "%s शुरू करने से पहले क्या जांचना चाहिए?" to "सुरक्षा उपकरण",
                "क्या %s के दौरान सुरक्षा हेलमेट पहनना चाहिए?" to "हाँ",
                "%s के लिए सुरक्षा चश्मा क्यों आवश्यक है?" to "आंखों की सुरक्षा",
                "क्या %s के दौरान दस्ताने पहनने चाहिए?" to "हाँ",
                "सुरक्षा बेल्ट का उपयोग कब करें?" to "ऊंचाई पर काम करते समय",
                "क्या कार्यस्थल पर धूम्रपान करना चाहिए?" to "नहीं",
                "आपातकालीन स्थिति में क्या करें?" to "तुरंत सूचित करें",
                "क्या प्राथमिक चिकित्सा किट का स्थान पता होना चाहिए?" to "हाँ",
                "क्या सुरक्षा नियमों का पालन करना अनिवार्य है?" to "हाँ"
            )
            "Kannada" -> listOf(
                "%s ಗೆ ಪಿಪಿಇ ಕಡ್ಡಾಯವೇ?" to "ಹೌದು",
                "%s ಪ್ರಾರಂಭಿಸುವ ಮೊದಲು ಏನನ್ನು ಪರೀಕ್ಷಿಸಬೇಕು?" to "ಸುರಕ್ಷತಾ ಸಾಧನಗಳು",
                "ಕೆಲಸದ ಸಮಯದಲ್ಲಿ ಹೆಲ್ಮೆಟ್ ಧರಿಸಬೇಕೆ?" to "ಹೌದು",
                "ಕಣ್ಣಿನ ರಕ್ಷಣೆ ಏಕೆ ಮುಖ್ಯ?" to "ಗಾಯಗಳನ್ನು ತಪ್ಪಿಸಲು",
                "ಕೈಗವಸುಗಳನ್ನು ಧರಿಸಬೇಕೆ?" to "ಹೌದು",
                "ಸೇಫ್ಟಿ ಬೆಲ್ಟ್ ಯಾವಾಗ ಬೇಕು?" to "ಎತ್ತರದಲ್ಲಿ ಕೆಲಸ ಮಾಡುವಾಗ",
                "ಧೂಮಪಾನ ಮಾಡಬಹುದೆ?" to "ಇಲ್ಲ",
                "ತುರ್ತು ಪರಿಸ್ಥಿತಿಯಲ್ಲಿ ಏನು ಮಾಡಬೇಕು?" to "ತಕ್ಷಣ ವರದಿ ಮಾಡಿ",
                "ಪ್ರಥಮ ಚಿಕಿತ್ಸಾ ಪೆಟ್ಟಿಗೆ ಎಲ್ಲಿದೆ ಎಂದು ಗೊತ್ತಿರಬೇಕೆ?" to "ಹೌದು",
                "ಸುರಕ್ಷತಾ ನಿಯಮಗಳನ್ನು ಪಾಲಿಸಬೇಕೆ?" to "ಹೌದು"
            )
            else -> listOf(
                "Is PPE mandatory for %s?" to "Yes",
                "What should be checked before starting %s?" to "Safety Gear",
                "Should you wear a helmet during %s?" to "Yes",
                "Why are safety goggles needed?" to "To protect eyes",
                "Should you wear gloves?" to "Yes",
                "When to use a safety harness?" to "Working at heights",
                "Is smoking allowed at workplace?" to "No",
                "What to do in an emergency?" to "Report immediately",
                "Should you know first aid location?" to "Yes",
                "Is safety training required?" to "Yes"
            )
        }
        return (1..10).map { i ->
            val pair = qBase[(i - 1) % qBase.size]
            QuizQuestion(
                question = pair.first.format(taskName) + " (Q$i)",
                options = listOf(pair.second, "No", "Maybe", "Not sure").shuffled(),
                correctAnswer = pair.second,
                explanation = "Standard industrial safety procedure for $taskName."
            )
        }
    }

    fun completeOnboarding(name: String, id: String) {
        val uid = UUID.randomUUID().toString()
        _userStats.value = _userStats.value.copy(name = name, id = id)
        viewModelScope.launch {
            repository.saveUserData(UserData(uid = uid, name = name, streak = 0, points = 0))
        }
    }

    fun selectTask(task: Task) {
        _selectedTask.value = task
        _ppeChecklist.value = task.requiredPPE.map { name ->
            val icon = when {
                name.contains("Helmet") || name.contains("Hard Hat") -> Icons.Default.Shield
                name.contains("Gloves") -> Icons.Default.FrontHand
                name.contains("Boots") || name.contains("Shoes") -> Icons.Default.CheckCircle
                else -> Icons.Default.Build
            }
            PPEItem(name, icon = icon)
        }
        _auditResult.value = ""
    }

    fun saveIncident(description: String, severity: String, location: String, date: String, time: String) {
        viewModelScope.launch {
            val incident = Incident(description = description, severity = severity, location = location, date = date, time = time)
            safetyDao.insertIncident(incident)
            repository.saveIncidentLog(IncidentLog(description = description, severity = severity, location = location))
        }
    }

    fun setSttText(text: String) { 
        _sttText.value = text 
        processDynamicCommand(text)
    }

    private fun processDynamicCommand(text: String) {
        val lowerText = text.lowercase()
        when {
            lowerText.contains("audit") || lowerText.contains("check ppe") -> {
                runSafetyAudit()
            }
            lowerText.contains("incident") || lowerText.contains("report") -> {
                // Logic to trigger incident reporting flow if needed
            }
            lowerText.contains("quiz") || lowerText.contains("start quiz") -> {
                // Topic selection logic could be here
            }
        }
    }

    fun setAssistantSpeaking(speaking: Boolean) { _isAssistantSpeaking.value = speaking }
    fun setQuizLanguage(lang: String) { 
        _selectedLanguage.value = lang.ifEmpty { null }
        _currentQuizTopic.value = null
    }
    
    fun startQuiz(topic: String) { 
        if (topic.isEmpty()) {
            _currentQuizTopic.value = null
            return
        }
        val lang = _selectedLanguage.value ?: "English"
        _currentQuizQuestions.value = generateCommonQuiz(topic, lang)
        _currentQuizTopic.value = topic
        _currentQuestionIndex.value = 0
        _quizScore.value = 0
        _quizFeedback.value = null
    }

    fun togglePPE(itemName: String) {
        _ppeChecklist.value = _ppeChecklist.value.map {
            if (it.name == itemName) it.copy(isEquipped = !it.isEquipped) else it
        }
    }

    fun runSafetyAudit() {
        val task = _selectedTask.value ?: return
        val missingItems = _ppeChecklist.value.filter { !it.isEquipped }.map { it.name }
        if (missingItems.isEmpty()) {
            _auditResult.value = "All set! You are protected for ${task.name}."
            _userStats.value = _userStats.value.copy(score = _userStats.value.score + 50)
            return
        }
        _auditResult.value = "Warning: Missing ${missingItems.joinToString(", ")}. High hazard risk detected."
    }

    fun submitQuizAnswer(option: String, correctAnswer: String, explanation: String) {
        if (option == correctAnswer) {
            _quizFeedback.value = "Correct! ✅"
            _quizScore.value += 1
            _userStats.value = _userStats.value.copy(score = _userStats.value.score + 10)
        } else {
            _quizFeedback.value = "Incorrect. ❌ The correct answer was $correctAnswer.\n$explanation"
        }
    }

    fun nextQuestion() { 
        _currentQuestionIndex.value += 1
        _quizFeedback.value = null 
    }

    fun updateProfile(name: String, dept: String, id: String) {
        _userStats.value = _userStats.value.copy(name = name, department = dept, id = id)
        viewModelScope.launch {
            repository.updateUserProfile("local_uid", name, dept, null)
        }
    }
}
