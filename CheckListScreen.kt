package com.example.kavach.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.kavach.*

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    navController: NavHostController, 
    viewModel: SafetyViewModel, 
    windowWidthSizeClass: WindowWidthSizeClass,
    isListening: Boolean,
    onStartStt: () -> Unit,
    onStopStt: () -> Unit
) {
    val task by viewModel.selectedTask.collectAsState()
    val ppeItems by viewModel.ppeChecklist.collectAsState()
    val auditResult by viewModel.auditResult.collectAsState()
    val isAuditing by viewModel.isAuditing.collectAsState()
    val isSpeaking by viewModel.isAssistantSpeaking.collectAsState()
    
    val isWide = windowWidthSizeClass != WindowWidthSizeClass.Compact

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(task?.name ?: "Checklist") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(end = 8.dp)) {
                        if (isSpeaking) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SafetyYellow, strokeWidth = 2.dp)
                        }
                        IconButton(onClick = { if (isListening) onStopStt() else onStartStt() }, enabled = !isSpeaking) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Voice Assistant",
                                tint = if (isListening) Color.Red else SafetyYellow
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SafetyBlack,
                    titleContentColor = SafetyYellow,
                    navigationIconContentColor = SafetyYellow
                )
            )
        }
    ) { padding ->
        if (isWide) {
            Row(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SafetyAvatar(ppeItems, modifier = Modifier.height(250.dp))
                    Spacer(Modifier.height(16.dp))
                    RiskMeter(task?.riskLevel ?: RiskLevel.LOW) { viewModel.runSafetyAudit() }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Required Gear", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    PPEList(ppeItems, modifier = Modifier.weight(1f)) { viewModel.togglePPE(it) }
                    
                    if (auditResult.isNotEmpty()) {
                        AuditResultCard(auditResult)
                    }

                    AuditButton(isAuditing) { viewModel.runSafetyAudit() }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SafetyAvatar(ppeItems, modifier = Modifier.height(200.dp))
                Spacer(modifier = Modifier.height(16.dp))
                RiskMeter(task?.riskLevel ?: RiskLevel.LOW) { viewModel.runSafetyAudit() }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Required Gear", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                PPEList(ppeItems, modifier = Modifier.heightIn(max = 400.dp)) { viewModel.togglePPE(it) }
                
                if (auditResult.isNotEmpty()) {
                    AuditResultCard(auditResult)
                }

                AuditButton(isAuditing) { viewModel.runSafetyAudit() }
            }
        }
    }
}

@Composable
fun AuditButton(isAuditing: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SafetyYellow),
        enabled = !isAuditing
    ) {
        if (isAuditing) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SafetyBlack)
        } else {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("RUN SAFETY AUDIT", color = SafetyBlack)
        }
    }
}

@Composable
fun PPEList(ppeItems: List<PPEItem>, modifier: Modifier = Modifier, onToggle: (String) -> Unit) {
    // Note: Since we are inside a verticalScroll in portrait mode, we should avoid nested scrolling if possible 
    // or use heightIn. For simplicity we use a simple Column if not large list.
    Column(modifier = modifier) {
        ppeItems.forEach { item ->
            PPEItemRow(item) { onToggle(item.name) }
        }
    }
}

@Composable
fun RiskMeter(level: RiskLevel, onAuditClick: () -> Unit) {
    val riskColor = when (level) {
        RiskLevel.LOW -> RiskLow
        RiskLevel.MEDIUM -> RiskMedium
        RiskLevel.HIGH -> RiskHigh
        RiskLevel.EXTREME -> RiskExtreme
    }
    
    val sweepAngle = when (level) {
        RiskLevel.LOW -> 45f
        RiskLevel.MEDIUM -> 90f
        RiskLevel.HIGH -> 135f
        RiskLevel.EXTREME -> 180f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAuditClick() },
        colors = CardDefaults.cardColors(containerColor = SafetyGray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(60.dp)) {
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.3f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = riskColor,
                        startAngle = 180f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(level.name, color = riskColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("Hazard Level", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Tap for AI Audit", fontSize = 12.sp, color = SafetyYellow)
            }
            
            Icon(Icons.Default.AutoAwesome, null, tint = SafetyYellow)
        }
    }
}

@Composable
fun AuditResultCard(result: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SafetyYellow.copy(alpha = 0.1f)),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            if (result.contains("All set")) {
                Icon(Icons.Default.CheckCircle, null, tint = Color.Green, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = result,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun SafetyAvatar(items: List<PPEItem>, modifier: Modifier = Modifier) {
    val equippedCount = items.count { it.isEquipped }
    val totalCount = items.size
    val progress = if (totalCount > 0) equippedCount.toFloat() / totalCount else 0f

    val hasHelmet = items.any { (it.name.contains("Helmet") || it.name.contains("Hard Hat")) && it.isEquipped }
    val hasGloves = items.any { it.name.contains("Gloves") && it.isEquipped }
    val hasVest = items.any { (it.name.contains("Vest") || it.name.contains("Apron") || it.name.contains("Harness")) && it.isEquipped }
    val hasBoots = items.any { (it.name.contains("Boots") || it.name.contains("Shoes")) && it.isEquipped }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SafetyGray),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = if (hasVest) SafetyYellow else Color.DarkGray,
                modifier = Modifier.size(100.dp)
            )
            
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = if (hasHelmet) SafetyYellow else Color.Transparent,
                modifier = Modifier.size(40.dp).offset(y = (-45).dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Icon(
                    imageVector = Icons.Default.FrontHand,
                    contentDescription = null,
                    tint = if (hasGloves) SafetyYellow else Color.Transparent,
                    modifier = Modifier.size(30.dp).offset(y = 15.dp, x = (-15).dp)
                )
                Icon(
                    imageVector = Icons.Default.FrontHand,
                    contentDescription = null,
                    tint = if (hasGloves) SafetyYellow else Color.Transparent,
                    modifier = Modifier.size(30.dp).offset(y = 15.dp, x = 15.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (hasBoots) SafetyYellow else Color.Transparent,
                modifier = Modifier.size(35.dp).offset(y = 55.dp)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                color = if (progress == 1f) Color(0xFF1B5E20) else SafetyBlack,
                shape = CircleShape,
                border = BorderStroke(1.dp, if (progress == 1f) Color.Green else SafetyYellow)
            ) {
                Text(
                    "${(progress * 100).toInt()}%",
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun PPEItemRow(item: PPEItem, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (item.isEquipped) SafetyYellow.copy(alpha = 0.1f) else Color.Transparent
        ),
        border = BorderStroke(1.dp, if (item.isEquipped) SafetyYellow else Color.Gray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (item.isEquipped) SafetyYellow else SafetyGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon ?: Icons.Default.Build,
                    contentDescription = null,
                    tint = if (item.isEquipped) SafetyBlack else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    color = if (item.isEquipped) Color.White else Color.Gray,
                    fontWeight = if (item.isEquipped) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }

            Checkbox(
                checked = item.isEquipped,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = SafetyYellow)
            )
        }
    }
}
