package com.example.kavach.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kavach.*

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

@Composable
fun ProfileScreen(viewModel: SafetyViewModel, windowWidthSizeClass: WindowWidthSizeClass) {
    val stats by viewModel.userStats.collectAsState()
    val isWide = windowWidthSizeClass != WindowWidthSizeClass.Compact
    
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(stats.name) }
    var editDept by remember { mutableStateOf(stats.department) }
    var editId by remember { mutableStateOf(stats.id) }

    Box(modifier = Modifier.fillMaxSize().background(SafetyBlack)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("User Profile", color = SafetyYellow, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { 
                    if (isEditing) {
                        viewModel.updateProfile(editName, editDept, editId)
                    } else {
                        editName = stats.name
                        editDept = stats.department
                        editId = stats.id
                    }
                    isEditing = !isEditing 
                }) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = SafetyYellow
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))

            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    ProfileHeader(stats, isEditing, editName, editDept, editId, onNameChange = { editName = it }, onDeptChange = { editDept = it }, onIdChange = { editId = it }, Modifier.weight(1f))
                    ProfileStatsGrid(stats, Modifier.weight(1f))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileHeader(stats, isEditing, editName, editDept, editId, onNameChange = { editName = it }, onDeptChange = { editDept = it }, onIdChange = { editId = it })
                    Spacer(Modifier.height(32.dp))
                    ProfileStatsGrid(stats)
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    stats: UserStats, 
    isEditing: Boolean, 
    editName: String, 
    editDept: String,
    editId: String,
    onNameChange: (String) -> Unit,
    onDeptChange: (String) -> Unit,
    onIdChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.AccountCircle, null, Modifier.size(120.dp), tint = SafetyYellow)
        Spacer(Modifier.height(16.dp))
        if (isEditing) {
            OutlinedTextField(
                value = editName,
                onValueChange = onNameChange,
                label = { Text("Name") },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = editId,
                onValueChange = onIdChange,
                label = { Text("Worker ID") },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = editDept,
                onValueChange = onDeptChange,
                label = { Text("Department") },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
        } else {
            Text(stats.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("ID: ${stats.id}", fontSize = 14.sp, color = SafetyYellow)
            Text(stats.department, fontSize = 16.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Surface(
                color = SafetyYellow,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    stats.rank, 
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = SafetyBlack, 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProfileStatsGrid(stats: UserStats, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SafetyGray)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(24.dp),
                Arrangement.SpaceEvenly
            ) {
                StatItem("Score", stats.score.toString())
                VerticalDivider(color = Color.Gray, modifier = Modifier.height(40.dp))
                StatItem("Safe Streak", "${stats.safeStreak} Days")
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Achievements", color = SafetyYellow, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BadgeIcon(Icons.Default.VerifiedUser, "PPE Pro")
            BadgeIcon(Icons.Default.WorkspacePremium, "Fast Learner")
            BadgeIcon(Icons.Default.EmojiEvents, "Top Audited")
        }
    }
}

@Composable
fun BadgeIcon(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(SafetyYellow),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = SafetyBlack)
        }
        Text(label, fontSize = 10.sp, color = Color.LightGray)
    }
}

@Composable
fun VerticalDivider(color: Color, modifier: Modifier) {
    Box(modifier.width(1.dp).background(color))
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 14.sp, color = Color.Gray)
    }
}
