package com.example.fitjourney.ui.viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.fitjourney.data.StudyData
import com.example.fitjourney.util.viewModel.BaseViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class StudyViewModel : BaseViewModel() {

    private val _studyData = mutableStateOf(StudyData())
    val studyData: State<StudyData> = _studyData

    init {
        loadStudyData()
    }

    fun simulateStudySession() {
        _studyData.value = _studyData.value.copy(
            activeStudyTime = _studyData.value.activeStudyTime + 25,
            sessionsCompleted = _studyData.value.sessionsCompleted + 1
        )
        saveStudyData()
    }

    fun simulateBreak() {
        _studyData.value = _studyData.value.copy(
            breakTime = _studyData.value.breakTime + 5
        )
        saveStudyData()
    }

    fun simulateProgress() {
        _studyData.value = _studyData.value.copy(
            activeStudyTime = _studyData.value.activeStudyTime + 25,
            breakTime = _studyData.value.breakTime + 8,
            sessionsCompleted = _studyData.value.sessionsCompleted + 1
        )
        saveStudyData()
    }

    fun updateStudyGoal(newGoalMinutes: Int) {
        _studyData.value = _studyData.value.copy(
            studyGoalMinutes = newGoalMinutes.coerceIn(15, 720)
        )
        saveStudyData()
    }

    fun updateBreakGoal(newGoalMinutes: Int) {
        _studyData.value = _studyData.value.copy(
            breakGoalMinutes = newGoalMinutes.coerceIn(5, 240)
        )
        saveStudyData()
    }

    fun updateTotalGoal(newGoalMinutes: Int) {
        _studyData.value = _studyData.value.copy(
            totalGoalMinutes = newGoalMinutes.coerceIn(60, 960)
        )
        saveStudyData()
    }

    fun addLiveStudyTime(minutes: Int) {
        if (minutes <= 0) return
        _studyData.value = _studyData.value.copy(
            activeStudyTime = _studyData.value.activeStudyTime + minutes
        )
        saveStudyData()
    }

    fun incrementSessionCount() {
        _studyData.value = _studyData.value.copy(
            sessionsCompleted = _studyData.value.sessionsCompleted + 1
        )
        saveStudyData()
    }

    private fun saveStudyData() {
        val userId = getCurrentUserId() ?: return
        val date = getTodayDateString()
        val now = getCurrentTimestamp()
        val data = _studyData.value.copy(lastUpdated = now)

        val studyMap = hashMapOf(
            "activeStudyTime" to data.activeStudyTime,
            "breakTime" to data.breakTime,
            "totalTime" to data.calculatedTotalTime,
            "sessionsCompleted" to data.sessionsCompleted,
            "studyGoalMinutes" to data.studyGoalMinutes,
            "breakGoalMinutes" to data.breakGoalMinutes,
            "totalGoalMinutes" to data.totalGoalMinutes,
            "lastUpdated" to data.lastUpdated,
            "isTemporary" to data.isTemporary
        )

        firestore.collection("users")
            .document(userId)
            .collection("studyData")
            .document(date)
            .set(studyMap)
            .addOnSuccessListener {
                println("Dati studio salvati per $date")
            }
            .addOnFailureListener { e ->
                println("Errore nel salvataggio dati studio: ${e.message}")
            }
    }

    private fun loadStudyData() {
        val userId = getCurrentUserId() ?: return
        val date = getTodayDateString()

        firestore.collection("users")
            .document(userId)
            .collection("studyData")
            .document(date)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val loadedData = StudyData(
                        activeStudyTime = document.getLong("activeStudyTime")?.toInt() ?: 0,
                        breakTime = document.getLong("breakTime")?.toInt() ?: 0,
                        totalTime = document.getLong("totalTime")?.toInt() ?: 0,
                        sessionsCompleted = document.getLong("sessionsCompleted")?.toInt() ?: 0,
                        studyGoalMinutes = document.getLong("studyGoalMinutes")?.toInt() ?: 180,
                        breakGoalMinutes = document.getLong("breakGoalMinutes")?.toInt() ?: 60,
                        totalGoalMinutes = document.getLong("totalGoalMinutes")?.toInt() ?: 480,
                        lastUpdated = document.getString("lastUpdated") ?: "",
                        isTemporary = document.getBoolean("isTemporary") ?: false
                    )
                    _studyData.value = loadedData
                    println("Dati studio caricati per $date")
                }
            }
            .addOnFailureListener { e ->
                println("Errore caricamento dati studio: ${e.message}")
            }
    }
}
