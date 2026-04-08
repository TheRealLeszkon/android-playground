package com.example.androidplayground.ui.demos.tiltpong

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay

private val AccentGreen = Color(0xFF3CDA84)
private val AccentGreenDark = Color(0xFF006D3B)
private val ScreenBg = Color(0xFFF9F9F9)
private val CanvasBg = Color(0xFF1C1B1F)
private val ActivePaddleColor = AccentGreen
private val InactivePaddleColor = Color(0xFF49454F)
private val BallColor = Color.White
private val CourtLine = Color(0xFF2B2930)
private val SubtitleColor = Color(0xFF49454F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiltPongScreen(
    navController: NavController,
    viewModel: TiltPongViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val tiltX by viewModel.tiltX.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var showStats by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        viewModel.startSensor(context)
        onDispose { viewModel.stopSensor() }
    }

    // Game loop (~60 fps)
    LaunchedEffect(state.isGameOver) {
        while (!state.isGameOver) {
            viewModel.tick()
            delay(16L)
        }
    }

    // Haptic feedback on collisions
    LaunchedEffect(state.wallHit, state.paddleHit, state.controlSwitched) {
        if (state.wallHit) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        if (state.paddleHit) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Tilt Pong", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showStats = !showStats }) {
                        Icon(
                            Icons.Filled.BarChart,
                            contentDescription = if (showStats) "Hide Stats" else "Show Stats",
                            tint = if (showStats) AccentGreen else SubtitleColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score + active side indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SCORE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                            color = SubtitleColor
                        )
                    )
                    val sideLabel = when (state.playerSide) {
                        PlayerSide.TOP -> "↑ Top"
                        PlayerSide.BOTTOM -> "↓ Bottom"
                    }
                    Text(
                        text = sideLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentGreenDark,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
                Text(
                    text = "${state.score}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen
                    )
                )
            }

            AnimatedVisibility(
                visible = showStats,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        StatRow("Tilt X", String.format("%.3f", tiltX))
                        StatRow("Top Pad", String.format("%.3f", state.topPaddleX))
                        StatRow("Bot Pad", String.format("%.3f", state.bottomPaddleX))
                        StatRow("Ball", String.format("%.3f, %.3f", state.ballX, state.ballY))
                        StatRow("Velocity", String.format("%.4f, %.4f", state.ballVx, state.ballVy))
                        StatRow("Control", state.playerSide.name)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(CanvasBg, RoundedCornerShape(20.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Court horizontal centre line
                    drawLine(
                        color = CourtLine,
                        start = Offset(0f, h * 0.5f),
                        end = Offset(w, h * 0.5f),
                        strokeWidth = 2f
                    )

                    // Centre circle
                    drawCircle(
                        color = CourtLine,
                        radius = 60f,
                        center = Offset(w * 0.5f, h * 0.5f),
                        style = Stroke(width = 2f)
                    )

                    // Top paddle — horizontal bar
                    val topActive = state.playerSide == PlayerSide.TOP
                    val topPw = state.paddleWidth * w
                    val topPh = state.paddleThickness * h
                    val topPx = state.topPaddleX * w - topPw / 2
                    val topPy = state.topPaddleY * h - topPh / 2

                    drawRoundRect(
                        color = if (topActive) ActivePaddleColor else InactivePaddleColor,
                        topLeft = Offset(topPx, topPy),
                        size = Size(topPw, topPh),
                        cornerRadius = CornerRadius(topPh / 2, topPh / 2)
                    )

                    // Bottom paddle — horizontal bar
                    val bottomActive = state.playerSide == PlayerSide.BOTTOM
                    val botPw = state.paddleWidth * w
                    val botPh = state.paddleThickness * h
                    val botPx = state.bottomPaddleX * w - botPw / 2
                    val botPy = state.bottomPaddleY * h - botPh / 2

                    drawRoundRect(
                        color = if (bottomActive) ActivePaddleColor else InactivePaddleColor,
                        topLeft = Offset(botPx, botPy),
                        size = Size(botPw, botPh),
                        cornerRadius = CornerRadius(botPh / 2, botPh / 2)
                    )

                    // Subtle glow behind active paddle
                    val activeX = if (topActive) state.topPaddleX else state.bottomPaddleX
                    val activeY = if (topActive) state.topPaddleY else state.bottomPaddleY
                    drawCircle(
                        color = AccentGreen.copy(alpha = 0.06f),
                        radius = state.paddleWidth * w * 0.35f,
                        center = Offset(activeX * w, activeY * h)
                    )

                    // Ball
                    drawCircle(
                        color = BallColor,
                        radius = state.ballRadius * minOf(w, h),
                        center = Offset(state.ballX * w, state.ballY * h)
                    )
                }

                // Game over overlay
                if (state.isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Game Over",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Score: ${state.score}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = AccentGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.restart() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Play Again", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tilt your device to move the active paddle",
                style = MaterialTheme.typography.bodyMedium.copy(color = SubtitleColor),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = SubtitleColor,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = AccentGreenDark
            )
        )
    }
}
