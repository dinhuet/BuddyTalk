package com.example.buddytalk.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.buddytalk.data.entity.StudySession

data class DailyCount(
    val dayLabel: String,
    val count: Int
)

data class DailyRaw(
    val dayDate: String,
    val count: Int
)

@Dao
interface StudySessionDao {
    @Insert
    suspend fun insert(session: StudySession)

    @Query("""
        SELECT date(timestamp / 1000, 'unixepoch') AS dayDate, COUNT(*) AS count
        FROM study_sessions
        WHERE timestamp >= :fromTimestamp
        GROUP BY dayDate
        ORDER BY dayDate
    """)
    suspend fun getDailyCounts(fromTimestamp: Long): List<DailyRaw>
}
