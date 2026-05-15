package com.example.kavach

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.kavach.ui.screens.*
import java.util.*

/* ---------- THEME COLORS ---------- */
val SafetyYellow = Color(0xFFFFD600)
val SafetyBlack = Color(0xFF1A1A1A)
val SafetyGray = Color(0xFF2D2D2D)
val RiskExtreme = Color(0xFFB71C1C)
val RiskHigh = Color(0xFFE65100)
val RiskMedium = Color(0xFFFBC02D)
val RiskLow = Color(0xFF2E7D32)

@Composable
fun RakshaTheme(content: @Composable () -> Unit) {
    val darkColorScheme = darkColorScheme(
        primary = SafetyYellow,
        onPrimary = SafetyBlack,
        background = SafetyBlack,
        surface = SafetyGray,
        onBackground = Color.White,
        onSurface = Color.White
    )
    MaterialTheme(colorScheme = darkColorScheme, content = content)
}

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private val isTtsReady = mutableStateOf(false)
    private val isSttListening = mutableStateOf(false)
    private var sharedViewModel: SafetyViewModel? = null

    // Permission Handler
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            this.runOnUiThread { initSpeech() }
        } else {
            android.widget.Toast.makeText(this, "Microphone permission is required for voice features", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Request Audio Permissions
        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)

        // 2. Initialize Text-to-Speech (TTS)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady.value = true
                tts?.setLanguage(Locale.US)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {
                        sharedViewModel?.setAssistantSpeaking(true)
                    }
                    override fun onDone(id: String?) {
                        sharedViewModel?.setAssistantSpeaking(false)
                        // If the AI finishes a "Prompt", auto-trigger STT to listen for worker response
                        if (id == "Prompt") {
                            this@MainActivity.runOnUiThread { startStt() }
                        }
                    }
                    override fun onError(id: String?) {
                        sharedViewModel?.setAssistantSpeaking(false)
                    }
                })
            }
        }

        setContent {
            RakshaTheme {
                val vm: SafetyViewModel = viewModel()
                sharedViewModel = vm

                @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
                val windowSizeClass = calculateWindowSizeClass(this)

                RakshaApp(
                    vm = vm,
                    tts = tts,
                    ttsReady = isTtsReady.value,
                    isListening = isSttListening.value,
                    onStartStt = { startStt() },
                    onStopStt = { speechRecognizer?.stopListening() },
                    onStartVoice = { text, id -> startVoice(text, id) },
                    windowWidthSizeClass = windowSizeClass.widthSizeClass
                )
            }
        }
    }

    private fun initSpeech() {
        this.runOnUiThread {
            if (speechRecognizer != null) return@runOnUiThread

            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(p0: Bundle?) {
                            isSttListening.value = true
                            android.util.Log.d("STT", "Ready for speech")
                        }
                        override fun onBeginningOfSpeech() {
                            isSttListening.value = true
                            android.util.Log.d("STT", "Beginning of speech")
                        }
                        override fun onRmsChanged(p0: Float) {}
                        override fun onBufferReceived(p0: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            isSttListening.value = false
                            android.util.Log.d("STT", "End of speech")
                        }
                        override fun onError(p0: Int) {
                            isSttListening.value = false
                            val message = when(p0) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission error"
                                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy - resetting"
                                SpeechRecognizer.ERROR_SERVER -> "Server error"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                                else -> "Error: $p0"
                            }
                            android.util.Log.e("STT", "Error code: $p0 - $message")
                            
                            if (p0 == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                                speechRecognizer?.cancel()
                            }

                            if (p0 != SpeechRecognizer.ERROR_NO_MATCH && p0 != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                                android.widget.Toast.makeText(this@MainActivity, message, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onResults(results: Bundle?) {
                            isSttListening.value = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                sharedViewModel?.setSttText(matches[0])
                                android.util.Log.d("STT", "Final Result: ${matches[0]}")
                            }
                        }
                        override fun onPartialResults(p0: Bundle?) {
                            val matches = p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                sharedViewModel?.setSttText(matches[0])
                            }
                        }
                        override fun onEvent(p0: Int, p1: Bundle?) {}
                    })
                }
            } else {
                android.widget.Toast.makeText(this@MainActivity, "Speech recognition not supported", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startStt() {
        this.runOnUiThread {
            try {
                if (speechRecognizer == null) initSpeech()
                speechRecognizer?.cancel() // Reset any previous session

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "How can I help you?")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                android.util.Log.e("STT", "Listening start error: ${e.message}")
                android.widget.Toast.makeText(this, "Speech recognition failed to start", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startVoice(text: String, id: String) {
        if (isTtsReady.value) {
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, id)
        } else {
            android.util.Log.e("TTS", "TTS not ready yet")
        }
    }

    override fun onDestroy() {
        tts?.shutdown()
        speechRecognizer?.destroy()
        super.onDestroy()
    }
}

@Composable
fun RakshaApp(
    vm: SafetyViewModel,
    tts: TextToSpeech?,
    ttsReady: Boolean,
    isListening: Boolean,
    onStartStt: () -> Unit,
    onStopStt: () -> Unit,
    onStartVoice: (String, String) -> Unit,
    windowWidthSizeClass: WindowWidthSizeClass
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") { OnboardingScreen(navController, vm, windowWidthSizeClass) }
        composable("main") {
            ResponsiveMainContent(
                navController, vm, tts, ttsReady, isListening,
                onStartStt, onStopStt, onStartVoice,
                windowWidthSizeClass
            )
        }
        composable("checklist") { ChecklistScreen(navController, vm, windowWidthSizeClass, isListening, onStartStt, onStopStt) }
    }
}

@Composable
fun ResponsiveMainContent(
    navController: NavHostController,
    vm: SafetyViewModel,
    tts: TextToSpeech?,
    ttsReady: Boolean,
    isListening: Boolean,
    onStartStt: () -> Unit,
    onStopStt: () -> Unit,
    onStartVoice: (String, String) -> Unit,
    windowWidthSizeClass: WindowWidthSizeClass
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val navItems = listOf("Tasks", "Quiz", "Logs", "Profile")
    val navIcons = listOf(Icons.Default.Home, Icons.Default.Quiz, Icons.Default.History, Icons.Default.Person)

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            navItems.forEachIndexed { index, label ->
                item(
                    selected = selectedItem == index,
                    onClick = { selectedItem = index },
                    icon = { Icon(navIcons[index], contentDescription = label) },
                    label = { Text(label) }
                )
            }
        }
    ) {
        Box(Modifier.fillMaxSize()) {
            when (selectedItem) {
                0 -> TaskSelectionScreen(
                    navController, vm, windowWidthSizeClass, isListening,
                    onStartStt, onStopStt
                )
                1 -> QuizScreen(vm, tts, ttsReady, isListening, onStartVoice, { tts?.stop(); vm.setAssistantSpeaking(false) }, windowWidthSizeClass)
                2 -> IncidentLogScreen(vm, isListening, onStartStt, onStopStt, windowWidthSizeClass)
                3 -> ProfileScreen(vm, windowWidthSizeClass)
            }
        }
    }
}
