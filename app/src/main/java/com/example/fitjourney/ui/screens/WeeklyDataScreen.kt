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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fitjourney.ui.viewModel.StudyViewModel
import com.example.fitjourney.ui.viewModel.WeeklyDataViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

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
            modifier = Modifier.fillMaxSize()
        ) {
            // Header con sfondo blu e senza padding esterno
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF283593))
                    .padding(bottom = 24.dp, top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (currentPeriod) {
                                WeeklyDataViewModel.Period.DAILY -> "Statistiche di Studio"
                                WeeklyDataViewModel.Period.WEEKLY -> "Tendenze Settimanali"
                                WeeklyDataViewModel.Period.MONTHLY -> "Panoramica Mensile"
                            },
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = when (currentPeriod) {
                        WeeklyDataViewModel.Period.DAILY -> "I tuoi progressi settimanali"
                        WeeklyDataViewModel.Period.WEEKLY -> "Ultime 4 settimane"
                        WeeklyDataViewModel.Period.MONTHLY -> "Ultimi 6 mesi"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

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

                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically(
                                    animationSpec = tween(durationMillis = 300, delayMillis = 200)
                                ) + fadeIn()
                            ) {
                                StatisticsOverviewCard(
                                    weeklyData = weeklyData,
                                    period = currentPeriod
                                )
                            }
                        }

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

                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically(
                                    animationSpec = tween(durationMillis = 300, delayMillis = 400)
                                ) + fadeIn()
                            ) {
                                DetailedStatsCard(
                                    weeklyData = weeklyData,
                                    filter = currentFilter,
                                    period = currentPeriod
                                )
                            }
                        }

                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically(
                                    animationSpec = tween(durationMillis = 300, delayMillis = 500)
                                ) + fadeIn()
                            ) {
                                InsightsCard(
                                    weeklyData = weeklyData,
                                    period = currentPeriod
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedTrendBadge(
    trendPercentage: Float,
    period: WeeklyDataViewModel.Period
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = trendPercentage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val isPositive = animatedPercentage >= 0
    val color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
    val icon = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown

    Card(
        modifier = Modifier
            .shadow(8.dp, CircleShape)
            .clip(CircleShape),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${if (isPositive) "+" else ""}${animatedPercentage.toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PeriodFilterRow(
    currentPeriod: WeeklyDataViewModel.Period,
    onPeriodChange: (WeeklyDataViewModel.Period) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(WeeklyDataViewModel.Period.values()) { period ->
            FilterChip(
                onClick = { onPeriodChange(period) },
                label = {
                    Text(
                        text = when (period) {
                            WeeklyDataViewModel.Period.DAILY -> "Giornaliero"
                            WeeklyDataViewModel.Period.WEEKLY -> "Settimanale"
                            WeeklyDataViewModel.Period.MONTHLY -> "Mensile"
                        }
                    )
                },
                selected = currentPeriod == period,
                leadingIcon = {
                    Icon(
                        imageVector = when (period) {
                            WeeklyDataViewModel.Period.DAILY -> Icons.Default.Today
                            WeeklyDataViewModel.Period.WEEKLY -> Icons.Default.DateRange
                            WeeklyDataViewModel.Period.MONTHLY -> Icons.Default.CalendarMonth
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun DataTypeFilterRow(
    currentFilter: WeeklyDataViewModel.DataFilter,
    onFilterChange: (WeeklyDataViewModel.DataFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(WeeklyDataViewModel.DataFilter.values()) { filter ->
            FilterChip(
                onClick = { onFilterChange(filter) },
                label = {
                    Text(text = filter.displayName)
                },
                selected = currentFilter == filter,
                leadingIcon = {
                    Icon(
                        imageVector = filter.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun StatisticsOverviewCard(
    weeklyData: WeeklyDataViewModel.WeeklyStatistics,
    period: WeeklyDataViewModel.Period
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📈 Panoramica Generale",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    title = "Ore Totali",
                    value = "${weeklyData.totalStudyTime}h",
                    icon = Icons.Default.Schedule,
                    color = Color(0xFF2196F3)
                )

                StatisticItem(
                    title = "Sessioni",
                    value = "${weeklyData.totalSessions}",
                    icon = Icons.Default.PlayArrow,
                    color = Color(0xFF4CAF50)
                )

                StatisticItem(
                    title = "Media/Giorno",
                    value = "${weeklyData.averageStudyPerDay}h",
                    icon = Icons.Default.BarChart,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
private fun StatisticItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DailyChartCard(
    weeklyData: WeeklyDataViewModel.WeeklyStatistics,
    filter: WeeklyDataViewModel.DataFilter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📊 Andamento Settimanale",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Aumentato da 200dp a 300dp
            ) {
                if (weeklyData.isEmpty) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nessun dato disponibile per questo periodo",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    DailyBarChart(
                        weeklyData = weeklyData,
                        filter = filter,
                        modifier = Modifier.fillMaxSize() // Aggiungi fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyChartCard(
    weeklyData: WeeklyDataViewModel.WeeklyStatistics,
    filter: WeeklyDataViewModel.DataFilter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📈 Tendenze Settimanali",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Aumentato da 200dp a 300dp
            ) {
                if (weeklyData.isEmpty) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nessun dato disponibile per questo periodo",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    WeeklyLineChart(
                        weeklyData = weeklyData,
                        filter = filter,
                        modifier = Modifier.fillMaxSize() // Aggiungi fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyChartCard(
    weeklyData: WeeklyDataViewModel.WeeklyStatistics,
    filter: WeeklyDataViewModel.DataFilter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📅 Panoramica Mensile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Aumentato da 200dp a 300dp
            ) {
                if (weeklyData.isEmpty) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nessun dato disponibile per questo periodo",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    MonthlyBarChart(
                        weeklyData = weeklyData,
                        filter = filter,
                        modifier = Modifier.fillMaxSize() // Aggiungi fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun DailyBarChart(
    weeklyData: WeeklyDataViewModel.WeeklyStatistics,
    filter: WeeklyDataViewModel.DataFilter,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            BarChart(context).apply {
                setupBarChartStyle()
            }
        },
        update = { chart ->
            try {
                // Verifica che weeklyData non sia null e abbia dati
                if (weeklyData.isEmpty) {
                    chart.clear()
                    chart.invalidate()
                    return@AndroidView
                }

                val entries = when (filter) {
                    WeeklyDataViewModel.DataFilter.STUDY -> {
                        if (weeklyData.dailyStudyTime.isEmpty()) {
                            // Dati di esempio se vuoti
                            listOf(1.5f, 2.0f, 1.8f, 2.5f, 3.0f, 1.0f, 0.5f)
                        } else {
                            weeklyData.dailyStudyTime
                        }.mapIndexed { index, value ->
                            BarEntry(index.toFloat(), maxOf(0f, value))
                        }
                    }
                    WeeklyDataViewModel.DataFilter.BREAK -> {
                        if (weeklyData.dailyBreakTime.isEmpty()) {
                            listOf(0.5f, 0.8f, 0.6f, 0.7f, 0.9f, 0.3f, 0.2f)
                        } else {
                            weeklyData.dailyBreakTime
                        }.mapIndexed { index, value ->
                            BarEntry(index.toFloat(), maxOf(0f, value))
                        }
                    }
                    WeeklyDataViewModel.DataFilter.TOTAL -> {
                        if (weeklyData.dailyTotalTime.isEmpty()) {
                            listOf(2.0f, 2.8f, 2.4f, 3.2f, 3.9f, 1.3f, 0.7f)
                        } else {
                            weeklyData.dailyTotalTime
                        }.mapIndexed { index, value ->
                            BarEntry(index.toFloat(), maxOf(0f, value))
                        }
                    }
                    WeeklyDataViewModel.DataFilter.SESSIONS -> {
                        if (weeklyData.dailySessions.isEmpty()) {
                            listOf(2, 3, 2, 4, 5, 1, 1)
                        } else {
                            weeklyData.dailySessions
                        }.mapIndexed { index, value ->
                            BarEntry(index.toFloat(), maxOf(0f, value.toFloat()))
                        }
                    }
                }

                if (entries.isNotEmpty() && entries.any { it.y > 0 }) {
                    val dataSet = BarDataSet(entries, filter.displayName).apply {
                        color = when (filter) {
                            WeeklyDataViewModel.DataFilter.STUDY -> android.graphics.Color.parseColor("#4CAF50")
                            WeeklyDataViewModel.DataFilter.BREAK -> android.graphics.Color.parseColor("#2196F3")
                            WeeklyDataViewModel.DataFilter.TOTAL -> android.graphics.Color.parseColor("#FF9800")
                            WeeklyDataViewModel.DataFilter.SESSIONS -> android.graphics.Color.parseColor("#9C27B0")
                        }
                        valueTextColor = android.graphics.Color.BLACK
                        valueTextSize = 10f
                        setDrawValues(true)
                    }

                    val barData = BarData(dataSet)
                    barData.barWidth = 0.8f

                    chart.data = barData
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                        arrayOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")
                    )

                    // Forza refresh del grafico
                    chart.notifyDataSetChanged()
                    chart.invalidate()

                    // Anima il grafico
                    chart.animateY(800)
                } else {
                    // Crea dati vuoti ma visibili
                    val emptyEntries = (0..6).map { BarEntry(it.toFloat(), 0.1f) }
                    val dataSet = BarDataSet(emptyEntries, "Nessun dato").apply {
                        color = android.graphics.Color.LTGRAY
                        setDrawValues(false)
                    }

                    chart.data = BarData(dataSet)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                        arrayOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")
                    )
                    chart.invalidate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // In caso di errore, mostra grafico con dati di esempio
                val fallbackEntries = (0..6).map { BarEntry(it.toFloat(), 1.0f) }
                val dataSet = BarDataSet(fallbackEntries, "Dati di esempio").apply {
                    color = android.graphics.Color.GRAY
                    setDrawValues(false)
                }
                chart.data = BarData(dataSet)
                chart.invalidate()
            }
        }
    )
}

@Composable
fun WeeklyLineChart(
    weeklyData: WeeklyDataViewModel.WeeklyStatistics,
    filter: WeeklyDataViewModel.DataFilter,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                setupLineChartStyle()
            }
        },
        update = { chart ->
            try {
                if (weeklyData.isEmpty) {
                    chart.clear()
                    chart.invalidate()
                    return@AndroidView
                }

                val entries = when (filter) {
                    WeeklyDataViewModel.DataFilter.STUDY -> {
                        if (weeklyData.weeklyStudyTime.isEmpty()) {
                            listOf(8.0f, 12.5f, 15.2f, 18.0f)
                        } else {
                            weeklyData.weeklyStudyTime
                        }.mapIndexed { index, value ->
                            Entry(index.toFloat(), maxOf(0f, value))
                        }
                    }
                    WeeklyDataViewModel.DataFilter.BREAK -> {
                        if (weeklyData.weeklyBreakTime.isEmpty()) {
                            listOf(2.0f, 3.5f, 4.2f, 4.8f)
                        } else {
                            weeklyData.weeklyBreakTime
                        }.mapIndexed { index, value ->
                            Entry(index.toFloat(), maxOf(0f, value))
                        }
                    }
                    WeeklyDataViewModel.DataFilter.TOTAL -> {
                        if (weeklyData.weeklyTotalTime.isEmpty()) {
                            listOf(10.0f, 16.0f, 19.4f, 22.8f)
                        } else {
                            weeklyData.weeklyTotalTime
                        }.mapIndexed { index, value ->
                            Entry(index.toFloat(), maxOf(0f, value))
                        }
                    }
                    WeeklyDataViewModel.DataFilter.SESSIONS -> {
                        if (weeklyData.weeklySessions.isEmpty()) {
                            listOf(12, 18, 22, 25)
                        } else {
                            weeklyData.weeklySessions
                        }.mapIndexed { index, value ->
                            Entry(index.toFloat(), maxOf(0f, value.toFloat()))
                        }
                    }
                }

                if (entries.isNotEmpty()) {
                    val dataSet = LineDataSet(entries, filter.displayName).apply {
                        color = when (filter) {
                            WeeklyDataViewModel.DataFilter.STUDY -> android.graphics.Color.parseColor("#4CAF50")
                            WeeklyDataViewModel.DataFilter.BREAK -> android.graphics.Color.parseColor("#2196F3")
                            WeeklyDataViewModel.DataFilter.TOTAL -> android.graphics.Color.parseColor("#FF9800")
                            WeeklyDataViewModel.DataFilter.SESSIONS -> android.graphics.Color.parseColor("#9C27B0")
                        }
                        lineWidth = 3f
                        circleRadius = 5f
                        setCircleColor(color)
                        circleHoleRadius = 2f
                        valueTextColor = android.graphics.Color.BLACK
                        valueTextSize = 10f
                        setDrawValues(true)
                        setDrawFilled(false)
                        setDrawCircles(true)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                    }

                    chart.data = LineData(dataSet)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                        arrayOf("Sett 4", "Sett 3", "Sett 2", "Sett 1")
                    )

                    chart.notifyDataSetChanged()
                    chart.invalidate()

                    if (entries.any { it.y > 0 }) {
                        chart.animateY(800)
                    }
                } else {
                    chart.clear()
                    chart.invalidate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                chart.clear()
                chart.invalidate()
            }
        }
    )
}

@Composable
fun MonthlyBarChart(
    weeklyData: WeeklyDataViewModel.WeeklyStatistics,
    filter: WeeklyDataViewModel.DataFilter,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            BarChart(context).apply {
                setupBarChartStyle()
            }
        },
        update = { chart ->
            try {
                if (weeklyData.isEmpty) {
                    chart.clear()
                    chart.invalidate()
                    return@AndroidView
                }

                val entries = when (filter) {
                    WeeklyDataViewModel.DataFilter.STUDY -> {
                        if (weeklyData.monthlyStudyTime.isEmpty()) {
                            listOf(45.0f, 52.0f, 48.0f, 65.0f, 72.0f, 78.0f)
                        } else {
                            weeklyData.monthlyStudyTime
                        }.mapIndexed { index, value ->
                            BarEntry(index.toFloat(), maxOf(0f, value))
                        }
                    }
                    WeeklyDataViewModel.DataFilter.BREAK -> {
                        if (weeklyData.monthlyBreakTime.isEmpty()) {
                            listOf(12.0f, 15.0f, 14.0f, 18.0f, 20.0f, 22.0f)
                        } else {
                            weeklyData.monthlyBreakTime
                        }.mapIndexed { index, value ->
                            BarEntry(index.toFloat(), maxOf(0f, value))
                        }
                    }
                    WeeklyDataViewModel.DataFilter.TOTAL -> {
                        if (weeklyData.monthlyTotalTime.isEmpty()) {
                            listOf(57.0f, 67.0f, 62.0f, 83.0f, 92.0f, 100.0f)
                        } else {
                            weeklyData.monthlyTotalTime
                        }.mapIndexed { index, value ->
                            BarEntry(index.toFloat(), maxOf(0f, value))
                        }
                    }
                    WeeklyDataViewModel.DataFilter.SESSIONS -> {
                        if (weeklyData.monthlySessions.isEmpty()) {
                            listOf(85, 95, 88, 110, 125, 135)
                        } else {
                            weeklyData.monthlySessions
                        }.mapIndexed { index, value ->
                            BarEntry(index.toFloat(), maxOf(0f, value.toFloat()))
                        }
                    }
                }

                if (entries.isNotEmpty()) {
                    val dataSet = BarDataSet(entries, filter.displayName).apply {
                        color = when (filter) {
                            WeeklyDataViewModel.DataFilter.STUDY -> android.graphics.Color.parseColor("#4CAF50")
                            WeeklyDataViewModel.DataFilter.BREAK -> android.graphics.Color.parseColor("#2196F3")
                            WeeklyDataViewModel.DataFilter.TOTAL -> android.graphics.Color.parseColor("#FF9800")
                            WeeklyDataViewModel.DataFilter.SESSIONS -> android.graphics.Color.parseColor("#9C27B0")
                        }
                        valueTextColor = android.graphics.Color.BLACK
                        valueTextSize = 10f
                        setDrawValues(true)
                    }

                    val barData = BarData(dataSet)
                    barData.barWidth = 0.8f

                    chart.data = barData
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                        arrayOf("M6", "M5", "M4", "M3", "M2", "M1")
                    )

                    chart.notifyDataSetChanged()
                    chart.invalidate()

                    if (entries.any { it.y > 0 }) {
                        chart.animateY(800)
                    }
                } else {
                    chart.clear()
                    chart.invalidate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                chart.clear()
                chart.invalidate()
            }
        }
    )
}

private fun BarChart.setupBarChartStyle() {
    // Disabilita descrizione
    description.isEnabled = false

    // Configurazione generale
    setDrawGridBackground(false)
    setDrawBarShadow(false)
    setDrawValueAboveBar(true)
    setMaxVisibleValueCount(60)
    setPinchZoom(false)
    setDrawGridBackground(false)
    setTouchEnabled(true)

    // Configurazione asse X
    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        setDrawGridLines(false)
        setDrawAxisLine(true)
        granularity = 1f
        isGranularityEnabled = true
        labelCount = 7
        textColor = android.graphics.Color.BLACK
        textSize = 14f // Aumentato da 12f a 14f
        setLabelCount(7, false)
        yOffset = 10f // Aggiungi spazio per le etichette
    }

    // Configurazione asse Y sinistro
    axisLeft.apply {
        setDrawGridLines(true)
        setDrawAxisLine(true)
        axisMinimum = 0f
        textColor = android.graphics.Color.BLACK
        textSize = 14f // Aumentato da 12f a 14f
        gridColor = android.graphics.Color.LTGRAY
        gridLineWidth = 0.5f
        setStartAtZero(true)
        spaceTop = 10f // Aggiungi spazio sopra
    }

    // Disabilita asse Y destro
    axisRight.isEnabled = false

    // Configurazione legenda
    legend.isEnabled = false

    // Configurazione touch
    setDragEnabled(true)
    setScaleEnabled(false)
    isDoubleTapToZoomEnabled = false

    // Margini e viewport - Aumentati per più spazio
    setExtraOffsets(15f, 20f, 15f, 20f)
    setFitBars(true)
}

private fun LineChart.setupLineChartStyle() {
    // Disabilita descrizione
    description.isEnabled = false

    // Configurazione generale
    setDrawGridBackground(false)
    setMaxVisibleValueCount(60)
    setTouchEnabled(true)
    setPinchZoom(true)
    setDrawGridBackground(false)

    // Configurazione asse X
    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        setDrawGridLines(false)
        setDrawAxisLine(true)
        granularity = 1f
        isGranularityEnabled = true
        labelCount = 4
        textColor = android.graphics.Color.BLACK
        textSize = 14f // Aumentato da 12f a 14f
        setLabelCount(4, false)
        yOffset = 10f // Aggiungi spazio per le etichette
    }

    // Configurazione asse Y sinistro
    axisLeft.apply {
        setDrawGridLines(true)
        setDrawAxisLine(true)
        axisMinimum = 0f
        textColor = android.graphics.Color.BLACK
        textSize = 14f // Aumentato da 12f a 14f
        gridColor = android.graphics.Color.LTGRAY
        gridLineWidth = 0.5f
        setStartAtZero(true)
        spaceTop = 10f // Aggiungi spazio sopra
    }

    // Disabilita asse Y destro
    axisRight.isEnabled = false

    // Configurazione legenda
    legend.isEnabled = false

    // Configurazione touch
    setDragEnabled(true)
    setScaleEnabled(true)
    isDoubleTapToZoomEnabled = false

    // Margini - Aumentati per più spazio
    setExtraOffsets(15f, 20f, 15f, 20f)
}

// Versione con altezza adattiva per tablet/schermi grandi
@Composable
private fun DailyChartCardAdaptive(
    weeklyData: WeeklyDataViewModel.WeeklyStatistics,
    filter: WeeklyDataViewModel.DataFilter
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val chartHeight = (screenHeight * 0.4f).coerceAtLeast(300.dp).coerceAtMost(500.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📊 Andamento Settimanale",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight) // Altezza adattiva
            ) {
                if (weeklyData.isEmpty) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nessun dato disponibile per questo periodo",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    DailyBarChart(
                        weeklyData = weeklyData,
                        filter = filter,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailedStatsCard(
    weeklyData: WeeklyDataViewModel.WeeklyStatistics,
    filter: WeeklyDataViewModel.DataFilter,
    period: WeeklyDataViewModel.Period
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🔍 Statistiche Dettagliate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(4) { index ->
                    DetailedStatItem(
                        title = when (index) {
                            0 -> "Miglior Giorno"
                            1 -> "Streak Attuale"
                            2 -> "Media Settimanale"
                            else -> "Obiettivo"
                        },
                        value = when (index) {
                            0 -> "Lunedì"
                            1 -> "5 giorni"
                            2 -> "4.2h"
                            else -> "85%"
                        },
                        color = when (index) {
                            0 -> Color(0xFF4CAF50)
                            1 -> Color(0xFF2196F3)
                            2 -> Color(0xFFFF9800)
                            else -> Color(0xFF9C27B0)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailedStatItem(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InsightsCard(
    weeklyData: WeeklyDataViewModel.WeeklyStatistics,
    period: WeeklyDataViewModel.Period
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "💡 Insights & Suggerimenti",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InsightItem(
                    text = "Le tue migliori performance sono nei giorni feriali. Considera di mantenere questa routine!",
                    icon = Icons.Default.TrendingUp
                )

                InsightItem(
                    text = "Hai aumentato il tempo di studio del 15% rispetto alla settimana scorsa. Ottimo lavoro!",
                    icon = Icons.Default.Celebration
                )

                InsightItem(
                    text = "Prova a fare pause più frequenti per migliorare la concentrazione.",
                    icon = Icons.Default.Psychology
                )
            }
        }
    }
}

@Composable
private fun InsightItem(
    text: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}