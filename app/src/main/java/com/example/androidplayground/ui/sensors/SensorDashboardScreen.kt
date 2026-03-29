package com.example.androidplayground.ui.sensors

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.androidplayground.navigation.Screen

private val SurfaceColor = Color(0xFFF9F9F9)
private val SurfaceLowest = Color(0xFFFFFFFF)
private val PrimaryGreen = Color(0xFF3CDA84)
private val PrimaryDark = Color(0xFF006D3B)
private val TextMain = Color(0xFF000000)
private val TextMuted = Color(0xFF49454F)

private val GraphRed = Color(0xFFEF5350)
private val GraphGreen = Color(0xFF66BB6A)
private val GraphBlue = Color(0xFF42A5F5)
private val GraphYellow = Color(0xFFFFCA28)
private val GraphBackground = Color(0xFFF5F5F5)
private val GridLineColor = Color(0xFFE0E0E0)
private val GridLabelColor = Color(0xFF9E9E9E)

private val DotBoundaryColor = Color(0xFFE0E0E0)
private val DotCrosshairColor = Color(0xFFEEEEEE)
private val DotColor = PrimaryGreen
private val DotAreaBackground = Color(0xFFFAFAFA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDashboardScreen(
    navController: NavController,
    viewModel: SensorDashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        viewModel.startSensor(context)
        onDispose { viewModel.stopSensor() }
    }

    LaunchedEffect(Unit) {
        viewModel.startFrameLoop()
    }

    Scaffold(
        containerColor = SurfaceColor,
        topBar = {
            TopAppBar(
                title = {
                    Text("Sensor Dashboard", fontWeight = FontWeight.Bold, color = TextMain)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextMain
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            AccelerometerCard(
                values = state.accelValues,
                history = state.accelHistory,
                accelPoint = state.accelPoint,
                onInfoClick = {
                    navController.navigate(
                        Screen.FeatureDetail.createRoute("sensor_info:accelerometer")
                    )
                }
            )

            GyroscopeCard(
                values = state.gyroValues,
                history = state.gyroHistory,
                onInfoClick = {
                    navController.navigate(
                        Screen.FeatureDetail.createRoute("sensor_info:gyroscope")
                    )
                }
            )

            LightSensorCard(
                value = state.lightValue,
                history = state.lightHistory,
                onInfoClick = {
                    navController.navigate(
                        Screen.FeatureDetail.createRoute("sensor_info:light")
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Sensor Cards
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AccelerometerCard(
    values: XYZReading,
    history: List<XYZReading>,
    accelPoint: Pair<Float, Float>,
    onInfoClick: () -> Unit
) {
    SensorCard(title = "Accelerometer", onInfoClick = onInfoClick) {
        XYZValues(values)
        Spacer(modifier = Modifier.height(16.dp))
        AccelXYVisualization(accelPoint = accelPoint)
        Spacer(modifier = Modifier.height(16.dp))
        XYZLegend()
        Spacer(modifier = Modifier.height(8.dp))
        XYZGraph(history = history)
    }
}

@Composable
private fun GyroscopeCard(
    values: XYZReading,
    history: List<XYZReading>,
    onInfoClick: () -> Unit
) {
    SensorCard(title = "Gyroscope", onInfoClick = onInfoClick) {
        XYZValues(values)
        Spacer(modifier = Modifier.height(16.dp))
        XYZLegend()
        Spacer(modifier = Modifier.height(8.dp))
        XYZGraph(history = history)
    }
}

@Composable
private fun LightSensorCard(
    value: Float,
    history: List<Float>,
    onInfoClick: () -> Unit
) {
    SensorCard(title = "Light Sensor", onInfoClick = onInfoClick) {
        Text(
            text = "%.1f lux".format(value),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        val maxLux = 40000f
        val progress = (value / maxLux).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = GraphYellow,
            trackColor = GraphBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        SingleLineGraph(history = history, lineColor = GraphYellow)
    }
}

// ═══════════════════════════════════════════════════════════════
// Reusable Components
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SensorCard(
    title: String,
    onInfoClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceLowest)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
            )
            IconButton(onClick = onInfoClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "$title info",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun XYZValues(values: XYZReading) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AxisValue("X", values.x, GraphRed)
        AxisValue("Y", values.y, GraphGreen)
        AxisValue("Z", values.z, GraphBlue)
    }
}

@Composable
private fun AxisValue(axis: String, value: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$axis:",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextMuted
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "%.2f".format(value),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
        )
    }
}

@Composable
private fun XYZLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendItem("X", GraphRed)
        LegendItem("Y", GraphGreen)
        LegendItem("Z", GraphBlue)
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                fontSize = 10.sp
            )
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Accelerometer XY Visualization
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AccelXYVisualization(accelPoint: Pair<Float, Float>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(DotAreaBackground),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = minOf(centerX, centerY) - 8f

            // Outer boundary circle
            drawCircle(
                color = DotBoundaryColor,
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2f)
            )

            // Inner guide circle
            drawCircle(
                color = DotBoundaryColor.copy(alpha = 0.4f),
                radius = radius * 0.5f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1f)
            )

            // Crosshair lines
            drawLine(
                color = DotCrosshairColor,
                start = Offset(centerX - radius, centerY),
                end = Offset(centerX + radius, centerY),
                strokeWidth = 1f
            )
            drawLine(
                color = DotCrosshairColor,
                start = Offset(centerX, centerY - radius),
                end = Offset(centerX, centerY + radius),
                strokeWidth = 1f
            )

            // Moving dot
            val dotX = centerX + accelPoint.first * radius
            val dotY = centerY - accelPoint.second * radius
            val dotRadius = 10f

            // Dot shadow
            drawCircle(
                color = DotColor.copy(alpha = 0.2f),
                radius = dotRadius + 6f,
                center = Offset(dotX, dotY)
            )
            // Dot fill
            drawCircle(
                color = DotColor,
                radius = dotRadius,
                center = Offset(dotX, dotY)
            )
            // Dot highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = dotRadius * 0.4f,
                center = Offset(dotX - dotRadius * 0.2f, dotY - dotRadius * 0.2f)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Graph Components (with grid lines + value markers)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun XYZGraph(history: List<XYZReading>) {
    if (history.isEmpty()) return

    val allValues = history.flatMap { listOf(it.x, it.y, it.z) }
    val minVal = allValues.min()
    val maxVal = allValues.max()
    val range = (maxVal - minVal).coerceAtLeast(1f)
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GraphBackground)
    ) {
        val labelPadding = 40f
        val topPadding = 12f
        val bottomPadding = 12f
        val graphLeft = labelPadding
        val graphRight = size.width - 8f
        val graphWidth = graphRight - graphLeft
        val graphTop = topPadding
        val graphBottom = size.height - bottomPadding
        val graphHeight = graphBottom - graphTop

        // Draw grid and labels
        drawGridWithLabels(
            textMeasurer = textMeasurer,
            minVal = minVal,
            maxVal = maxVal,
            graphLeft = graphLeft,
            graphRight = graphRight,
            graphTop = graphTop,
            graphBottom = graphBottom,
            lineCount = 5
        )

        // Draw data lines
        fun drawDataLine(values: List<Float>, color: Color) {
            if (values.size < 2) return
            val path = Path()
            val step = graphWidth / (SensorDashboardViewModel.BUFFER_SIZE - 1).toFloat()

            values.forEachIndexed { index, value ->
                val x = graphLeft + index * step
                val y = graphTop + graphHeight * (1f - (value - minVal) / range)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )
        }

        drawDataLine(history.map { it.x }, GraphRed)
        drawDataLine(history.map { it.y }, GraphGreen)
        drawDataLine(history.map { it.z }, GraphBlue)
    }
}

@Composable
private fun SingleLineGraph(history: List<Float>, lineColor: Color) {
    if (history.isEmpty()) return

    val minVal = history.min()
    val maxVal = history.max()
    val range = (maxVal - minVal).coerceAtLeast(1f)
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GraphBackground)
    ) {
        if (history.size < 2) return@Canvas

        val labelPadding = 40f
        val topPadding = 12f
        val bottomPadding = 12f
        val graphLeft = labelPadding
        val graphRight = size.width - 8f
        val graphWidth = graphRight - graphLeft
        val graphTop = topPadding
        val graphBottom = size.height - bottomPadding
        val graphHeight = graphBottom - graphTop

        drawGridWithLabels(
            textMeasurer = textMeasurer,
            minVal = minVal,
            maxVal = maxVal,
            graphLeft = graphLeft,
            graphRight = graphRight,
            graphTop = graphTop,
            graphBottom = graphBottom,
            lineCount = 5
        )

        val step = graphWidth / (SensorDashboardViewModel.BUFFER_SIZE - 1).toFloat()
        val path = Path()
        history.forEachIndexed { index, value ->
            val x = graphLeft + index * step
            val y = graphTop + graphHeight * (1f - (value - minVal) / range)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawGridWithLabels(
    textMeasurer: TextMeasurer,
    minVal: Float,
    maxVal: Float,
    graphLeft: Float,
    graphRight: Float,
    graphTop: Float,
    graphBottom: Float,
    lineCount: Int
) {
    val graphHeight = graphBottom - graphTop
    val range = maxVal - minVal

    for (i in 0 until lineCount) {
        val fraction = i.toFloat() / (lineCount - 1)
        val y = graphTop + graphHeight * fraction
        val value = maxVal - range * fraction

        // Grid line
        drawLine(
            color = GridLineColor,
            start = Offset(graphLeft, y),
            end = Offset(graphRight, y),
            strokeWidth = 1f
        )

        // Value label
        val label = if (kotlin.math.abs(value) >= 100f) {
            "%.0f".format(value)
        } else {
            "%.1f".format(value)
        }

        val textResult = textMeasurer.measure(
            text = label,
            style = TextStyle(fontSize = 8.sp, color = GridLabelColor)
        )
        drawText(
            textLayoutResult = textResult,
            topLeft = Offset(
                x = 2f,
                y = y - textResult.size.height / 2f
            )
        )
    }
}
