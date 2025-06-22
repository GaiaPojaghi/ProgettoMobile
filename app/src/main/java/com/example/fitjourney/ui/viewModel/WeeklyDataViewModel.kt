package com.example.fitjourney.ui.viewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.fitjourney.util.viewModel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

class WeeklyDataViewModel : BaseViewModel() {

    private val _weeklyData = MutableStateFlow(WeeklyStatistics())
    val weeklyData: StateFlow<WeeklyStatistics> = _weeklyData

    private val _currentFilter = MutableStateFlow(DataFilter.STUDY)
    val currentFilter: StateFlow<DataFilter> = _currentFilter

    private val _currentPeriod = MutableStateFlow(Period.DAILY)
    val currentPeriod: StateFlow<Period> = _currentPeriod

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    enum class DataFilter(val displayName: String, val icon: ImageVector) {
        STUDY("Studio", Icons.Default.School),
        BREAK("Pause", Icons.Default.Coffee),
        TOTAL("Totale", Icons.Default.Timer),
        SESSIONS("Sessioni", Icons.Default.PlayArrow)
    }

    enum class Period(val displayName: String, val icon: ImageVector) {
        DAILY("Giornaliero", Icons.Default.Today),
        WEEKLY("Settimanale", Icons.Default.DateRange),
        MONTHLY("Mensile", Icons.Default.CalendarMonth)
    }

    data class WeeklyStatistics(
        val totalStudyTime: Int = 0,
        val totalBreakTime: Int = 0,
        val totalTime: Int = 0,
        val totalSessions: Int = 0,
        val dailyStudyTime: List<Float> = List(7) { 0f },
        val dailyBreakTime: List<Float> = List(7) { 0f },
        val dailyTotalTime: List<Float> = List(7) { 0f },
        val dailySessions: List<Int> = List(7) { 0 },
        val weeklyStudyGoal: Int = 1260, // 180 * 7 = 21 ore settimanali
        val weeklyBreakGoal: Int = 420,  // 60 * 7 = 7 ore settimanali
        val studyGoalProgress: Float = 0f,
        val breakGoalProgress: Float = 0f,
        val averageStudyPerDay: Float = 0f,
        val averageBreakPerDay: Float = 0f,
        val mostProductiveDay: String = "",
        val weekStartDate: String = "",
        val weekEndDate: String = ""
    )

    fun setFilter(filter: DataFilter) {
        _currentFilter.value = filter
    }

    fun setPeriod(period: Period) {
        _currentPeriod.value = period
        loadWeeklyData() // Ricarica i dati per il nuovo periodo
    }

    fun loadWeeklyData() {
        val userId = getCurrentUserId() ?: return
        _isLoading.value = true

        when (_currentPeriod.value) {
            Period.DAILY -> loadDailyData(userId)
            Period.WEEKLY -> loadWeeklyData(userId)
            Period.MONTHLY -> loadMonthlyData(userId)
        }
    }

    private fun loadDailyData(userId: String) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Trova l'inizio della settimana (lunedì)
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val weekStartDate = dateFormat.format(calendar.time)
        val weekStart = calendar.clone() as Calendar

        // Fine settimana (domenica)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val weekEndDate = dateFormat.format(calendar.time)

        val dailyStudyTime = mutableListOf<Float>()
        val dailyBreakTime = mutableListOf<Float>()
        val dailyTotalTime = mutableListOf<Float>()
        val dailySessions = mutableListOf<Int>()

        var totalStudy = 0
        var totalBreak = 0
        var totalSessions = 0
        var daysProcessed = 0

        // Carica i dati per ogni giorno della settimana
        for (i in 0..6) {
            val currentDate = dateFormat.format(weekStart.time)

            firestore.collection("users")
                .document(userId)
                .collection("studyData")
                .document(currentDate)
                .get()
                .addOnSuccessListener { document ->
                    val studyTime = document.getLong("activeStudyTime")?.toFloat() ?: 0f
                    val breakTime = document.getLong("breakTime")?.toFloat() ?: 0f
                    val sessions = document.getLong("sessionsCompleted")?.toInt() ?: 0

                    dailyStudyTime.add(studyTime)
                    dailyBreakTime.add(breakTime)
                    dailyTotalTime.add(studyTime + breakTime)
                    dailySessions.add(sessions)

                    totalStudy += studyTime.toInt()
                    totalBreak += breakTime.toInt()
                    totalSessions += sessions

                    daysProcessed++

                    // Quando tutti i giorni sono stati processati
                    if (daysProcessed == 7) {
                        updateWeeklyStatistics(
                            totalStudy = totalStudy,
                            totalBreak = totalBreak,
                            totalSessions = totalSessions,
                            dailyStudyTime = dailyStudyTime,
                            dailyBreakTime = dailyBreakTime,
                            dailyTotalTime = dailyTotalTime,
                            dailySessions = dailySessions,
                            weekStartDate = weekStartDate,
                            weekEndDate = weekEndDate
                        )
                        _isLoading.value = false
                    }
                }
                .addOnFailureListener {
                    // In caso di errore, aggiungi valori zero
                    dailyStudyTime.add(0f)
                    dailyBreakTime.add(0f)
                    dailyTotalTime.add(0f)
                    dailySessions.add(0)

                    daysProcessed++

                    if (daysProcessed == 7) {
                        updateWeeklyStatistics(
                            totalStudy = totalStudy,
                            totalBreak = totalBreak,
                            totalSessions = totalSessions,
                            dailyStudyTime = dailyStudyTime,
                            dailyBreakTime = dailyBreakTime,
                            dailyTotalTime = dailyTotalTime,
                            dailySessions = dailySessions,
                            weekStartDate = weekStartDate,
                            weekEndDate = weekEndDate
                        )
                        _isLoading.value = false
                    }
                }

            weekStart.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun loadWeeklyData(userId: String) {
        // TODO: Implementa il caricamento dei dati settimanali per le ultime 4 settimane
        _isLoading.value = false
    }

    private fun loadMonthlyData(userId: String) {
        // TODO: Implementa il caricamento dei dati mensili
        _isLoading.value = false
    }

    private fun updateWeeklyStatistics(
        totalStudy: Int,
        totalBreak: Int,
        totalSessions: Int,
        dailyStudyTime: List<Float>,
        dailyBreakTime: List<Float>,
        dailyTotalTime: List<Float>,
        dailySessions: List<Int>,
        weekStartDate: String,
        weekEndDate: String
    ) {
        val weeklyStudyGoal = 1260 // 3 ore al giorno * 7 giorni
        val weeklyBreakGoal = 420  // 1 ora al giorno * 7 giorni

        val studyProgress = if (weeklyStudyGoal > 0) {
            totalStudy.toFloat() / weeklyStudyGoal.toFloat()
        } else 0f

        val breakProgress = if (weeklyBreakGoal > 0) {
            totalBreak.toFloat() / weeklyBreakGoal.toFloat()
        } else 0f

        val averageStudyPerDay = totalStudy / 7f
        val averageBreakPerDay = totalBreak / 7f

        // Trova il giorno più produttivo
        val dayNames = listOf("Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica")
        val maxStudyIndex = dailyStudyTime.withIndex().maxByOrNull { it.value }?.index ?: 0
        val mostProductiveDay = dayNames[maxStudyIndex]

        _weeklyData.value = WeeklyStatistics(
            totalStudyTime = totalStudy,
            totalBreakTime = totalBreak,
            totalTime = totalStudy + totalBreak,
            totalSessions = totalSessions,
            dailyStudyTime = dailyStudyTime,
            dailyBreakTime = dailyBreakTime,
            dailyTotalTime = dailyTotalTime,
            dailySessions = dailySessions,
            weeklyStudyGoal = weeklyStudyGoal,
            weeklyBreakGoal = weeklyBreakGoal,
            studyGoalProgress = studyProgress.coerceIn(0f, 1f),
            breakGoalProgress = breakProgress.coerceIn(0f, 1f),
            averageStudyPerDay = averageStudyPerDay,
            averageBreakPerDay = averageBreakPerDay,
            mostProductiveDay = mostProductiveDay,
            weekStartDate = weekStartDate,
            weekEndDate = weekEndDate
        )
    }
}

