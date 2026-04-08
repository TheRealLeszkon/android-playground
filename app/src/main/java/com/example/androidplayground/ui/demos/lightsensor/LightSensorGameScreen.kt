package com.example.androidplayground.ui.demos.lightsensor

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

// ── Color palettes per time of day ──

private val DimGradient = listOf(Color(0xFFFFF8E1), Color(0xFFFFE0B2), Color(0xFFFFCC80))
private val BrightGradient = listOf(Color(0xFFFFF9C4), Color(0xFFFFEB3B), Color(0xFFFFC107))
private val NightGradient = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))

private val DimContent = Color(0xFF5D4037)
private val BrightContent = Color(0xFF4E342E)
private val NightContent = Color(0xFFE0E0E0)

private val DimSubtitle = Color(0xFF8D6E63)
private val BrightSubtitle = Color(0xFF795548)
private val NightSubtitle = Color(0xFF90A4AE)

private val AccentGreen = Color(0xFF3CDA84)
private val AccentGreenDark = Color(0xFF006D3B)
private val SubtitleColor = Color(0xFF49454F)
private val ScreenBg = Color(0xFFF9F9F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightSensorGameScreen(
    navController: NavController,
    viewModel: LightSensorGameViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var showStats by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        viewModel.startSensor(context)
        onDispose { viewModel.stopSensor() }
    }

    // If sensor is not available, show a simple fallback
    if (!state.sensorAvailable) {
        SensorUnavailableScreen(navController)
        return
    }

    // Top bar colors change based on time of day
    val topBarColor = when (state.timeOfDay) {
        TimeOfDay.DIM -> DimGradient.first()
        TimeOfDay.BRIGHT -> BrightGradient.first()
        TimeOfDay.NIGHT -> NightGradient.first()
    }
    val topBarContent = when (state.timeOfDay) {
        TimeOfDay.DIM -> DimContent
        TimeOfDay.BRIGHT -> BrightContent
        TimeOfDay.NIGHT -> NightContent
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Light Sensor", fontWeight = FontWeight.Bold, color = topBarContent) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = topBarContent)
                    }
                },
                actions = {
                    IconButton(onClick = { showStats = true }) {
                        Icon(Icons.Filled.BarChart, contentDescription = "Stats", tint = topBarContent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarColor)
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = state.timeOfDay,
            transitionSpec = {
                (fadeIn(animationSpec = tween(400)) + slideInVertically(
                    animationSpec = tween(400),
                    initialOffsetY = { it / 8 }
                )) togetherWith (fadeOut(animationSpec = tween(300)) + slideOutVertically(
                    animationSpec = tween(300),
                    targetOffsetY = { -it / 8 }
                ))
            },
            label = "timeOfDay"
        ) { tod ->
            val gradient = when (tod) {
                TimeOfDay.DIM -> DimGradient
                TimeOfDay.BRIGHT -> BrightGradient
                TimeOfDay.NIGHT -> NightGradient
            }
            val contentColor = when (tod) {
                TimeOfDay.DIM -> DimContent
                TimeOfDay.BRIGHT -> BrightContent
                TimeOfDay.NIGHT -> NightContent
            }
            val subtitleColor = when (tod) {
                TimeOfDay.DIM -> DimSubtitle
                TimeOfDay.BRIGHT -> BrightSubtitle
                TimeOfDay.NIGHT -> NightSubtitle
            }
            val icon = when (tod) {
                TimeOfDay.DIM -> Icons.Filled.WbTwilight
                TimeOfDay.BRIGHT -> Icons.Filled.WbSunny
                TimeOfDay.NIGHT -> Icons.Filled.DarkMode
            }
            val title = when (tod) {
                TimeOfDay.DIM -> "Dim"
                TimeOfDay.BRIGHT -> "Bright"
                TimeOfDay.NIGHT -> "Night"
            }
            val subtitle = when (tod) {
                TimeOfDay.DIM -> "Soft ambient light\nModerate brightness detected"
                TimeOfDay.BRIGHT -> "Bright and vivid\nHigh ambient light detected"
                TimeOfDay.NIGHT -> "Stars are out\nLow light detected"
            }

            TimeOfDayContent(
                gradient = gradient,
                icon = icon,
                title = title,
                subtitle = subtitle,
                contentColor = contentColor,
                subtitleColor = subtitleColor,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    // Stats bottom sheet
    if (showStats) {
        StatsBottomSheet(
            lux = state.lux,
            smoothedLux = state.smoothedLux,
            timeOfDay = state.timeOfDay,
            onDismiss = { showStats = false }
        )
    }
}

// ── Time-of-day full-screen content ──

@Composable
private fun TimeOfDayContent(
    gradient: List<Color>,
    icon: ImageVector,
    title: String,
    subtitle: String,
    contentColor: Color,
    subtitleColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Large icon
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor.copy(alpha = 0.2f),
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = subtitleColor,
                    lineHeight = 24.sp
                ),
                modifier = Modifier.padding(horizontal = 48.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ── Stats Bottom Sheet ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsBottomSheet(
    lux: Float,
    smoothedLux: Float,
    timeOfDay: TimeOfDay,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                "SENSOR STATS",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = AccentGreenDark,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            StatRow("Raw Lux", String.format("%.1f lx", lux))
            Spacer(modifier = Modifier.height(12.dp))
            StatRow("Smoothed Lux", String.format("%.1f lx", smoothedLux))
            Spacer(modifier = Modifier.height(12.dp))
            StatRow("Detected State", timeOfDay.name.lowercase().replaceFirstChar { it.uppercase() })
            Spacer(modifier = Modifier.height(12.dp))

            // Range indicator
            val rangeLabel = when (timeOfDay) {
                TimeOfDay.NIGHT -> "0 – 50 lux"
                TimeOfDay.DIM -> "50 – 500 lux"
                TimeOfDay.BRIGHT -> "500+ lux"
            }
            StatRow("Expected Range", rangeLabel)

            Spacer(modifier = Modifier.height(20.dp))

            // Lux bar visualization
            LuxBar(smoothedLux)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SubtitleColor,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AccentGreenDark,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun LuxBar(lux: Float) {
    // Simple visual bar: 0–1000+ lux mapped to 0–100% width
    val fraction = (lux / 1000f).coerceIn(0f, 1f)

    Column {
        Text(
            "LUX LEVEL",
            style = MaterialTheme.typography.labelSmall.copy(
                color = SubtitleColor,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE0E0E0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(AccentGreen, Color(0xFFFFEB3B), Color(0xFFFF9800))
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0", style = MaterialTheme.typography.labelSmall.copy(color = SubtitleColor))
            Text("Night", style = MaterialTheme.typography.labelSmall.copy(color = SubtitleColor))
            Text("Dim", style = MaterialTheme.typography.labelSmall.copy(color = SubtitleColor))
            Text("Bright", style = MaterialTheme.typography.labelSmall.copy(color = SubtitleColor))
            Text("1000+", style = MaterialTheme.typography.labelSmall.copy(color = SubtitleColor))
        }
    }
}

// ── Sensor unavailable fallback ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SensorUnavailableScreen(navController: NavController) {
    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Light Sensor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBg)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.WbSunny,
                    contentDescription = null,
                    tint = SubtitleColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Light sensor not available",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SubtitleColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This device doesn't have a light sensor",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SubtitleColor.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}
