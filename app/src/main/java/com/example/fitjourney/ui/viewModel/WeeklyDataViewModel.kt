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

    private val _isUserAuthenticated = MutableStateFlow(false)
    val isUserAuthenticated: StateFlow<Boolean> = _isUserAuthenticated

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

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
        // Dati settimanali (ultime 4 settimane)
        val weeklyStudyTime: List<Float> = List(4) { 0f },
        val weeklyBreakTime: List<Float> = List(4) { 0f },
        val weeklyTotalTime: List<Float> = List(4) { 0f },
        val weeklySessions: List<Int> = List(4) { 0 },
        // Dati mensili (ultimi 6 mesi)
        val monthlyStudyTime: List<Float> = List(6) { 0f },
        val monthlyBreakTime: List<Float> = List(6) { 0f },
        val monthlyTotalTime: List<Float> = List(6) { 0f },
        val monthlySessions: List<Int> = List(6) { 0 },
        val weeklyStudyGoal: Int = 1260, // 180 * 7 = 21 ore settimanali
        val weeklyBreakGoal: Int = 420,  // 60 * 7 = 7 ore settimanali
        val studyGoalProgress: Float = 0f,
        val breakGoalProgress: Float = 0f,
        val averageStudyPerDay: Float = 0f,
        val averageBreakPerDay: Float = 0f,
        val mostProductiveDay: String = "",
        val weekStartDate: String = "",
        val weekEndDate: String = "",
        val trendPercentage: Float = 0f, // Trend rispetto al periodo precedente
        val bestWeek: String = "",
        val bestMonth: String = "",
        val isEmpty: Boolean = true // Indica se ci sono dati da mostrare
    )

    init {
        checkUserAuthentication()
    }

    private fun checkUserAuthentication() {
        val userId = getCurrentUserId()
        _isUserAuthenticated.value = userId != null

        if (userId != null) {
            loadWeeklyData()
        } else {
            _errorMessage.value = "Effettua il login per visualizzare le tue statistiche di studio"
            resetToEmptyState()
        }
    }

    private fun resetToEmptyState() {
        _weeklyData.value = WeeklyStatistics(isEmpty = true)
        _isLoading.value = false
    }

    fun setFilter(filter: DataFilter) {
        if (!_isUserAuthenticated.value) {
            _errorMessage.value = "Effettua il login per visualizzare le statistiche"
            return
        }
        _currentFilter.value = filter
    }

    fun setPeriod(period: Period) {
        if (!_isUserAuthenticated.value) {
            _errorMessage.value = "Effettua il login per visualizzare le statistiche"
            return
        }
        _currentPeriod.value = period
        loadWeeklyData() // Ricarica i dati per il nuovo periodo
    }

    fun loadWeeklyData() {
        val userId = getCurrentUserId()
        if (userId == null) {
            _isUserAuthenticated.value = false
            _errorMessage.value = "Sessione scaduta. Effettua nuovamente il login"
            resetToEmptyState()
            return
        }

        _isUserAuthenticated.value = true
        _errorMessage.value = null
        _isLoading.value = true

        when (_currentPeriod.value) {
            Period.DAILY -> loadDailyData(userId)
            Period.WEEKLY -> loadWeeklyData(userId)
            Period.MONTHLY -> loadMonthlyData(userId)
        }
    }

    fun refreshData() {
        checkUserAuthentication()
    }

    fun clearError() {
        _errorMessage.value = null
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
                .addOnFailureListener { exception ->
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

                        // Log dell'errore per debug
                        _errorMessage.value = "Errore nel caricamento di alcuni dati: ${exception.message}"
                    }
                }

            weekStart.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun loadWeeklyData(userId: String) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val weeklyStudyTime = mutableListOf<Float>()
        val weeklyBreakTime = mutableListOf<Float>()
        val weeklyTotalTime = mutableListOf<Float>()
        val weeklySessions = mutableListOf<Int>()

        var weeksProcessed = 0
        var totalStudy = 0
        var totalBreak = 0
        var totalSessions = 0

        // Carica i dati delle ultime 4 settimane
        for (weekOffset in 0..3) {
            val weekCalendar = Calendar.getInstance()
            weekCalendar.add(Calendar.WEEK_OF_YEAR, -weekOffset)
            weekCalendar.firstDayOfWeek = Calendar.MONDAY
            weekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            var weekStudy = 0f
            var weekBreak = 0f
            var weekSessions = 0
            var daysInWeekProcessed = 0

            // Per ogni giorno della settimana
            for (dayOffset in 0..6) {
                val dayCalendar = weekCalendar.clone() as Calendar
                dayCalendar.add(Calendar.DAY_OF_MONTH, dayOffset)
                val currentDate = dateFormat.format(dayCalendar.time)

                firestore.collection("users")
                    .document(userId)
                    .collection("studyData")
                    .document(currentDate)
                    .get()
                    .addOnSuccessListener { document ->
                        val studyTime = document.getLong("activeStudyTime")?.toFloat() ?: 0f
                        val breakTime = document.getLong("breakTime")?.toFloat() ?: 0f
                        val sessions = document.getLong("sessionsCompleted")?.toInt() ?: 0

                        weekStudy += studyTime
                        weekBreak += breakTime
                        weekSessions += sessions

                        daysInWeekProcessed++

                        if (daysInWeekProcessed == 7) {
                            weeklyStudyTime.add(weekStudy)
                            weeklyBreakTime.add(weekBreak)
                            weeklyTotalTime.add(weekStudy + weekBreak)
                            weeklySessions.add(weekSessions)

                            if (weekOffset == 0) { // Settimana corrente
                                totalStudy = weekStudy.toInt()
                                totalBreak = weekBreak.toInt()
                                totalSessions = weekSessions
                            }

                            weeksProcessed++

                            if (weeksProcessed == 4) {
                                updateWeeklyStatisticsForWeeklyView(
                                    totalStudy = totalStudy,
                                    totalBreak = totalBreak,
                                    totalSessions = totalSessions,
                                    weeklyStudyTime = weeklyStudyTime.reversed(), // Ordine cronologico
                                    weeklyBreakTime = weeklyBreakTime.reversed(),
                                    weeklyTotalTime = weeklyTotalTime.reversed(),
                                    weeklySessions = weeklySessions.reversed()
                                )
                                _isLoading.value = false
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        daysInWeekProcessed++
                        if (daysInWeekProcessed == 7) {
                            weeklyStudyTime.add(weekStudy)
                            weeklyBreakTime.add(weekBreak)
                            weeklyTotalTime.add(weekStudy + weekBreak)
                            weeklySessions.add(weekSessions)

                            weeksProcessed++

                            if (weeksProcessed == 4) {
                                updateWeeklyStatisticsForWeeklyView(
                                    totalStudy = totalStudy,
                                    totalBreak = totalBreak,
                                    totalSessions = totalSessions,
                                    weeklyStudyTime = weeklyStudyTime.reversed(),
                                    weeklyBreakTime = weeklyBreakTime.reversed(),
                                    weeklyTotalTime = weeklyTotalTime.reversed(),
                                    weeklySessions = weeklySessions.reversed()
                                )
                                _isLoading.value = false

                                if (totalStudy == 0 && totalBreak == 0 && totalSessions == 0) {
                                    _errorMessage.value = "Nessun dato trovato per questo periodo"
                                }
                            }
                        }
                    }
            }
        }
    }

    private fun loadMonthlyData(userId: String) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val monthlyStudyTime = mutableListOf<Float>()
        val monthlyBreakTime = mutableListOf<Float>()
        val monthlyTotalTime = mutableListOf<Float>()
        val monthlySessions = mutableListOf<Int>()

        var monthsProcessed = 0
        var totalStudy = 0
        var totalBreak = 0
        var totalSessions = 0

        // Carica i dati degli ultimi 6 mesi
        for (monthOffset in 0..5) {
            val monthCalendar = Calendar.getInstance()
            monthCalendar.add(Calendar.MONTH, -monthOffset)
            monthCalendar.set(Calendar.DAY_OF_MONTH, 1)

            val lastDayOfMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            var monthStudy = 0f
            var monthBreak = 0f
            var monthSessions = 0
            var daysInMonthProcessed = 0

            // Per ogni giorno del mese
            for (day in 1..lastDayOfMonth) {
                val dayCalendar = monthCalendar.clone() as Calendar
                dayCalendar.set(Calendar.DAY_OF_MONTH, day)
                val currentDate = dateFormat.format(dayCalendar.time)

                firestore.collection("users")
                    .document(userId)
                    .collection("studyData")
                    .document(currentDate)
                    .get()
                    .addOnSuccessListener { document ->
                        val studyTime = document.getLong("activeStudyTime")?.toFloat() ?: 0f
                        val breakTime = document.getLong("breakTime")?.toFloat() ?: 0f
                        val sessions = document.getLong("sessionsCompleted")?.toInt() ?: 0

                        monthStudy += studyTime
                        monthBreak += breakTime
                        monthSessions += sessions

                        daysInMonthProcessed++

                        if (daysInMonthProcessed == lastDayOfMonth) {
                            monthlyStudyTime.add(monthStudy)
                            monthlyBreakTime.add(monthBreak)
                            monthlyTotalTime.add(monthStudy + monthBreak)
                            monthlySessions.add(monthSessions)

                            if (monthOffset == 0) { // Mese corrente
                                totalStudy = monthStudy.toInt()
                                totalBreak = monthBreak.toInt()
                                totalSessions = monthSessions
                            }

                            monthsProcessed++

                            if (monthsProcessed == 6) {
                                updateWeeklyStatisticsForMonthlyView(
                                    totalStudy = totalStudy,
                                    totalBreak = totalBreak,
                                    totalSessions = totalSessions,
                                    monthlyStudyTime = monthlyStudyTime.reversed(), // Ordine cronologico
                                    monthlyBreakTime = monthlyBreakTime.reversed(),
                                    monthlyTotalTime = monthlyTotalTime.reversed(),
                                    monthlySessions = monthlySessions.reversed()
                                )
                                _isLoading.value = false
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        daysInMonthProcessed++
                        if (daysInMonthProcessed == lastDayOfMonth) {
                            monthlyStudyTime.add(monthStudy)
                            monthlyBreakTime.add(monthBreak)
                            monthlyTotalTime.add(monthStudy + monthBreak)
                            monthlySessions.add(monthSessions)

                            monthsProcessed++

                            if (monthsProcessed == 6) {
                                updateWeeklyStatisticsForMonthlyView(
                                    totalStudy = totalStudy,
                                    totalBreak = totalBreak,
                                    totalSessions = totalSessions,
                                    monthlyStudyTime = monthlyStudyTime.reversed(),
                                    monthlyBreakTime = monthlyBreakTime.reversed(),
                                    monthlyTotalTime = monthlyTotalTime.reversed(),
                                    monthlySessions = monthlySessions.reversed()
                                )
                                _isLoading.value = false

                                if (totalStudy == 0 && totalBreak == 0 && totalSessions == 0) {
                                    _errorMessage.value = "Nessun dato trovato per questo periodo"
                                }
                            }
                        }
                    }
            }
        }
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
        val weeklyStudyGoal = 1260 // 3 ore al giorno * 7 giorni (in minuti)
        val weeklyBreakGoal = 420  // 1 ora al giorno * 7 giorni (in minuti)

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
        val mostProductiveDay = if (dailyStudyTime[maxStudyIndex] > 0) dayNames[maxStudyIndex] else "Nessuno"

        val hasData = totalStudy > 0 || totalBreak > 0 || totalSessions > 0

        _weeklyData.value = _weeklyData.value.copy(
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
            weekEndDate = weekEndDate,
            isEmpty = !hasData
        )
    }

    private fun updateWeeklyStatisticsForWeeklyView(
        totalStudy: Int,
        totalBreak: Int,
        totalSessions: Int,
        weeklyStudyTime: List<Float>,
        weeklyBreakTime: List<Float>,
        weeklyTotalTime: List<Float>,
        weeklySessions: List<Int>
    ) {
        // Calcola trend rispetto alla settimana precedente
        val currentWeekStudy = weeklyStudyTime.lastOrNull() ?: 0f
        val previousWeekStudy = weeklyStudyTime.getOrNull(weeklyStudyTime.size - 2) ?: 0f
        val trendPercentage = if (previousWeekStudy > 0) {
            ((currentWeekStudy - previousWeekStudy) / previousWeekStudy) * 100
        } else 0f

        val bestWeekIndex = weeklyStudyTime.withIndex().maxByOrNull { it.value }?.index ?: 0
        val bestWeek = "Settimana ${4 - bestWeekIndex}"

        val hasData = totalStudy > 0 || totalBreak > 0 || totalSessions > 0

        _weeklyData.value = _weeklyData.value.copy(
            totalStudyTime = totalStudy,
            totalBreakTime = totalBreak,
            totalTime = totalStudy + totalBreak,
            totalSessions = totalSessions,
            weeklyStudyTime = weeklyStudyTime,
            weeklyBreakTime = weeklyBreakTime,
            weeklyTotalTime = weeklyTotalTime,
            weeklySessions = weeklySessions,
            trendPercentage = trendPercentage,
            bestWeek = bestWeek,
            isEmpty = !hasData
        )
    }

    private fun updateWeeklyStatisticsForMonthlyView(
        totalStudy: Int,
        totalBreak: Int,
        totalSessions: Int,
        monthlyStudyTime: List<Float>,
        monthlyBreakTime: List<Float>,
        monthlyTotalTime: List<Float>,
        monthlySessions: List<Int>
    ) {
        // Calcola trend rispetto al mese precedente
        val currentMonthStudy = monthlyStudyTime.lastOrNull() ?: 0f
        val previousMonthStudy = monthlyStudyTime.getOrNull(monthlyStudyTime.size - 2) ?: 0f
        val trendPercentage = if (previousMonthStudy > 0) {
            ((currentMonthStudy - previousMonthStudy) / previousMonthStudy) * 100
        } else 0f

        val monthNames = listOf("6 mesi fa", "5 mesi fa", "4 mesi fa", "3 mesi fa", "2 mesi fa", "Mese corrente")
        val bestMonthIndex = monthlyStudyTime.withIndex().maxByOrNull { it.value }?.index ?: 0
        val bestMonth = monthNames[bestMonthIndex]

        val hasData = totalStudy > 0 || totalBreak > 0 || totalSessions > 0

        _weeklyData.value = _weeklyData.value.copy(
            totalStudyTime = totalStudy,
            totalBreakTime = totalBreak,
            totalTime = totalStudy + totalBreak,
            totalSessions = totalSessions,
            monthlyStudyTime = monthlyStudyTime,
            monthlyBreakTime = monthlyBreakTime,
            monthlyTotalTime = monthlyTotalTime,
            monthlySessions = monthlySessions,
            trendPercentage = trendPercentage,
            bestMonth = bestMonth,
            isEmpty = !hasData
        )
    }
}