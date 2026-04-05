package com.example.androidplayground.ui.demos.orientation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

private val AccentGreen = Color(0xFF3CDA84)
private val AccentGreenDark = Color(0xFF006D3B)
private val ScreenBg = Color(0xFFF9F9F9)
private val CardBg = Color(0xFFFFFFFF)
private val SubtitleColor = Color(0xFF49454F)
private val IndicatorActive = Color(0xFF3CDA84)
private val DarkSurface = Color(0xFF1C1B1F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceOrientationScreen(
    navController: NavController,
    viewModel: DeviceOrientationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        viewModel.startSensor(context)
        onDispose { viewModel.stopSensor() }
    }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Device Orientation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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

            // Sensor mode toggle
            SensorModeToggle(
                isGyroscope = state.sensorMode == SensorMode.GYROSCOPE,
                onToggle = { viewModel.toggleSensorMode() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Visual orientation indicator — fixed height so it's always fully visible
            OrientationIndicator(
                position = state.position,
                isGyroscope = state.sensorMode == SensorMode.GYROSCOPE
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Inline collapsible dashboard panel
            InlineDashboardPanel(
                state = state,
                onToggle = { viewModel.toggleDashboard() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Sensor Mode Toggle ──

@Composable
private fun SensorModeToggle(
    isGyroscope: Boolean,
    onToggle: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isGyroscope) "GYROSCOPE" else "ACCELEROMETER",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = AccentGreenDark,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGyroscope) "Detects movement & rotation (angular velocity)"
                    else "Detects position & orientation (gravity)",
                    style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = isGyroscope,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = AccentGreen,
                    checkedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFDDDDDD),
                    uncheckedThumbColor = Color.White
                )
            )
        }
    }
}

// ── Orientation Indicator ──

@Composable
private fun OrientationIndicator(
    position: OrientationPosition,
    isGyroscope: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(DarkSurface),
        contentAlignment = Alignment.Center
    ) {
        if (!isGyroscope) {
            AccelerometerVisual(position)
        } else {
            GyroscopeVisual(position)
        }
    }
}

@Composable
private fun AccelerometerVisual(position: OrientationPosition) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        DirectionBox(
            label = "TOP",
            isActive = position == OrientationPosition.TOP,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        )

        DirectionBox(
            label = "BOTTOM",
            isActive = position == OrientationPosition.BOTTOM,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )

        DirectionBox(
            label = "LEFT",
            isActive = position == OrientationPosition.LEFT,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
        )

        DirectionBox(
            label = "RIGHT",
            isActive = position == OrientationPosition.RIGHT,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
        )

        CenterLabel(position)
    }
}

@Composable
private fun GyroscopeVisual(position: OrientationPosition) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        DirectionBox(
            label = "TILT\nTOP UP",
            isActive = position == OrientationPosition.TILT_TOP_UP,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        )

        DirectionBox(
            label = "TILT\nBOTTOM UP",
            isActive = position == OrientationPosition.TILT_BOTTOM_UP,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )

        DirectionBox(
            label = "TURN\nLEFT",
            isActive = position == OrientationPosition.TURN_LEFT,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )

        DirectionBox(
            label = "TURN\nRIGHT",
            isActive = position == OrientationPosition.TURN_RIGHT,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        )

        CenterLabel(position)
    }
}

@Composable
private fun DirectionBox(
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) IndicatorActive else Color.White.copy(alpha = 0.08f),
        animationSpec = tween(durationMillis = 250),
        label = "dirBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isActive) DarkSurface else Color.White.copy(alpha = 0.45f),
        animationSpec = tween(durationMillis = 250),
        label = "dirText"
    )
    val boxScale by animateFloatAsState(
        targetValue = if (isActive) 1.08f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "dirScale"
    )

    Box(
        modifier = modifier
            .scale(boxScale)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        )
    }
}

@Composable
private fun CenterLabel(position: OrientationPosition) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val isNeutral = position == OrientationPosition.FLAT || position == OrientationPosition.STILL
        val dotScale by animateFloatAsState(
            targetValue = if (isNeutral) 1f else 1.4f,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "dotPulse"
        )
        val dotColor by animateColorAsState(
            targetValue = if (isNeutral) AccentGreen.copy(alpha = 0.35f) else AccentGreen,
            animationSpec = tween(300),
            label = "dotColor"
        )

        Box(
            modifier = Modifier
                .size(12.dp)
                .scale(dotScale)
                .clip(CircleShape)
                .background(dotColor)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = position.label,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Detected Position",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.35f),
                letterSpacing = 1.sp
            )
        )
    }
}

// ── Inline Dashboard Panel ──

@Composable
private fun InlineDashboardPanel(
    state: OrientationState,
    onToggle: () -> Unit
) {
    var showInfo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
    ) {
        // Header — always visible, acts as expand/collapse toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DASHBOARD",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = AccentGreenDark,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (state.sensorMode == SensorMode.ACCELEROMETER) "Accelerometer" else "Gyroscope",
                    style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Live position chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(IndicatorActive.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = state.position.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AccentGreenDark
                        )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (state.dashboardExpanded)
                        Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (state.dashboardExpanded) "Collapse" else "Expand",
                    tint = SubtitleColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Expandable content
        AnimatedVisibility(
            visible = state.dashboardExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
            ) {
                // Divider via tonal shift
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFEEEEEE))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Live X, Y, Z values
                SensorValueRow(
                    axis = "X",
                    value = state.sensorX,
                    description = if (state.sensorMode == SensorMode.ACCELEROMETER)
                        "Left / Right tilt" else "Forward / Back rotation"
                )
                Spacer(modifier = Modifier.height(12.dp))
                SensorValueRow(
                    axis = "Y",
                    value = state.sensorY,
                    description = if (state.sensorMode == SensorMode.ACCELEROMETER)
                        "Up / Down tilt" else "Left / Right turn"
                )
                Spacer(modifier = Modifier.height(12.dp))
                SensorValueRow(
                    axis = "Z",
                    value = state.sensorZ,
                    description = if (state.sensorMode == SensorMode.ACCELEROMETER)
                        "Face up / Face down" else "Twist (knob rotation)"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Info toggle row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (showInfo) AccentGreen.copy(alpha = 0.08f)
                            else Color(0xFFF5F5F5)
                        )
                        .clickable { showInfo = !showInfo }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "Info",
                            tint = if (showInfo) AccentGreen else SubtitleColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "How does this sensor work?",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = if (showInfo) AccentGreenDark else SubtitleColor
                            )
                        )
                    }
                    Icon(
                        imageVector = if (showInfo)
                            Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = SubtitleColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Info content
                AnimatedVisibility(
                    visible = showInfo,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    if (state.sensorMode == SensorMode.ACCELEROMETER) {
                        AccelerometerInfoContent()
                    } else {
                        GyroscopeInfoContent()
                    }
                }
            }
        }
    }
}

// ── Info Content Sections ──

@Composable
private fun AccelerometerInfoContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "The accelerometer detects gravity and tilt — " +
                    "it measures how the force of gravity is distributed across each axis.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = SubtitleColor,
                lineHeight = 20.sp
            )
        )

        InfoAxisItem(
            color = Color(0xFFEF5350),
            axis = "X axis",
            description = "Left / Right tilt",
            detail = "Positive X → Device tilted LEFT\nNegative X → Device tilted RIGHT"
        )

        InfoAxisItem(
            color = AccentGreen,
            axis = "Y axis",
            description = "Up / Down tilt",
            detail = "Positive Y → TOP side raised\nNegative Y → BOTTOM side raised"
        )

        InfoAxisItem(
            color = Color(0xFF42A5F5),
            axis = "Z axis",
            description = "Facing up or down",
            detail = "Detects whether the screen faces the sky or the ground (flat detection)"
        )
    }
}

@Composable
private fun GyroscopeInfoContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "The gyroscope detects rotation and angular velocity — " +
                    "it measures how fast the device is spinning around each axis, in radians/sec.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = SubtitleColor,
                lineHeight = 20.sp
            )
        )

        InfoAxisItem(
            color = Color(0xFFEF5350),
            axis = "X axis",
            description = "Tilt forward / backward",
            detail = "Rotation around the horizontal axis\n(tilting screen toward or away from you)"
        )

        InfoAxisItem(
            color = AccentGreen,
            axis = "Y axis",
            description = "Turn left / right",
            detail = "Rotation around the vertical axis\n(turning the device like a steering wheel)"
        )

        InfoAxisItem(
            color = Color(0xFF42A5F5),
            axis = "Z axis",
            description = "Twist / spin",
            detail = "Rotation around the screen axis\n(twisting like turning a knob or dial)"
        )
    }
}

@Composable
private fun InfoAxisItem(
    color: Color,
    axis: String,
    description: String,
    detail: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row {
                Text(
                    text = axis,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "— $description",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SubtitleColor
                    )
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SubtitleColor.copy(alpha = 0.75f),
                    lineHeight = 18.sp
                )
            )
        }
    }
}

// ── Sensor Value Row ──

@Composable
private fun SensorValueRow(axis: String, value: Float, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (axis) {
                            "X" -> Color(0xFFEF5350)
                            "Y" -> AccentGreen
                            else -> Color(0xFF42A5F5)
                        }
                    )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Axis $axis",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SubtitleColor,
                        fontSize = 11.sp
                    )
                )
            }
        }
        Text(
            text = String.format("%+.3f", value),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                letterSpacing = 0.5.sp
            )
        )
    }
}
