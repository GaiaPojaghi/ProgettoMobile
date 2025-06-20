package com.example.fitjourney.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fitjourney.data.StudyData
import com.example.fitjourney.ui.viewModel.StudyViewModel

data class ActivityRing(
    val title: String,
    val current: Int,
    val goal: Int,
    val color: Color,
    val icon: ImageVector,
    val unit: String,
    val isExcessive: Boolean = false  // Per gestire pause eccessive
)

data class DailyStats(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

// Dialog per impostare l'obiettivo
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSettingDialog(
    isVisible: Boolean,
    currentGoal: Int,
    goalTitle: String,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    if (isVisible) {
        var goalInput by remember { mutableStateOf(currentGoal.toString()) }
        var isError by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Imposta $goalTitle",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF283593)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = {
                            goalInput = it
                            isError = it.toIntOrNull()?.let { num -> num < 15 || num > 720 } ?: true
                        },
                        label = { Text("Minuti") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = isError,
                        supportingText = {
                            if (isError) {
                                Text("Inserisci un valore tra 15 e 720 minuti")
                            } else {
                                Text("Consigliato: 120-300 minuti (2-5 ore)")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Annulla")
                        }

                        Button(
                            onClick = {
                                goalInput.toIntOrNull()?.let { goal ->
                                    if (goal in 15..720) {
                                        onSave(goal)
                                        onDismiss()
                                    }
                                }
                            },
                            enabled = !isError && goalInput.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283593))
                        ) {
                            Text("Salva", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyHomeScreen(navController: NavController, viewModel: StudyViewModel = viewModel()) {
    val studyData by viewModel.studyData
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadStudyData() // Ricarica i dati aggiornati da Firestore
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    var showGoalDialog by remember { mutableStateOf(false) }

    // Calcolo del tempo totale
    val totalTime = studyData.activeStudyTime + studyData.breakTime

    val rings = listOf(
        // 1. Tempo di Studio - Blu/Verde per focus e calma
        ActivityRing(
            title = "Studio Attivo",
            current = studyData.activeStudyTime,
            goal = studyData.studyGoalMinutes,
            color = Color(0xFF4CAF50), // Verde per calma e concentrazione
            icon = Icons.Default.School,
            unit = "min"
        ),

        // 2. Tempo di Pausa - Giallo/Arancio per energia e rilassamento
        ActivityRing(
            title = "Pause",
            current = studyData.breakTime,
            goal = studyData.breakGoalMinutes,
            color = if (studyData.isBreakExcessive) Color(0xFFFF5722) else Color(0xFFFF9800), // Arancio, rosso se eccessive
            icon = Icons.Default.Coffee,
            unit = "min",
            isExcessive = studyData.isBreakExcessive
        ),

        // 3. Tempo Totale - Viola per visione d'insieme
        ActivityRing(
            title = "Tempo Totale",
            current = totalTime,
            goal = studyData.totalGoalMinutes,
            color = Color(0xFF9C27B0), // Viola per fusione di concentrazione e rilassamento
            icon = Icons.Default.Timer,
            unit = "min"
        )
    )

    val stats = listOf(
        DailyStats("Sessioni", "${studyData.sessionsCompleted}", Icons.Default.CheckCircle, Color(0xFF81C784)),
        DailyStats("Focus", "${if (totalTime > 0) ((studyData.activeStudyTime * 100) / totalTime) else 0}%", Icons.Default.Visibility, Color(0xFF64B5F6)),
        DailyStats("Obiettivo Studio", "${studyData.studyGoalMinutes} min", Icons.Default.Flag, Color(0xFF9575CD))
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("StudyFocus", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF283593))
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bentornato! 🎓",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF283593)
                        )

                        // Messaggio dinamico basato sui dati
                        val motivationMessage = when {
                            studyData.activeStudyTime == 0 -> "Inizia la tua giornata di studio!"
                            studyData.isBreakExcessive -> "Attenzione: troppe pause! Torna a concentrarti 📚"
                            studyData.activeStudyTime >= studyData.studyGoalMinutes -> "Fantastico! Hai raggiunto l'obiettivo! 🎉"
                            else -> "Continua a impegnarti, ogni minuto conta!"
                        }

                        Text(
                            text = motivationMessage,
                            fontSize = 16.sp,
                            color = if (studyData.isBreakExcessive) Color(0xFFE53935) else Color(0xFF607D8B),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            item {
                Text("Il tuo progresso di oggi", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    rings.forEach {
                        ActivityRingCard(it, Modifier.weight(1f).padding(horizontal = 4.dp))
                    }
                }
            }

            item {
                Text("Statistiche Giornaliere", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(stats) { stat -> StatCard(stat = stat) }
                }
            }

            item {
                Button(
                    onClick = {
                        navController.navigate("studySession")
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283593)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("INIZIA SESSIONE DI STUDIO", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Pulsanti per simulare progresso (per testing)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.simulateStudySession() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("+ Studio", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { viewModel.simulateBreak() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Text("+ Pausa", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { showGoalDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                    ) {
                        Text("+ Obiettivo", fontSize = 12.sp, color = Color.White)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Dialog per impostare l'obiettivo
    GoalSettingDialog(
        isVisible = showGoalDialog,
        currentGoal = studyData.studyGoalMinutes,
        goalTitle = "Obiettivo Studio Giornaliero",
        onDismiss = { showGoalDialog = false },
        onSave = { newGoal -> viewModel.updateStudyGoal(newGoal) }
    )
}

@Composable
fun ActivityRingCard(ring: ActivityRing, modifier: Modifier = Modifier) {
    val targetProgress = (ring.current.toFloat() / ring.goal.toFloat()).coerceAtMost(1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1200),
        label = "progress"
    )

    Card(
        modifier = modifier.height(200.dp).padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Titolo dell'anello
            Text(
                text = ring.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C3E50),
                textAlign = TextAlign.Center
            )

            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 8.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = center

                    // Cerchio di sfondo
                    drawCircle(
                        color = ring.color.copy(alpha = 0.2f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Arco di progresso
                    drawArc(
                        color = ring.color,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Effetto "brilluccichìo" se pause eccessive
                    if (ring.isExcessive && ring.title == "Pause") {
                        drawCircle(
                            color = Color.Red.copy(alpha = 0.3f),
                            radius = radius - strokeWidth,
                            center = center
                        )
                    }
                }
                Icon(
                    ring.icon,
                    contentDescription = null,
                    tint = ring.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${ring.current}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                Text(
                    "/ ${ring.goal} ${ring.unit}",
                    fontSize = 10.sp,
                    color = Color(0xFF7F8C8D)
                )

                // Indicatore percentuale
                Text(
                    "${(targetProgress * 100).toInt()}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = ring.color
                )
            }
        }
    }
}

@Composable
fun StatCard(stat: DailyStats) {
    Card(
        modifier = Modifier.width(120.dp).height(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(stat.icon, contentDescription = null, tint = stat.color, modifier = Modifier.size(24.dp))
            Text(stat.value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
            Text(stat.title, fontSize = 12.sp, color = Color(0xFF7F8C8D), textAlign = TextAlign.Center)
        }
    }
}