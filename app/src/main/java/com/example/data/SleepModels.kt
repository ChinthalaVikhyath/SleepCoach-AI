package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // format: YYYY-MM-DD
    val sleepScore: Int, // 0 - 100
    val durationMinutes: Int, 
    val deepSleepMinutes: Int,
    val remSleepMinutes: Int,
    val lightSleepMinutes: Int,
    val awakeMinutes: Int,
    val hrv: Int, // HRV in ms
    val latencyMinutes: Int, // latency to fall asleep in minutes
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "lifestyle_logs")
data class LifestyleLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // format: YYYY-MM-DD
    val caffeineMg: Int, // caffeine consumption in mg
    val alcoholUnits: Int, // alcohol units
    val exerciseMinutes: Int, // exercise duration
    val screenTimeMinutes: Int, // screen time before bed in minutes
    val stressLevel: Int, // scale 1 to 10
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
