package com.example.kavach.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.kavach.*

@Composable
fun OnboardingScreen(navController: NavHostController, viewModel: SafetyViewModel, windowWidthSizeClass: WindowWidthSizeClass) {
    var step by remember { mutableIntStateOf(0) }
    val pages = listOf(
        "Welcome to Raksha-Kavach" to "Your AI-powered industrial safety auditor.",
        "PPE Verifier" to "Scan and verify your safety gear before starting any hazardous task.",
        "Daily Challenges" to "Complete safety quizzes and earn rewards for staying safe.",
        "Set Up Your Profile" to "Enter your details to get started."
    )

    var name by remember { mutableStateOf("") }
    var workerId by remember { mutableStateOf("") }
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val contentMaxWidth = if (windowWidthSizeClass != WindowWidthSizeClass.Compact) 600.dp else Dp.Unspecified

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SafetyBlack
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .then(if (contentMaxWidth != Dp.Unspecified) Modifier.widthIn(max = contentMaxWidth) else Modifier.fillMaxWidth())
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (step < 3) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = SafetyYellow,
                        modifier = Modifier.size(if (isLandscape) 60.dp else 100.dp)
                    )
                    Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 32.dp))
                    Text(
                        text = pages[step].first,
                        color = SafetyYellow,
                        fontSize = if (isLandscape) 20.sp else 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = pages[step].second,
                        color = Color.LightGray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    Text(
                        text = pages[step].first,
                        color = SafetyYellow,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Worker Name") },
                        modifier = Modifier.fillMaxWidth(if (isLandscape) 0.6f else 1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = SafetyYellow,
                            unfocusedLabelColor = Color.Gray,
                            focusedBorderColor = SafetyYellow,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = workerId,
                        onValueChange = { workerId = it },
                        label = { Text("Worker ID") },
                        modifier = Modifier.fillMaxWidth(if (isLandscape) 0.6f else 1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = SafetyYellow,
                            unfocusedLabelColor = Color.Gray,
                            focusedBorderColor = SafetyYellow,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(if (isLandscape) 24.dp else 48.dp))
                
                Button(
                    onClick = {
                        if (step < 3) {
                            step++
                        } else if (name.isNotBlank() && workerId.isNotBlank()) {
                            viewModel.completeOnboarding(name, workerId)
                            navController.navigate("main") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyYellow),
                    modifier = Modifier.fillMaxWidth(if (isLandscape) 0.4f else 1f),
                    enabled = step < 3 || (name.isNotBlank() && workerId.isNotBlank())
                ) {
                    Text(if (step < 3) "NEXT" else "GET STARTED", color = SafetyBlack)
                }
            }
        }
    }
}
