package com.example.androidplayground.ui.demos.safecracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// ── Design tokens ──

private val AccentGreen = Color(0xFF3CDA84)
private val AccentGreenDark = Color(0xFF006D3B)
private val ScreenBg = Color(0xFFF9F9F9)
private val CardBg = Color(0xFFFFFFFF)
private val DarkSurface = Color(0xFF1C1B1F)
private val SubtitleColor = Color(0xFF49454F)
private val DialRing = Color(0xFF2B2930)
private val NotchColor = Color(0xFF555555)
private val PointerColor = Color(0xFFEF5350)

// ── Main screen ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeCrackerScreen(
    navController: NavController,
    viewModel: SafeCrackerViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val textMeasurer = rememberTextMeasurer()

    var lastProximity by remember { mutableIntStateOf(0) }

    // Continuous proximity haptics — haptics are the PRIMARY feedback
    LaunchedEffect(state.dialValue, state.step) {
        while (true) {
            val prox = viewModel.getProximity()
            when (prox) {
                Proximity.EXACT -> viewModel.playExactHaptic(context)
                Proximity.NEAR -> viewModel.playNearHaptic(context)
                Proximity.FAR -> {}
            }
            delay(if (prox == Proximity.EXACT) 250L else 450L)
        }
    }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Safe Cracker", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.phase == GamePhase.WON) {
                        IconButton(onClick = { viewModel.resetGame() }) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "New Game",
                                tint = AccentGreen
                            )
                        }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            StepIndicator(step = state.step, phase = state.phase)

            Spacer(modifier = Modifier.height(8.dp))

            DirectionHint(state = state)

            Spacer(modifier = Modifier.height(16.dp))

            // Dial — no visual proximity cues, haptics only
            DialCanvas(
                state = state,
                textMeasurer = textMeasurer,
                onDragStart = { x, y, cx, cy ->
                    viewModel.onDragStart(x, y, cx, cy)
                },
                onDrag = { x, y, cx, cy ->
                    viewModel.onDialDrag(x, y, cx, cy)
                    if (viewModel.hasNotchChanged()) {
                        viewModel.playTickHaptic(context)
                    }
                },
                onDragEnd = {
                    viewModel.onDragEnd()
                },
                onLockClick = {
                    val result = viewModel.tryLock()
                    when (result) {
                        LockResult.CORRECT -> viewModel.playLockCorrectHaptic(context)
                        LockResult.WIN -> viewModel.playWinHaptic(context)
                        else -> {}
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ValueDisplay(state = state)

            Spacer(modifier = Modifier.height(12.dp))

            if (state.lockMessage.isNotEmpty()) {
                StatusMessage(state = state)
            }

            AnimatedVisibility(
                visible = state.phase == GamePhase.WON,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                WinCard(onNewGame = { viewModel.resetGame() })
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Step indicator ──

@Composable
private fun StepIndicator(step: Int, phase: GamePhase) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SAFE CRACKER",
            style = MaterialTheme.typography.labelMedium.copy(
                color = AccentGreenDark,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { i ->
                val done = i < step
                val active = i == step && phase != GamePhase.WON
                val dotColor by animateColorAsState(
                    targetValue = when {
                        done -> AccentGreen
                        active -> AccentGreen.copy(alpha = 0.4f)
                        else -> Color(0xFFDDDDDD)
                    },
                    animationSpec = tween(300),
                    label = "dot$i"
                )
                Box(
                    modifier = Modifier
                        .size(if (active) 28.dp else 10.dp, 10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(dotColor)
                )
            }
        }
    }
}

// ── Direction hint ──

@Composable
private fun DirectionHint(state: SafeCrackerState) {
    if (state.phase == GamePhase.WON) return

    val dirLabel = when (state.requiredDirection) {
        RotationDir.RIGHT -> "→ Turn CLOCKWISE"
        RotationDir.LEFT -> "← Turn COUNTER-CLOCKWISE"
        RotationDir.NONE -> ""
    }

    Text(
        text = "Step ${state.step + 1} / 3  •  $dirLabel",
        style = MaterialTheme.typography.bodySmall.copy(
            color = SubtitleColor,
            letterSpacing = 0.5.sp
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

// ── Canvas dial — pure mechanical, no color hints ──

@Composable
private fun DialCanvas(
    state: SafeCrackerState,
    textMeasurer: TextMeasurer,
    onDragStart: (Float, Float, Float, Float) -> Unit,
    onDrag: (Float, Float, Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    onLockClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(DarkSurface)
            .pointerInput(state.phase) {
                if (state.phase == GamePhase.WON) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        onDragStart(offset.x, offset.y, cx, cy)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        onDrag(change.position.x, change.position.y, cx, cy)
                    },
                    onDragEnd = {
                        onDragEnd()
                    },
                    onDragCancel = {
                        onDragEnd()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerR = size.minDimension * 0.44f
            val innerR = outerR * 0.72f

            // The accumulated rotation drives the entire dial face
            val rotationRad = Math.toRadians(state.dialRotation.toDouble())

            // Outer ring
            drawCircle(
                color = DialRing,
                radius = outerR,
                center = Offset(cx, cy),
                style = Stroke(width = 6f)
            )

            // Inner ring
            drawCircle(
                color = DialRing.copy(alpha = 0.5f),
                radius = innerR,
                center = Offset(cx, cy),
                style = Stroke(width = 3f)
            )

            // Notches — rotate with the dial
            for (i in 0 until 90) {
                val baseAngle = Math.toRadians((i * 4).toDouble())
                val notchAngle = baseAngle + rotationRad
                val isMajor = i % 10 == 0
                val isMinor5 = i % 5 == 0
                val startR = if (isMajor) outerR - 20f
                    else if (isMinor5) outerR - 14f
                    else outerR - 9f
                val endR = outerR - 3f

                val startX = cx + startR * cos(notchAngle).toFloat()
                val startY = cy + startR * sin(notchAngle).toFloat()
                val endX = cx + endR * cos(notchAngle).toFloat()
                val endY = cy + endR * sin(notchAngle).toFloat()

                drawLine(
                    color = if (isMajor) Color.White.copy(alpha = 0.7f)
                        else NotchColor.copy(alpha = 0.5f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 2.5f else 1.2f,
                    cap = StrokeCap.Round
                )
            }

            // Number labels — rotate with the dial
            drawDialNumbers(cx, cy, outerR, rotationRad, textMeasurer)

            // Fixed pointer at 12 o'clock (does NOT rotate)
            drawPointer(cx, cy, outerR)

            // Small indicator dot on the dial face (rotates with dial)
            val indicatorAngle = rotationRad  // 0° of the dial rotated by accumulated angle
            val indR = innerR + (outerR - innerR) * 0.5f
            val indX = cx + indR * cos(indicatorAngle).toFloat()
            val indY = cy + indR * sin(indicatorAngle).toFloat()
            drawCircle(
                color = AccentGreen.copy(alpha = 0.7f),
                radius = 5f,
                center = Offset(indX, indY)
            )
        }

        // Center lock button
        LockButton(
            enabled = state.phase != GamePhase.WON,
            onClick = onLockClick
        )
    }
}

private fun DrawScope.drawPointer(cx: Float, cy: Float, outerR: Float) {
    val tipY = cy - outerR + 8f
    val baseY = cy - outerR - 16f
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(cx, tipY)
        lineTo(cx - 8f, baseY)
        lineTo(cx + 8f, baseY)
        close()
    }
    drawPath(path, color = PointerColor)

    drawLine(
        color = PointerColor,
        start = Offset(cx, baseY - 2f),
        end = Offset(cx, baseY - 10f),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawDialNumbers(
    cx: Float, cy: Float, outerR: Float, rotationRad: Double, textMeasurer: TextMeasurer
) {
    val labelR = outerR - 34f
    for (i in 0..8) {
        val value = i * 10
        val baseAngle = Math.toRadians((value * 4).toDouble())
        val angle = baseAngle + rotationRad

        val lx = cx + labelR * cos(angle).toFloat()
        val ly = cy + labelR * sin(angle).toFloat()

        val text = "$value"
        val style = TextStyle(
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        val result = textMeasurer.measure(text, style)
        drawText(
            result,
            topLeft = Offset(lx - result.size.width / 2f, ly - result.size.height / 2f)
        )
    }
}

// ── Lock button ──

@Composable
private fun LockButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        modifier = Modifier
            .size(80.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = if (enabled) listOf(AccentGreenDark, AccentGreen)
                    else listOf(DialRing, DialRing)
                ),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = if (enabled) Icons.Filled.Lock else Icons.Filled.LockOpen,
            contentDescription = "Lock",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

// ── Value display ──

@Composable
private fun ValueDisplay(state: SafeCrackerState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DIAL VALUE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = AccentGreenDark,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (state.currentDirection) {
                        RotationDir.RIGHT -> "Turning clockwise →"
                        RotationDir.LEFT -> "← Turning counter-clockwise"
                        RotationDir.NONE -> "Waiting..."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor)
                )
            }
            Text(
                text = "${state.dialValue}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen
                )
            )
        }
    }
}

// ── Status message ──

@Composable
private fun StatusMessage(state: SafeCrackerState) {
    val isError = state.lockMessage.contains("Wrong") || state.lockMessage.contains("Not close")
    val isSuccess = state.lockMessage.contains("unlocked") || state.lockMessage.contains("Cracked")

    val bgColor by animateColorAsState(
        targetValue = when {
            isSuccess -> AccentGreen.copy(alpha = 0.1f)
            isError -> PointerColor.copy(alpha = 0.1f)
            else -> CardBg
        },
        animationSpec = tween(200),
        label = "msgBg"
    )
    val textColor = when {
        isSuccess -> AccentGreenDark
        isError -> PointerColor
        else -> SubtitleColor
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = state.lockMessage,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

// ── Win card ──

@Composable
private fun WinCard(onNewGame: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.LockOpen,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Safe Cracked!",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "All 3 pins unlocked",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.5f)
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNewGame,
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("New Game", fontWeight = FontWeight.SemiBold)
        }
    }
}
