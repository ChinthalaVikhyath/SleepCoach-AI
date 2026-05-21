package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ChatMessage
import com.example.data.LifestyleLog
import com.example.data.SleepLog
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SleepCoachApp()
            }
        }
    }
}

enum class SleepScreen {
    Dashboard,
    Coach
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepCoachApp(viewModel: MainViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf(SleepScreen.Dashboard) }
    var showLogSleep by remember { mutableStateOf(false) }
    var showLogLifestyle by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .frostedMeshBackground(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.NightsStay,
                                contentDescription = "Sleep Moon",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SleepCoach AI",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = 0.5.sp,
                                fontSize = 20.sp
                            )
                        }
                        Text(
                            text = "Your Circadian & CBT-I Wellness Guide",
                            style = MaterialTheme.styleTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F1113).copy(alpha = 0.85f),
                                Color(0xFF0F1113).copy(alpha = 0.45f)
                            )
                        )
                    )
                    .drawBehind {
                        drawLine(
                            color = Color.White.copy(alpha = 0.12f),
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    },
                actions = {
                    if (currentScreen == SleepScreen.Coach) {
                        IconButton(
                            onClick = {
                                viewModel.clearChat()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Coaching session cleared")
                                }
                            },
                            modifier = Modifier.testTag("clear_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Clear Chat",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F1113).copy(alpha = 0.65f),
                                Color(0xFF0C0E10).copy(alpha = 0.95f)
                            )
                        )
                    )
                    .drawBehind {
                        drawLine(
                            color = Color.White.copy(alpha = 0.12f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentScreen == SleepScreen.Dashboard,
                    onClick = { currentScreen = SleepScreen.Dashboard },
                    label = { Text("Dashboard") },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == SleepScreen.Dashboard) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Dashboard Icon"
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.GlassPrimary,
                        selectedTextColor = com.example.ui.theme.GlassPrimary,
                        indicatorColor = Color.White.copy(alpha = 0.08f),
                        unselectedIconColor = com.example.ui.theme.GlassOnSurface.copy(alpha = 0.55f),
                        unselectedTextColor = com.example.ui.theme.GlassOnSurface.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = currentScreen == SleepScreen.Coach,
                    onClick = { currentScreen = SleepScreen.Coach },
                    label = { Text("AI Coach") },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == SleepScreen.Coach) Icons.Filled.Psychology else Icons.Outlined.Psychology,
                            contentDescription = "Coach Icon"
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.GlassPrimary,
                        selectedTextColor = com.example.ui.theme.GlassPrimary,
                        indicatorColor = Color.White.copy(alpha = 0.08f),
                        unselectedIconColor = com.example.ui.theme.GlassOnSurface.copy(alpha = 0.55f),
                        unselectedTextColor = com.example.ui.theme.GlassOnSurface.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier.testTag("nav_coach")
                )
            }
        },
        floatingActionButton = {
            if (currentScreen == SleepScreen.Dashboard) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { showLogLifestyle = true },
                        text = { Text("Log Habits", fontSize = 13.sp) },
                        icon = { Icon(Icons.Default.Coffee, contentDescription = "Caffeine Icon") },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.testTag("fab_log_lifestyle")
                    )

                    ExtendedFloatingActionButton(
                        onClick = { showLogSleep = true },
                        text = { Text("Log Sleep", fontSize = 13.sp) },
                        icon = { Icon(Icons.Default.Bedtime, contentDescription = "Bedtime Icon") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("fab_log_sleep")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (currentScreen) {
                SleepScreen.Dashboard -> {
                    DashboardScreen(viewModel)
                }
                SleepScreen.Coach -> {
                    CoachChatScreen(viewModel, onNavigateToDashboard = { currentScreen = SleepScreen.Dashboard })
                }
            }
        }
    }

    if (showLogSleep) {
        LogSleepDialog(
            onDismiss = { showLogSleep = false },
            onSave = { date, score, duration, deep, rem, light, awake, hrv, latency, notes ->
                viewModel.logSleep(date, score, duration, deep, rem, light, awake, hrv, latency, notes)
                showLogSleep = false
                scope.launch {
                    snackbarHostState.showSnackbar("Night report recorded and analyzed! Check AI Coach.")
                }
            }
        )
    }

    if (showLogLifestyle) {
        LogLifestyleDialog(
            onDismiss = { showLogLifestyle = false },
            onSave = { date, caffeine, alcohol, exercise, screenTime, stress, notes ->
                viewModel.logLifestyle(date, caffeine, alcohol, exercise, screenTime, stress, notes)
                showLogLifestyle = false
                scope.launch {
                    snackbarHostState.showSnackbar("Lifestyle habits recorded.")
                }
            }
        )
    }
}

// --- FROSTED GLASS STYLING EXTENSIONS ---

fun Modifier.frostedMeshBackground(): Modifier = this.drawBehind {
    // Solid background base
    drawRect(color = Color(0xFF0F1113))

    // Top-left radial mesh gradient (Rich Royal Indigo/Purple `#2D235C`)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF2D235C).copy(alpha = 0.55f), Color.Transparent),
            center = Offset(0f, 0f),
            radius = size.width * 0.9f
        ),
        radius = size.width * 0.9f,
        center = Offset(0f, 0f)
    )

    // Bottom-right radial mesh gradient (Sleek Deep Teal/Blue `#1B3A4B`)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF1B3A4B).copy(alpha = 0.55f), Color.Transparent),
            center = Offset(size.width, size.height),
            radius = size.width * 0.9f
        ),
        radius = size.width * 0.9f,
        center = Offset(size.width, size.height)
    )
}

fun Modifier.glassPanel(shape: RoundedCornerShape = RoundedCornerShape(24.dp)): Modifier = this
    .background(
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.055f),
                Color.White.copy(alpha = 0.015f)
            )
        ),
        shape
    )
    .border(
        BorderStroke(
            1.dp,
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.16f),
                    Color.White.copy(alpha = 0.03f)
                )
            )
        ),
        shape
    )

// --- DASHBOARD SCREEN COMPOSABLE ---
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val sleepLogs by viewModel.sleepLogs.collectAsStateWithLifecycle()
    val lifestyleLogs by viewModel.lifestyleLogs.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper-Header visual based on Frosted Glass mock template
        if (sleepLogs.isNotEmpty()) {
            val latestLog = sleepLogs.first()
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LAST NIGHT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC4C6D0),
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = androidx.compose.ui.text.buildAnnotatedString {
                                append("Restorative ")
                                withStyle(style = androidx.compose.ui.text.SpanStyle(color = com.example.ui.theme.GlassPrimary, fontWeight = FontWeight.Bold)) {
                                    append("${latestLog.sleepScore}")
                                }
                            },
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF1A1C1E), CircleShape)
                            .border(2.dp, com.example.ui.theme.GlassPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${latestLog.sleepScore}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.GlassPrimary
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassPanel(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sleep Score Trend",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = "Timeline",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (sleepLogs.size >= 2) {
                        SleepScoreChart(logs = sleepLogs)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Log at least 2 nights of sleep to generate your clinical trend lines.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.styleTextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Insights Card
        if (sleepLogs.isNotEmpty()) {
            val latestSleep = sleepLogs.first()
            val latestLifestyle = lifestyleLogs.find { it.date == latestSleep.date }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassPanel(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Tips",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                               )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Last Night's Overview (${latestSleep.date})",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // Score and recovery Metrics
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            MetricBlock("Sleep Score", "${latestSleep.sleepScore}/100", Icons.Default.Star, MaterialTheme.colorScheme.tertiary)
                            MetricBlock("Duration", "${latestSleep.durationMinutes / 60}h ${latestSleep.durationMinutes % 60}m", Icons.Default.Schedule, MaterialTheme.colorScheme.primary)
                            MetricBlock("HRV Recovery", "${latestSleep.hrv} ms", Icons.Default.Favorite, Color(0xFFEF4444))
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sleep Stage Proportions (CBT-I optimized):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        SleepStagesRatioBar(latestSleep)

                        if (latestLifestyle != null) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))
                            Text(
                                text = "Associated Lifestyle Inputs for this date:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HabitChip("Caffeine", "${latestLifestyle.caffeineMg}mg", Icons.Default.Coffee, MaterialTheme.colorScheme.secondary)
                                HabitChip("Screen", "${latestLifestyle.screenTimeMinutes}m", Icons.Default.Smartphone, MaterialTheme.colorScheme.secondary)
                                HabitChip("Stress", "${latestLifestyle.stressLevel}/10", Icons.Default.SentimentDissatisfied, MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }

        // Sleep History list
        item {
            Text(
                "Sleep & Habits History Logs",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (sleepLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassPanel(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = "No data",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Your Sleep Log is currently empty.",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Use the 'Log Sleep' button to journal your nights.",
                                style = MaterialTheme.styleTextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            items(sleepLogs) { log ->
                val associatedHabits = lifestyleLogs.find { it.date == log.date }
                SleepLogItem(
                    log = log,
                    lifestyle = associatedHabits,
                    onDeleteSleep = { viewModel.deleteSleepLog(log) },
                    onDeleteHabits = { associatedHabits?.let { viewModel.deleteLifestyleLog(it) } }
                )
            }
        }
    }
}

// Custom Helper to handle secondary text color securely in Jetpack Compose
val MaterialTheme.styleTextSecondary: androidx.compose.ui.text.TextStyle
    @Composable
    get() = androidx.compose.ui.text.TextStyle(
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    )

@Composable
fun MetricBlock(label: String, value: String, icon: ImageVector, iconColor: Color) {
    Column(
        modifier = Modifier
            .glassPanel(RoundedCornerShape(16.dp))
            .padding(10.dp)
            .width(85.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 10.sp, style = MaterialTheme.styleTextSecondary, maxLines = 1)
    }
}

@Composable
fun HabitChip(label: String, value: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(vertical = 4.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("$label: ", fontSize = 11.sp, style = MaterialTheme.styleTextSecondary)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// Stacked Bar component representing sleep stages
@Composable
fun SleepStagesRatioBar(log: SleepLog) {
    val total = (log.deepSleepMinutes + log.remSleepMinutes + log.lightSleepMinutes + log.awakeMinutes).toFloat()
    if (total == 0f) return

    val deepRatio = log.deepSleepMinutes / total
    val remRatio = log.remSleepMinutes / total
    val lightRatio = log.lightSleepMinutes / total
    val awakeRatio = log.awakeMinutes / total

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(9.dp))
        ) {
            if (deepRatio > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(deepRatio)
                        .background(Color(0xFF3F51B5)), // deep blue
                    contentAlignment = Alignment.Center
                ) {}
            }
            if (remRatio > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(remRatio)
                        .background(Color(0xFF818CF8)), // REM light purple
                    contentAlignment = Alignment.Center
                ) {}
            }
            if (lightRatio > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(lightRatio)
                        .background(Color(0xFFC084FC)), // Light sleep violet
                    contentAlignment = Alignment.Center
                ) {}
            }
            if (awakeRatio > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(awakeRatio)
                        .background(Color(0xFFFDA4AF)), // Wake pastel pink/red
                    contentAlignment = Alignment.Center
                ) {}
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StageIndicator("Deep (${(deepRatio * 100).roundToInt()}%)", Color(0xFF3F51B5))
            StageIndicator("REM (${(remRatio * 100).roundToInt()}%)", Color(0xFF818CF8))
            StageIndicator("Light (${(lightRatio * 100).roundToInt()}%)", Color(0xFFC084FC))
            StageIndicator("Awake (${(awakeRatio * 100).roundToInt()}%)", Color(0xFFFDA4AF))
        }
    }
}

@Composable
fun StageIndicator(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

// SleepLogItem displaying history card with details
@Composable
fun SleepLogItem(
    log: SleepLog,
    lifestyle: LifestyleLog?,
    onDeleteSleep: () -> Unit,
    onDeleteHabits: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .glassPanel(RoundedCornerShape(16.dp))
            .testTag("sleep_log_item_${log.date}"),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = log.date,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sleep: ${log.durationMinutes / 60}h ${log.durationMinutes % 60}m  |  Latency: ${log.latencyMinutes}m  |  HRV: ${log.hrv}ms",
                        fontSize = 12.sp,
                        style = MaterialTheme.styleTextSecondary
                    )
                }

                // Score gauge badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = when {
                                log.sleepScore >= 85 -> Color(0xFF10B981).copy(alpha = 0.15f)
                                log.sleepScore >= 70 -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                else -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            },
                            shape = CircleShape
                        )
                        .border(
                            1.dp,
                            color = when {
                                log.sleepScore >= 85 -> Color(0xFF10B981)
                                log.sleepScore >= 70 -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${log.sleepScore}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = when {
                            log.sleepScore >= 85 -> Color(0xFF10B981)
                            log.sleepScore >= 70 -> Color(0xFFF1F5F9)
                            else -> Color(0xFFF87171)
                        }
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text("Stages Breakdowns:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    SleepStagesRatioBar(log)

                    if (log.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Personal Journal Notes:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "\"${log.notes}\"",
                            fontSize = 12.sp,
                            style = MaterialTheme.styleTextSecondary,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    if (lifestyle != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Lifestyle correlations found:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        Row(modifier = Modifier.padding(top = 4.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HabitChip("Caffeine", "${lifestyle.caffeineMg}mg", Icons.Default.Coffee, MaterialTheme.colorScheme.secondary)
                            HabitChip("Screen", "${lifestyle.screenTimeMinutes}m", Icons.Default.Smartphone, MaterialTheme.colorScheme.secondary)
                            HabitChip("Stress", "${lifestyle.stressLevel}/10", Icons.Default.SentimentDissatisfied, MaterialTheme.colorScheme.secondary)
                        }
                        if (lifestyle.notes.isNotBlank()) {
                            Text(
                                text = "Habits notes: \"${lifestyle.notes}\"",
                                fontSize = 11.sp,
                                style = MaterialTheme.styleTextSecondary,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No lifestyle habits logged for this date. (Log habits to review clinical correlations)",
                            fontSize = 11.sp,
                            style = MaterialTheme.styleTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDeleteSleep,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF87171)),
                            modifier = Modifier.testTag("delete_sleep_log_${log.date}")
                        ) {
                            Text("Delete Log", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// Custom Draw Chart sleep score lines representing past 5 entries
@Composable
fun SleepScoreChart(logs: List<SleepLog>) {
    val items = logs.take(7).reversed() // Chronological order
    val maxScore = 100f
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val width = size.width
        val height = size.height
        val padding = 16.dp.toPx()
        val chartWidth = width - (2 * padding)
        val chartHeight = height - (2 * padding)

        if (items.size >= 2) {
            val stepX = chartWidth / (items.size - 1)
            val points = items.mapIndexed { idx, log ->
                val x = padding + (idx * stepX)
                // Normalize points based on 0-100 score
                val normalizedValue = log.sleepScore.toFloat() / maxScore
                val y = padding + chartHeight - (normalizedValue * chartHeight)
                Offset(x, y)
            }

            // Draw grid lines
            val numGridLines = 3
            for (i in 0..numGridLines) {
                val gridY = padding + (chartHeight / numGridLines) * i
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(padding, gridY),
                    end = Offset(width - padding, gridY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw line gradient/shading path
            val fillPath = Path().apply {
                moveTo(points.first().x, padding + chartHeight)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, padding + chartHeight)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF818CF8).copy(alpha = 0.25f),
                        Color(0xFF818CF8).copy(alpha = 0.00f)
                    )
                )
            )

            // Draw structural connecting lines
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = Color(0xFF818CF8),
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw vertices indicator circles
            points.forEachIndexed { idx, pt ->
                drawCircle(
                    color = Color(0xFF141929),
                    radius = 5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = if (items[idx].sleepScore >= 85) Color(0xFF10B981) else if (items[idx].sleepScore >= 70) Color(0xFFFBBF24) else Color(0xFFF87171),
                    radius = 3.5.dp.toPx(),
                    center = pt,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }
    }
}


// --- COACH CHAT SCREEN COMPOSABLE ---
@Composable
fun CoachChatScreen(viewModel: MainViewModel, onNavigateToDashboard: () -> Unit) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val apiError by viewModel.apiError.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    var userText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to latest message on loading/arrival
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val coachingPills = listOf(
        "Check caffeine pattern ☕",
        "Suggest wind-down routine 🍵",
        "Explain sleep latency ⏱️",
        "Explain CBT-I stimulus control"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Scrollable message area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .glassPanel(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = "coaching info", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SleepCoach AI Guidelines", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = "We use CBT-I principles and sleep-stages analysis tailored to your logged habits to give action steps. We never diagnose sleep disorders.",
                            fontSize = 11.sp,
                            style = MaterialTheme.styleTextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Quick interaction Pills
            item {
                Text(
                    "Quick Coach Queries:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    coachingPills.take(2).forEach { pill ->
                        PillChip(text = pill, onClick = {
                            viewModel.sendPrompt(pill)
                        })
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    coachingPills.drop(2).forEach { pill ->
                        PillChip(text = pill, onClick = {
                            viewModel.sendPrompt(pill)
                        })
                    }
                }
            }

            items(messages) { msg ->
                ChatBubble(message = msg)
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "SleepCoach AI is studying your biomarkers...",
                            fontSize = 12.sp,
                            style = MaterialTheme.styleTextSecondary
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F1113).copy(alpha = 0.85f))
                .drawBehind {
                    drawLine(
                        color = Color.White.copy(alpha = 0.12f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = userText,
                        onValueChange = { userText = it },
                        placeholder = { Text("Ask SleepCoach AI about caffeine, routines, CBT-I...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text")
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = com.example.ui.theme.GlassPrimary
                        ),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (userText.isNotBlank()) {
                                viewModel.sendPrompt(userText)
                                userText = ""
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("send_chat_button"),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send prompt",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PillChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .glassPanel(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .testTag("pill_$text")
    ) {
        Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val textState = message.text
    // Regex cleanups to support basic markdown rendering of **bold** text in Bubble
    val annotatedParts = remember(textState) { parseMarkdownBold(textState) }

    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val textColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface
    val shape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = if (message.isUser) {
                Modifier
                    .widthIn(max = 290.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                com.example.ui.theme.GlassPrimary,
                                Color(0xFF6366F1)
                            )
                        ),
                        shape
                    )
                    .padding(12.dp)
            } else {
                Modifier
                    .widthIn(max = 290.dp)
                    .glassPanel(shape)
                    .padding(12.dp)
            }
        ) {
            Column {
                if (!message.isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Expert",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "SleepCoach AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Content with custom formatting to render markdown bold blocks (using AnnotatedString logic simplified)
                Text(
                    text = annotatedParts,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = textColor
                )
            }
        }
    }
}

// Simple Helper to do static parsing of markdown bold in Bubble
private fun parseMarkdownBold(input: String): androidx.compose.ui.text.AnnotatedString {
    val builder = androidx.compose.ui.text.AnnotatedString.Builder()
    var index = 0
    val size = input.length

    while (index < size) {
        val nextBold = input.indexOf("**", index)
        if (nextBold == -1) {
            builder.append(input.substring(index))
            break
        }
        
        builder.append(input.substring(index, nextBold))
        val endBold = input.indexOf("**", nextBold + 2)
        if (endBold == -1) {
            builder.append(input.substring(nextBold))
            break
        }
        
        builder.pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
        builder.append(input.substring(nextBold + 2, endBold))
        builder.pop()
        index = endBold + 2
    }
    return builder.toAnnotatedString()
}


// --- POPUP RECORDING DIALOGS ---

@Composable
fun LogSleepDialog(
    onDismiss: () -> Unit,
    onSave: (date: String, score: Int, duration: Int, deep: Int, rem: Int, light: Int, awake: Int, hrv: Int, latency: Int, notes: String) -> Unit
) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    var dateText by remember { mutableStateOf(sdf.format(Date())) }
    
    var scoreValue by remember { mutableFloatStateOf(80f) }
    var durationHrs by remember { mutableStateOf("7") }
    var durationMins by remember { mutableStateOf("30") }
    
    var deepMins by remember { mutableStateOf("60") }
    var remMins by remember { mutableStateOf("60") }
    var lightMins by remember { mutableStateOf("290") }
    var awakeMins by remember { mutableStateOf("40") }
    
    var hrvMs by remember { mutableStateOf("45") }
    var latencyMinutes by remember { mutableStateOf("15") }
    var notesText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .glassPanel(RoundedCornerShape(24.dp))
                .testTag("log_sleep_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Log Night Sleep",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Enter sleep details & Stages in minutes.", fontSize = 11.sp, style = MaterialTheme.styleTextSecondary)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))
                }

                // Date
                item {
                    TextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("add_sleep_date"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                // Sleep Score Slider
                item {
                    Text("Overall Sleep Quality Score: ${scoreValue.roundToInt()}/100", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = scoreValue,
                        onValueChange = { scoreValue = it },
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("add_sleep_score_slider")
                    )
                }

                // Duration Row
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = durationHrs,
                            onValueChange = { durationHrs = it },
                            label = { Text("Duration Hours") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("add_sleep_hour"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        TextField(
                            value = durationMins,
                            onValueChange = { durationMins = it },
                            label = { Text("Mins") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("add_sleep_min"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                item {
                    Text("Sleep Stages Breakdowns (Mins):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = deepMins,
                            onValueChange = { deepMins = it },
                            label = { Text("Deep Mins") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("add_sleep_deep"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        TextField(
                            value = remMins,
                            onValueChange = { remMins = it },
                            label = { Text("REM Mins") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("add_sleep_rem"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = lightMins,
                            onValueChange = { lightMins = it },
                            label = { Text("Light Mins") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("add_sleep_light"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        TextField(
                            value = awakeMins,
                            onValueChange = { awakeMins = it },
                            label = { Text("Awake Mins") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("add_sleep_awake"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                // Recovery / Latency Row
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = hrvMs,
                            onValueChange = { hrvMs = it },
                            label = { Text("HRV (ms)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("add_sleep_hrv"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        TextField(
                            value = latencyMinutes,
                            onValueChange = { latencyMinutes = it },
                            label = { Text("Latency (mins)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("add_sleep_latency"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                item {
                    TextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Notes / Dream journal") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("add_sleep_notes"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val dHrs = durationHrs.toIntOrNull() ?: 0
                                val dMins = durationMins.toIntOrNull() ?: 0
                                val totalDuration = (dHrs * 60) + dMins

                                val dpM = deepMins.toIntOrNull() ?: 0
                                val remM = remMins.toIntOrNull() ?: 0
                                val lM = lightMins.toIntOrNull() ?: 0
                                val awM = awakeMins.toIntOrNull() ?: 0

                                val hVal = hrvMs.toIntOrNull() ?: 45
                                val latVal = latencyMinutes.toIntOrNull() ?: 15

                                if (totalDuration <= 0) {
                                    Toast.makeText(context, "Please enter a valid duration", Toast.LENGTH_SHORT).show()
                                } else {
                                    onSave(
                                        dateText,
                                        scoreValue.roundToInt(),
                                        totalDuration,
                                        dpM,
                                        remM,
                                        lM,
                                        awM,
                                        hVal,
                                        latVal,
                                        notesText
                                    )
                                }
                            },
                            modifier = Modifier.testTag("save_sleep_log_button")
                        ) {
                            Text("Save Report")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogLifestyleDialog(
    onDismiss: () -> Unit,
    onSave: (date: String, caffeine: Int, alcohol: Int, exercise: Int, screenTime: Int, stress: Int, notes: String) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    var dateText by remember { mutableStateOf(sdf.format(Date())) }

    var caffeineMg by remember { mutableFloatStateOf(80f) }
    var alcoholUnits by remember { mutableFloatStateOf(0f) }
    
    var exerciseMins by remember { mutableStateOf("30") }
    var screenTimeMinutes by remember { mutableStateOf("45") }
    var stressLevel by remember { mutableFloatStateOf(4f) }
    
    var notesText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .glassPanel(RoundedCornerShape(24.dp))
                .testTag("log_lifestyle_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Log Daily Habits",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Record caffeine, screen use, and stress.", fontSize = 11.sp, style = MaterialTheme.styleTextSecondary)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))
                }

                // Date
                item {
                    TextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("add_habits_date"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                // Caffeine mg Slider
                item {
                    Text("Caffeine (coffee/soda/tea): ${caffeineMg.roundToInt()} mg", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = caffeineMg,
                        onValueChange = { caffeineMg = it },
                        valueRange = 0f..400f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.testTag("add_habits_caffeine_slider")
                    )
                }

                // Alcohol units Slider
                item {
                    Text("Alcohol Intake: ${alcoholUnits.roundToInt()} Units", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = alcoholUnits,
                        onValueChange = { alcoholUnits = it },
                        valueRange = 0f..10f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.testTag("add_habits_alcohol_slider")
                    )
                }

                // Exercise & Screen Time Text Fields
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = exerciseMins,
                            onValueChange = { exerciseMins = it },
                            label = { Text("Exercise (Mins)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("add_habits_exercise"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        TextField(
                            value = screenTimeMinutes,
                            onValueChange = { screenTimeMinutes = it },
                            label = { Text("Bed Screen (Mins)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("add_habits_screen"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                // Stress level slider
                item {
                    Text("Daytime Stress Level: ${stressLevel.roundToInt()}/10", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = stressLevel,
                        onValueChange = { stressLevel = it },
                        valueRange = 1f..10f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.testTag("add_habits_stress_slider")
                    )
                }

                item {
                    TextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Notes / Context") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("add_habits_notes"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val exeVal = exerciseMins.toIntOrNull() ?: 0
                                val scrVal = screenTimeMinutes.toIntOrNull() ?: 0
                                onSave(
                                    dateText,
                                    caffeineMg.roundToInt(),
                                    alcoholUnits.roundToInt(),
                                    exeVal,
                                    scrVal,
                                    stressLevel.roundToInt(),
                                    notesText
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary),
                            modifier = Modifier.testTag("save_habits_log_button")
                        ) {
                            Text("Save Habits")
                        }
                    }
                }
            }
        }
    }
}
