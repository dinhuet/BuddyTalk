package com.example.buddytalk.data.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddytalk.data.database.AppDatabase
import com.example.buddytalk.data.repository.StudySessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class BarData(
    val label: String,
    val count: Int,
    val heightFraction: Float
)

data class AnalyticsUiState(
    val weeklyBars: List<BarData> = emptyList(),
    val maxCount: Int = 0
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StudySessionRepository

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).studySessionDao()
        repository = StudySessionRepository(dao)
        loadWeeklyData()
    }

    fun loadWeeklyData() {
        viewModelScope.launch {
            val now = Calendar.getInstance()
            val weekAgo = now.clone() as Calendar
            weekAgo.add(Calendar.DAY_OF_YEAR, -6)
            weekAgo.set(Calendar.HOUR_OF_DAY, 0)
            weekAgo.set(Calendar.MINUTE, 0)
            weekAgo.set(Calendar.SECOND, 0)
            weekAgo.set(Calendar.MILLISECOND, 0)

            val raw = withContext(Dispatchers.IO) {
                repository.getDailyCounts(weekAgo.timeInMillis)
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
            val rawMap = raw.associate { it.dayDate to it.count }

            val calendar = weekAgo.clone() as Calendar
            val bars = mutableListOf<BarData>()
            var maxCount = 0

            for (i in 0 until 7) {
                val dateStr = dateFormat.format(Date(calendar.timeInMillis))
                val count = rawMap[dateStr] ?: 0
                if (count > maxCount) maxCount = count
                val labelIndex = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
                bars.add(BarData(label = dayLabels[labelIndex], count = count, heightFraction = 0f))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            if (maxCount > 0) {
                for (i in bars.indices) {
                    bars[i] = bars[i].copy(heightFraction = bars[i].count.toFloat() / maxCount)
                }
            }

            _uiState.value = AnalyticsUiState(weeklyBars = bars, maxCount = maxCount)
        }
    }
}
