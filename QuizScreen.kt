package com.example.kavach.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kavach.*
import java.util.*

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

@Composable
fun QuizScreen(
    viewModel: SafetyViewModel, 
    tts: TextToSpeech?, 
    isTtsReady: Boolean,
    isListening: Boolean,
    onStartVoice: (String, String) -> Unit,
    onStopVoice: () -> Unit,
    windowWidthSizeClass: WindowWidthSizeClass
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val topic by viewModel.currentQuizTopic.collectAsState()
    val questions by viewModel.currentQuizQuestions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val feedback by viewModel.quizFeedback.collectAsState()
    val score by viewModel.quizScore.collectAsState()
    val allTasks by viewModel.tasks.collectAsState()

    val columns = when (windowWidthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        WindowWidthSizeClass.Expanded -> 3
        else -> 1
    }

    val currentQuestion = if (topic != null && currentIndex < questions.size) questions[currentIndex] else null

    LaunchedEffect(currentQuestion) {
        currentQuestion?.let {
            val textToSpeak = "${it.question}. Options are: ${it.options.joinToString(", ")}"
            onStartVoice(textToSpeak, "QuizID")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selectedLanguage != null) {
                IconButton(onClick = {
                    if (topic != null) {
                        viewModel.startQuiz("") // Goes back to topic selection
                    } else {
                        viewModel.setQuizLanguage("") // Goes back to language selection
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SafetyYellow)
                }
            }
            Text(
                "Safety Quiz",
                color = SafetyYellow,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (selectedLanguage == null) {
            // Language Selection
            Text("Select Your Preferred Language:", color = Color.White, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            val languages = listOf("English", "Hindi", "Kannada", "Telugu", "Tamil")
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(languages) { lang ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setQuizLanguage(lang) },
                        colors = CardDefaults.cardColors(containerColor = SafetyGray)
                    ) {
                        Box(Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(lang, fontWeight = FontWeight.Bold, color = SafetyYellow, fontSize = 18.sp)
                        }
                    }
                }
            }
        } else if (topic == null) {
            // Topic Selection
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Language: $selectedLanguage", color = Color.Gray, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { viewModel.setQuizLanguage("") }) {
                    Text("Change", color = SafetyYellow, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Select a topic to start:", color = Color.White, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            val availableTopics = allTasks.map { it.name }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(availableTopics) { t ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.startQuiz(t) },
                        colors = CardDefaults.cardColors(containerColor = SafetyGray)
                    ) {
                        Box(Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(t, fontWeight = FontWeight.Bold, color = SafetyYellow)
                        }
                    }
                }
            }
        } else {
            if (currentIndex < questions.size) {
                val questionItem = questions[currentIndex]
                
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Topic: $topic", color = SafetyYellow, fontWeight = FontWeight.Bold)
                            Text("Language: $selectedLanguage", color = Color.Gray, fontSize = 12.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        
                        IconButton(onClick = { 
                            val textToSpeak = "${questionItem.question}. Options are: ${questionItem.options.joinToString(", ")}"
                            onStartVoice(textToSpeak, "QuizID")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen", tint = SafetyYellow)
                        }

                        Text("Question ${currentIndex + 1}/${questions.size}", color = Color.Gray)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SafetyGray)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                questionItem.question,
                                fontSize = if (windowWidthSizeClass != WindowWidthSizeClass.Compact) 26.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafetyYellow
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            var selectedOption by remember(currentIndex) { mutableStateOf<String?>(null) }

                            val gridHeight = if (windowWidthSizeClass != WindowWidthSizeClass.Compact) 600.dp else 400.dp
                            val quizColumns = when (windowWidthSizeClass) {
                                WindowWidthSizeClass.Expanded -> 3
                                WindowWidthSizeClass.Medium -> 2
                                else -> 1
                            }
                            
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(quizColumns),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.heightIn(max = gridHeight)
                            ) {
                                items(questionItem.options) { option ->
                                    val isCorrect = option == questionItem.correctAnswer
                                    val isSelected = option == selectedOption

                                    val buttonColor = when {
                                        selectedOption == null -> SafetyBlack
                                        isCorrect -> Color(0xFF1B5E20)
                                        isSelected -> Color(0xFFB71C1C)
                                        else -> SafetyBlack
                                    }

                                    Button(
                                        onClick = {
                                            if (selectedOption == null) {
                                                selectedOption = option
                                                viewModel.submitQuizAnswer(option, questionItem.correctAnswer, questionItem.explanation)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                                    ) {
                                        Text(
                                            option, 
                                            color = if (selectedOption == null) SafetyYellow else Color.White,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            feedback?.let {
                                Spacer(modifier = Modifier.height(16.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (it.contains("Correct")) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                                    )
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(it, color = Color.White)
                                        Spacer(Modifier.height(8.dp))
                                        Button(
                                            onClick = { viewModel.nextQuestion() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Text("NEXT", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SafetyGray)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.EmojiEvents, null, Modifier.size(80.dp), tint = SafetyYellow)
                        Spacer(Modifier.height(16.dp))
                        Text("Quiz Completed!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Your Score: $score / ${questions.size}", fontSize = 18.sp, color = SafetyYellow)
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.startQuiz(topic!!) },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyYellow)
                        ) {
                            Text("RETRY", color = SafetyBlack)
                        }
                        TextButton(onClick = { viewModel.startQuiz("") }) {
                            Text("BACK TO TOPICS", color = SafetyYellow)
                        }
                    }
                }
            }
        }
    }
}
