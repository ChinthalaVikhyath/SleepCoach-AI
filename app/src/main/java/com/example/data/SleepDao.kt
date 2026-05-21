package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {

    // --- Sleep Logs ---
    @Query("SELECT * FROM sleep_logs ORDER BY date DESC")
    fun getAllSleepLogs(): Flow<List<SleepLog>>

    @Query("SELECT * FROM sleep_logs ORDER BY date DESC LIMIT 7")
    fun getRecentSleepLogs(): Flow<List<SleepLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(log: SleepLog)

    @Delete
    suspend fun deleteSleepLog(log: SleepLog)

    @Query("DELETE FROM sleep_logs")
    suspend fun deleteAllSleepLogs()

    // --- Lifestyle Logs ---
    @Query("SELECT * FROM lifestyle_logs ORDER BY date DESC")
    fun getAllLifestyleLogs(): Flow<List<LifestyleLog>>

    @Query("SELECT * FROM lifestyle_logs WHERE date = :date LIMIT 1")
    suspend fun getLifestyleLogByDate(date: String): LifestyleLog?

    @Query("SELECT * FROM lifestyle_logs ORDER BY date DESC LIMIT 7")
    fun getRecentLifestyleLogs(): Flow<List<LifestyleLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLifestyleLog(log: LifestyleLog)

    @Delete
    suspend fun deleteLifestyleLog(log: LifestyleLog)

    @Query("DELETE FROM lifestyle_logs")
    suspend fun deleteAllLifestyleLogs()

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()
}
