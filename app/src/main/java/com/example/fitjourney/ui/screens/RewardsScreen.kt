package com.example.fitjourney.ui.screens

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitjourney.ui.viewModel.StudyViewModel

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val requirement: Int,
    val color: Color,
    val category: AchievementCategory
)

enum class AchievementCategory {
    STUDY_TIME, SESSIONS, CONSISTENCY, SPECIAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(viewModel: StudyViewModel = viewModel()) {
    val studyData by viewModel.studyData

    // Ricarica i dati ogni volta che la pagina viene aperta
    LaunchedEffect(Unit) {
        viewModel.loadStudyData()
    }

    val achievements = remember {
        listOf(
            // Studio - Tempo
            Achievement("first_study", "Prima Sessione", "Completa la tua prima sessione di studio", Icons.Default.Star, 1, Color(0xFFFFD700), AchievementCategory.STUDY_TIME),
            Achievement("study_30min", "Studiatore Principiante", "Studia per 30 minuti in totale", Icons.Default.MenuBook, 30, Color(0xFF4CAF50), AchievementCategory.STUDY_TIME),
            Achievement("study_2h", "Studente Dedicato", "Studia per 2 ore in totale", Icons.Default.School, 120, Color(0xFF2196F3), AchievementCategory.STUDY_TIME),
            Achievement("study_5h", "Maratoneta dello Studio", "Studia per 5 ore in totale", Icons.Default.EmojiEvents, 300, Color(0xFF9C27B0), AchievementCategory.STUDY_TIME),
            Achievement("study_10h", "Maestro della Concentrazione", "Studia per 10 ore in totale", Icons.Default.Psychology, 600, Color(0xFFFF5722), AchievementCategory.STUDY_TIME),

            // Sessioni
            Achievement("sessions_5", "Costanza", "Completa 5 sessioni di studio", Icons.Default.CheckCircle, 5, Color(0xFF4CAF50), AchievementCategory.SESSIONS),
            Achievement("sessions_10", "Determinazione", "Completa 10 sessioni di studio", Icons.Default.Timeline, 10, Color(0xFF2196F3), AchievementCategory.SESSIONS),
            Achievement("sessions_25", "Perseveranza", "Completa 25 sessioni di studio", Icons.Default.TrendingUp, 25, Color(0xFF9C27B0), AchievementCategory.SESSIONS),
            Achievement("sessions_50", "Campione di Disciplina", "Completa 50 sessioni di studio", Icons.Default.EmojiEvents, 50, Color(0xFFFF9800), AchievementCategory.SESSIONS),

            // Speciali
            Achievement("balanced_study", "Equilibrio Perfetto", "Studia per almeno 2 ore con pause equilibrate", Icons.Default.Balance, 1, Color(0xFFE91E63), AchievementCategory.SPECIAL),
            Achievement("focus_master", "Maestro del Focus", "Completa una sessione da 25 minuti senza pause", Icons.Default.Visibility, 1, Color(0xFF3F51B5), AchievementCategory.SPECIAL)
        )
    }

    fun isAchievementUnlocked(achievement: Achievement): Boolean {
        return when (achievement.category) {
            AchievementCategory.STUDY_TIME -> studyData.activeStudyTime >= achievement.requirement
            AchievementCategory.SESSIONS -> studyData.sessionsCompleted >= achievement.requirement
            AchievementCategory.SPECIAL -> {
                when (achievement.id) {
                    "balanced_study" -> studyData.activeStudyTime >= 120 && studyData.breakTime >= 30
                    "focus_master" -> studyData.sessionsCompleted >= 1 // Semplificato per demo
                    else -> false
                }
            }
            else -> false
        }
    }

    fun getProgress(achievement: Achievement): Float {
        val current = when (achievement.category) {
            AchievementCategory.STUDY_TIME -> studyData.activeStudyTime
            AchievementCategory.SESSIONS -> studyData.sessionsCompleted
            AchievementCategory.SPECIAL -> if (isAchievementUnlocked(achievement)) achievement.requirement else 0
            else -> 0
        }
        return (current.toFloat() / achievement.requirement).coerceAtMost(1f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🏆  Medaglie e Obiettivi",
                        fontSize = 22.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con statistiche
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "I tuoi Progressi",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${studyData.activeStudyTime}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("Minuti studiati", fontSize = 12.sp)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${studyData.sessionsCompleted}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("Sessioni", fontSize = 12.sp)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val unlockedCount = achievements.count { isAchievementUnlocked(it) }
                                Text(
                                    "$unlockedCount/${achievements.size}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("Medaglie", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Sezioni per categoria
            val groupedAchievements = achievements.groupBy { it.category }

            items(groupedAchievements.keys.toList()) { category ->
                val categoryAchievements = groupedAchievements[category] ?: emptyList()
                val categoryTitle = when (category) {
                    AchievementCategory.STUDY_TIME -> "⏱️ Tempo di Studio"
                    AchievementCategory.SESSIONS -> "📚 Sessioni Completate"
                    AchievementCategory.CONSISTENCY -> "🔥 Costanza"
                    AchievementCategory.SPECIAL -> "✨ Obiettivi Speciali"
                }

                Column {
                    Text(
                        categoryTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )

                    categoryAchievements.forEach { achievement ->
                        AchievementCard(
                            achievement = achievement,
                            isUnlocked = isAchievementUnlocked(achievement),
                            progress = getProgress(achievement)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementCard(
    achievement: Achievement,
    isUnlocked: Boolean,
    progress: Float
) {
    val alpha = if (isUnlocked) 1f else 0.4f
    val cardColor = if (isUnlocked) achievement.color.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)
    val iconColor = if (isUnlocked) achievement.color else Color.Gray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona della medaglia
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = achievement.icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Informazioni dell'achievement
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                )

                Text(
                    text = achievement.description,
                    fontSize = 14.sp,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Barra di progresso se non sbloccato
                if (!isUnlocked && progress > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = achievement.color,
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )

                    val currentValue = when (achievement.category) {
                        AchievementCategory.STUDY_TIME, AchievementCategory.SESSIONS ->
                            (progress * achievement.requirement).toInt()
                        else -> if (progress >= 1f) achievement.requirement else 0
                    }

                    Text(
                        text = "$currentValue / ${achievement.requirement}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Badge di completamento
            if (isUnlocked) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Sbloccato",
                    tint = achievement.color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}