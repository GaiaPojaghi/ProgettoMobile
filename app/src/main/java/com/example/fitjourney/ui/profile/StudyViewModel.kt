package com.example.fitjourney.ui.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

data class StudyData(
    val totalStudyTime: Int = 0,
    val focusTime: Int = 0,
    val breakTime: Int = 0,
    val sessionsCompleted: Int = 0,
    val goalMinutes: Int = 240
)

class StudyViewModel : ViewModel() {
    private val _studyData = mutableStateOf(StudyData())
    val studyData: State<StudyData> = _studyData

    fun simulateProgress() {
        _studyData.value = _studyData.value.copy(
            totalStudyTime = _studyData.value.totalStudyTime + 25,
            focusTime = _studyData.value.focusTime + 20,
            breakTime = _studyData.value.breakTime + 5,
            sessionsCompleted = _studyData.value.sessionsCompleted + 1
        )
    }

    fun addLiveStudyTime(seconds: Int) {
        if (seconds <= 0) return
        val minutes = (seconds / 60f).toInt() // Arrotonda per difetto, oppure usa ceil per arrotondare in su

        _studyData.value = _studyData.value.copy(
            totalStudyTime = _studyData.value.totalStudyTime + minutes,
            focusTime = _studyData.value.focusTime + (minutes * 4 / 5),
            breakTime = _studyData.value.breakTime + (minutes * 1 / 5)
        )
    }


    fun incrementSessionCount() {
        _studyData.value = _studyData.value.copy(
            sessionsCompleted = _studyData.value.sessionsCompleted + 1
        )
    }
}