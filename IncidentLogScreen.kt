package com.example.kavach.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kavach.*
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentLogScreen(
    viewModel: SafetyViewModel, 
    isListening: Boolean,
    onStartStt: () -> Unit,
    onStopStt: () -> Unit,
    windowWidthSizeClass: WindowWidthSizeClass
) {
    var severity by remember { mutableStateOf("Low") }
    var location by remember { mutableStateOf("") }
    val incidents by viewModel.incidents.collectAsState()
    val description by viewModel.sttText.collectAsState()
    val isSpeaking by viewModel.isAssistantSpeaking.collectAsState()
    
    val isWide = windowWidthSizeClass != WindowWidthSizeClass.Compact

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val currentDateTime = remember { mutableStateOf("${dateFormat.format(Date())} ${timeFormat.format(Date())}") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Safety Incidents", color = SafetyYellow, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Current Time: ${currentDateTime.value}", color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))
        
        if (isWide) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    IncidentForm(
                        description, { viewModel.setSttText(it) }, 
                        severity, { severity = it },
                        location, { location = it },
                        onSTT = { if (isListening) onStopStt() else onStartStt() },
                        isListening = isListening,
                        isSpeaking = isSpeaking
                    ) {
                        viewModel.saveIncident(
                            description, severity, location, 
                            dateFormat.format(Date()), timeFormat.format(Date())
                        )
                        viewModel.setSttText("")
                        location = ""
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    IncidentHistoryList(incidents)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    IncidentForm(
                        description, { viewModel.setSttText(it) }, 
                        severity, { severity = it },
                        location, { location = it },
                        onSTT = { if (isListening) onStopStt() else onStartStt() },
                        isListening = isListening,
                        isSpeaking = isSpeaking
                    ) {
                        viewModel.saveIncident(
                            description, severity, location, 
                            dateFormat.format(Date()), timeFormat.format(Date())
                        )
                        viewModel.setSttText("")
                        location = ""
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("Incident History", color = SafetyYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                }
                
                items(incidents) { incident ->
                    IncidentCard(incident)
                    Spacer(Modifier.height(8.dp))
                }
                
                if (incidents.isEmpty()) {
                    item {
                        Text("No incidents logged yet.", color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentForm(
    description: String, 
    onDescriptionChange: (String) -> Unit, 
    severity: String, 
    onSeverityChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    onSTT: () -> Unit,
    isListening: Boolean = false,
    isSpeaking: Boolean = false,
    onLog: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = SafetyGray)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Report Near-Miss / Incident", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Event, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date()), color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(Modifier.height(12.dp))
            
            OutlinedTextField(
                value = location,
                onValueChange = onLocationChange,
                label = { Text("Location (e.g. Zone B, Shop Floor)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SafetyYellow,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafetyYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = SafetyYellow,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(Modifier.width(8.dp))
                
                Box(contentAlignment = Alignment.Center) {
                    if (isSpeaking) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SafetyYellow, strokeWidth = 2.dp)
                    }
                    IconButton(onClick = onSTT, enabled = !isSpeaking) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic, 
                            contentDescription = "Voice Assistant", 
                            tint = if (isListening) Color.Red else SafetyYellow
                        )
                    }
                }
            }
            
            if (isListening) {
                Text("Assistant is listening...", color = Color.Red, fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp))
            } else if (isSpeaking) {
                Text("Assistant is speaking...", color = SafetyYellow, fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp))
            }

            Spacer(Modifier.height(16.dp))
            Text("Severity", color = Color.White)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Low", "Medium", "High").forEach { level ->
                    FilterChip(
                        selected = severity == level,
                        onClick = { onSeverityChange(level) },
                        label = { Text(level) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SafetyYellow,
                            selectedLabelColor = SafetyBlack
                        )
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onLog,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SafetyYellow)
            ) {
                Text("LOG INCIDENT", color = SafetyBlack)
            }
        }
    }
}

@Composable
fun IncidentHistoryList(incidents: List<Incident>) {
    Column {
        Text("Incident History", color = SafetyYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(incidents) { incident ->
                IncidentCard(incident)
            }
            if (incidents.isEmpty()) {
                item {
                    Text("No incidents logged yet.", color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
        }
    }
}

@Composable
fun IncidentCard(incident: Incident) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SafetyGray.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val color = when(incident.severity) {
                    "High" -> RiskHigh
                    "Medium" -> RiskMedium
                    else -> RiskLow
                }
                Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(8.dp))
                Text(incident.severity, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Surface(
                    color = Color.Gray.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "${incident.date} at ${incident.time}", 
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.LightGray, 
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Location: ${incident.location}", color = SafetyYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(incident.description, color = Color.White, fontSize = 14.sp)
        }
    }
}
