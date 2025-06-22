package com.example.fitjourney.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.ui.viewModel.StudyViewModel
import com.example.fitjourney.ui.viewModel.WeeklyDataViewModel
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyDataScreen(
    studyViewModel: StudyViewModel,
    weeklyDataViewModel: WeeklyDataViewModel = remember { WeeklyDataViewModel() }
) {
    val weeklyData by weeklyDataViewModel.weeklyData.collectAsState()
    val currentFilter by weeklyDataViewModel.currentFilter.collectAsState()
    val currentPeriod by weeklyDataViewModel.currentPeriod.collectAsState()
    val isLoading by weeklyDataViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        weeklyDataViewModel.loadWeeklyData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header con effetto gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "📊 Studio Analytics",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "I tuoi progressi settimanali",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Badge decorativo
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Caricamento dati...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Filtri periodo con animazione
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically() + fadeIn()
                        ) {
                            PeriodFilterRow(
                                currentPeriod = currentPeriod,
                                onPeriodChange = { weeklyDataViewModel.setPeriod(it) }
                            )
                        }
                    }

                    // Filtri tipo dati
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                animationSpec = tween(durationMillis = 300, delayMillis = 100)
                            ) + fadeIn()
                        ) {
                            DataTypeFilterRow(
                                currentFilter = currentFilter,
                                onFilterChange = { weeklyDataViewModel.setFilter(it) }
                            )
                        }
                    }

                    // Statistiche generali
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                animationSpec = tween(durationMillis = 300, delayMillis = 200)
                            ) + fadeIn()
                        ) {
                            StatisticsOverviewCard(weeklyData = weeklyData)
                        }
                    }

                    // Grafico principale
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                animationSpec = tween(durationMillis = 300, delayMillis = 300)
                            ) + fadeIn()
                        ) {
                            when (currentPeriod) {
                                WeeklyDataViewModel.Period.DAILY -> {
                                    DailyChartCard(
                                        weeklyData = weeklyData,
                                        filter = currentFilter
                                    )
                                }
                                WeeklyDataViewModel.Period.WEEKLY -> {
                                    WeeklyChartCard(
                                        weeklyData = weeklyData,
                                        filter = currentFilter
                                    )
                                }
                                WeeklyDataViewModel.Period.MONTHLY -> {
                                    MonthlyChartCard(
                                        weeklyData = weeklyData,
                                        filter = currentFilter
                                    )
                                }
                            }
                        }
                    }

                    // Dettagli giornalieri
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                animationSpec = tween(durationMillis = 300, delayMillis = 400)
                            ) + fadeIn()
                        ) {
                            DailyDetailsCard(weeklyData = weeklyData)
                        }
                    }

                    // Progressi obiettivi
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                animationSpec = tween(durationMillis = 300, delayMillis = 500)
                            ) + fadeIn()
                        ) {
                            GoalsProgressCard(weeklyData = weeklyData)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodFilterRow(
    currentPeriod: WeeklyDataViewModel.Period,
    onPeriodChange: (WeeklyDataViewModel.Period) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Periodo di Visualizzazione",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(WeeklyDataViewModel.Period.values()) { period ->
                    val isSelected = currentPeriod == period

                    FilterChip(
                        onClick = { onPeriodChange(period) },
                        label = {
                            Text(
                                text = period.displayName,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        selected = isSelected,
                        leadingIcon = {
                            Icon(
                                imageVector = period.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DataTypeFilterRow(
    currentFilter: WeeklyDataViewModel.DataFilter,
    onFilterChange: (WeeklyDataViewModel.DataFilter) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tipo di Dati da Visualizzare",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(WeeklyDataViewModel.DataFilter.values()) { filter ->
                    val isSelected = currentFilter == filter

                    FilterChip(
                        onClick = { onFilterChange(filter) },
                        label = {
                            Text(
                                text = filter.displayName,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        selected = isSelected,
                        leadingIcon = {
                            Icon(
                                imageVector = filter.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.secondary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticsOverviewCard(weeklyData: WeeklyDataViewModel.WeeklyStatistics) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Text(
                        text = "🏆",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Riepilogo Settimanale",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EnhancedStatisticItem(
                        title = "Studio Totale",
                        value = "${weeklyData.totalStudyTime}m",
                        percentage = weeklyData.studyGoalProgress,
                        icon = Icons.Default.School,
                        color = MaterialTheme.colorScheme.primary,
                        emoji = "📚"
                    )

                    EnhancedStatisticItem(
                        title = "Pause Totali",
                        value = "${weeklyData.totalBreakTime}m",
                        percentage = weeklyData.breakGoalProgress,
                        icon = Icons.Default.Coffee,
                        color = MaterialTheme.colorScheme.secondary,
                        emoji = "☕"
                    )

                    EnhancedStatisticItem(
                        title = "Sessioni",
                        value = "${weeklyData.totalSessions}",
                        percentage = null,
                        icon = Icons.Default.PlayArrow,
                        color = MaterialTheme.colorScheme.tertiary,
                        emoji = "🎯"
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedStatisticItem(
    title: String,
    value: String,
    percentage: Float?,
    icon: ImageVector,
    color: Color,
    emoji: String
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )

            percentage?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(it * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (it >= 1f) color else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DailyChartCard(
    weeklyData: WeeklyDataViewModel.WeeklyStatistics,
    filter: WeeklyDataViewModel.DataFilter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Andamento Settimanale - ${filter.displayName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            EnhancedBarChart(
                data = when (filter) {
                    WeeklyDataViewModel.DataFilter.STUDY -> weeklyData.dailyStudyTime
                    WeeklyDataViewModel.DataFilter.BREAK -> weeklyData.dailyBreakTime
                    WeeklyDataViewModel.DataFilter.TOTAL -> weeklyData.dailyTotalTime
                    WeeklyDataViewModel.DataFilter.SESSIONS -> weeklyData.dailySessions.map { it.toFloat() }
                },
                labels = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"),
                color = when (filter) {
                    WeeklyDataViewModel.DataFilter.STUDY -> MaterialTheme.colorScheme.primary
                    WeeklyDataViewModel.DataFilter.BREAK -> MaterialTheme.colorScheme.secondary
                    WeeklyDataViewModel.DataFilter.TOTAL -> MaterialTheme.colorScheme.tertiary
                    WeeklyDataViewModel.DataFilter.SESSIONS -> MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

@Composable
fun EnhancedBarChart(
    data: List<Float>,
    labels: List<String>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOrNull() ?: 1f
    val animatedValues = remember { mutableStateListOf<Float>() }

    LaunchedEffect(data) {
        animatedValues.clear()
        data.forEach { animatedValues.add(0f) }

        data.forEachIndexed { index, targetValue ->
            animate(
                initialValue = 0f,
                targetValue = targetValue,
                animationSpec = tween(
                    durationMillis = 800,
                    delayMillis = index * 100,
                    easing = EaseOutBounce
                )
            ) { value, _ ->
                animatedValues[index] = value
            }
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                animatedValues.forEachIndexed { index, value ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Valore
                        AnimatedVisibility(
                            visible = value > 0,
                            enter = fadeIn() + scaleIn()
                        ) {
                            Text(
                                text = value.toInt().toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        // Barra con gradiente
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(((value / max(maxValue, 1f)) * 150).dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            color,
                                            color.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )

                        // Label
                        Text(
                            text = labels.getOrNull(index) ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyChartCard(
    weeklyData: WeeklyDataViewModel.WeeklyStatistics,
    filter: WeeklyDataViewModel.DataFilter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        ),
                        radius = 300f
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "📊", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confronto Settimanale - ${filter.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Dati delle ultime 4 settimane",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyChartCard(
    weeklyData: WeeklyDataViewModel.WeeklyStatistics,
    filter: WeeklyDataViewModel.DataFilter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        ),
                        radius = 300f
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "📅", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Andamento Mensile - ${filter.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Statistiche mensili",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyDetailsCard(weeklyData: WeeklyDataViewModel.WeeklyStatistics) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(text = "📋", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dettagli Giornalieri",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            val days = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")
            val dayEmojis = listOf("💼", "🔥", "⚡", "🎯", "🚀", "🌟", "😎")

            days.forEachIndexed { index, day ->
                if (index < weeklyData.dailyStudyTime.size) {
                    EnhancedDayDetailRow(
                        day = day,
                        emoji = dayEmojis[index],
                        studyTime = weeklyData.dailyStudyTime[index].toInt(),
                        breakTime = weeklyData.dailyBreakTime[index].toInt(),
                        sessions = weeklyData.dailySessions[index]
                    )

                    if (index < days.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedDayDetailRow(
    day: String,
    emoji: String,
    studyTime: Int,
    breakTime: Int,
    sessions: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = day,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Studio: ${studyTime}m",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Pausa: ${breakTime}m",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "Sessioni: $sessions",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun GoalsProgressCard(weeklyData: WeeklyDataViewModel.WeeklyStatistics) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Obiettivi Settimanali",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            GoalProgressItem("Studio", weeklyData.studyGoalProgress, MaterialTheme.colorScheme.primary)
            GoalProgressItem("Pause", weeklyData.breakGoalProgress, MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun GoalProgressItem(label: String, progress: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
        )
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.align(Alignment.End)
        )
    }
}
