package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DailyRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecordDao {
    @Query("SELECT * FROM daily_records WHERE date = :date LIMIT 1")
    fun getRecordForDate(date: String): Flow<DailyRecord?>

    @Query("SELECT * FROM daily_records WHERE date = :date LIMIT 1")
    suspend fun getRecordForDateSync(date: String): DailyRecord?

    @Query("SELECT * FROM daily_records ORDER BY date DESC LIMIT :limit")
    fun getRecentRecords(limit: Int): Flow<List<DailyRecord>>

    @Query("SELECT * FROM daily_records ORDER BY date ASC")
    fun getAllRecords(): Flow<List<DailyRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: DailyRecord)

    @Query("UPDATE daily_records SET steps = :steps WHERE date = :date")
    suspend fun updateSteps(date: String, steps: Int)

    @Query("UPDATE daily_records SET water = :water WHERE date = :date")
    suspend fun updateWater(date: String, water: Int)

    @Query("UPDATE daily_records SET sleepMinutes = :sleepMinutes, bedTime = :bedTime, wakeTime = :wakeTime WHERE date = :date")
    suspend fun updateSleep(date: String, sleepMinutes: Int, bedTime: String?, wakeTime: String?)
}
