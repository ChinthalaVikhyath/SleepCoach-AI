package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class SleepRepository(private val sleepDao: SleepDao) {

    val allSleepLogs: Flow<List<SleepLog>> = sleepDao.getAllSleepLogs()
    val recentSleepLogs: Flow<List<SleepLog>> = sleepDao.getRecentSleepLogs()

    val allLifestyleLogs: Flow<List<LifestyleLog>> = sleepDao.getAllLifestyleLogs()
    val recentLifestyleLogs: Flow<List<LifestyleLog>> = sleepDao.getRecentLifestyleLogs()

    val chatMessages: Flow<List<ChatMessage>> = sleepDao.getAllChatMessages()

    suspend fun insertSleepLog(log: SleepLog) {
        sleepDao.insertSleepLog(log)
    }

    suspend fun deleteSleepLog(log: SleepLog) {
        sleepDao.deleteSleepLog(log)
    }

    suspend fun insertLifestyleLog(log: LifestyleLog) {
        sleepDao.insertLifestyleLog(log)
    }

    suspend fun deleteLifestyleLog(log: LifestyleLog) {
        sleepDao.deleteLifestyleLog(log)
    }

    suspend fun insertChatMessage(message: ChatMessage) {
        sleepDao.insertChatMessage(message)
    }

    suspend fun clearChatMessages() {
        sleepDao.clearChatMessages()
    }

    suspend fun populateSampleDataIfEmpty() {
        val sleepLogs = sleepDao.getAllSleepLogs().first()
        if (sleepLogs.isEmpty()) {
            val sdf = SimpleDateFormat("yyyy-MM-DD", Locale.US)
            val calendar = Calendar.getInstance()

            // Generate 5 days of history ending yesterday
            val today = calendar.time
            
            // Day 5: Yesterday
            calendar.time = today
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val date1 = getFormattedDate(calendar.time)
            sleepDao.insertSleepLog(
                SleepLog(
                    date = date1,
                    sleepScore = 88,
                    durationMinutes = 480, // 8h
                    deepSleepMinutes = 95,
                    remSleepMinutes = 90,
                    lightSleepMinutes = 275,
                    awakeMinutes = 20,
                    hrv = 52,
                    latencyMinutes = 8,
                    notes = "Very refreshing sleep after a solid run and reading a book before bed!"
                )
            )
            sleepDao.insertLifestyleLog(
                LifestyleLog(
                    date = date1,
                    caffeineMg = 80, // morning tea
                    alcoholUnits = 0,
                    exerciseMinutes = 45,
                    screenTimeMinutes = 15,
                    stressLevel = 2,
                    notes = "No stress today, light wind-down."
                )
            )

            // Day 4: 2 Days Ago
            calendar.time = today
            calendar.add(Calendar.DAY_OF_YEAR, -2)
            val date2 = getFormattedDate(calendar.time)
            sleepDao.insertSleepLog(
                SleepLog(
                    date = date2,
                    sleepScore = 82,
                    durationMinutes = 465, // 7h 45m
                    deepSleepMinutes = 85,
                    remSleepMinutes = 80,
                    lightSleepMinutes = 275,
                    awakeMinutes = 25,
                    hrv = 48,
                    latencyMinutes = 12,
                    notes = "Felt relaxed. Avoided screen before bed."
                )
            )
            sleepDao.insertLifestyleLog(
                LifestyleLog(
                    date = date2,
                    caffeineMg = 100,
                    alcoholUnits = 0,
                    exerciseMinutes = 30,
                    screenTimeMinutes = 20,
                    stressLevel = 3,
                    notes = "Productive day, moderate workout."
                )
            )

            // Day 3: 3 Days ago (Poor sleep night)
            calendar.time = today
            calendar.add(Calendar.DAY_OF_YEAR, -3)
            val date3 = getFormattedDate(calendar.time)
            sleepDao.insertSleepLog(
                SleepLog(
                    date = date3,
                    sleepScore = 55,
                    durationMinutes = 345, // 5h 45m
                    deepSleepMinutes = 30,
                    remSleepMinutes = 45,
                    lightSleepMinutes = 230,
                    awakeMinutes = 40,
                    hrv = 28,
                    latencyMinutes = 45, // high latency
                    notes = "Tossed and turned. Felt heart racing, likely wine and late dinner."
                )
            )
            sleepDao.insertLifestyleLog(
                LifestyleLog(
                    date = date3,
                    caffeineMg = 250, // high caffeine, late espresso
                    alcoholUnits = 3, // multiple drinks
                    exerciseMinutes = 0,
                    screenTimeMinutes = 150, // late work on laptop
                    stressLevel = 8, // high stress
                    notes = "Stressed about deadline. Had wine to help wind down (failed)."
                )
            )

            // Day 2: 4 Days ago
            calendar.time = today
            calendar.add(Calendar.DAY_OF_YEAR, -4)
            val date4 = getFormattedDate(calendar.time)
            sleepDao.insertSleepLog(
                SleepLog(
                    date = date4,
                    sleepScore = 71,
                    durationMinutes = 420, // 7h
                    deepSleepMinutes = 60,
                    remSleepMinutes = 65,
                    lightSleepMinutes = 260,
                    awakeMinutes = 35,
                    hrv = 38,
                    latencyMinutes = 25,
                    notes = "Decent sleep, woke up only once."
                )
            )
            sleepDao.insertLifestyleLog(
                LifestyleLog(
                    date = date4,
                    caffeineMg = 150, // early caffeine
                    alcoholUnits = 0,
                    exerciseMinutes = 20,
                    screenTimeMinutes = 60,
                    stressLevel = 6,
                    notes = "Mild stress, watched TV before sleeping."
                )
            )

            // Day 1: 5 Days ago (Late caffeine & alcohol)
            calendar.time = today
            calendar.add(Calendar.DAY_OF_YEAR, -5)
            val date5 = getFormattedDate(calendar.time)
            sleepDao.insertSleepLog(
                SleepLog(
                    date = date5,
                    sleepScore = 62,
                    durationMinutes = 390, // 6h 30m
                    deepSleepMinutes = 45,
                    remSleepMinutes = 50,
                    lightSleepMinutes = 250,
                    awakeMinutes = 45,
                    hrv = 32,
                    latencyMinutes = 35,
                    notes = "Trouble drifting to sleep. Caffeine late in the afternoon."
                )
            )
            sleepDao.insertLifestyleLog(
                LifestyleLog(
                    date = date5,
                    caffeineMg = 300, // very late coffee
                    alcoholUnits = 2, // bedtime beer
                    exerciseMinutes = 0,
                    screenTimeMinutes = 120,
                    stressLevel = 7,
                    notes = "Social gathering, late caffeine and beer."
                )
            )

            // Insert a warm introductory message from SleepCoach AI
            sleepDao.insertChatMessage(
                ChatMessage(
                    text = "Hello! I am **SleepCoach AI**, your personalized sleep wellness companion. 🌟\n\nI have imported your recent sleep & lifestyle logs so we can begin tracking your patterns right away.\n\nI notice that your sleep quality fluctuates heavily depending on factors like **afternoon caffeine consumption**, **bedtime screen usage**, and **late-evening alcohol**.\n\nHow did you sleep last night, or what specific area of your sleep would you like to discuss? I can help with nighttime reports, building a wind-down routine, or looking at circadian rhythms!",
                    isUser = false,
                    timestamp = System.currentTimeMillis() - 10000
                )
            )
        }
    }

    private fun getFormattedDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(date)
    }
}
