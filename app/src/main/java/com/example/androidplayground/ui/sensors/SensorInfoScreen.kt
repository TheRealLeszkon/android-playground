package com.example.androidplayground.ui.sensors

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val SurfaceColor = Color(0xFFF9F9F9)
private val SurfaceLowest = Color(0xFFFFFFFF)
private val PrimaryGreen = Color(0xFF3CDA84)
private val PrimaryDark = Color(0xFF006D3B)
private val TextMain = Color(0xFF000000)
private val TextMuted = Color(0xFF49454F)

private data class SensorInfo(
    val title: String,
    val measures: String,
    val axes: List<String>,
    val useCases: List<String>,
    val extra: String,
    val docsUrl: String
)

private val sensorInfoMap = mapOf(
    "accelerometer" to SensorInfo(
        title = "Accelerometer",
        measures = "Measures acceleration force in m/s² applied to the device, including the force of gravity.",
        axes = listOf(
            "X — Left/right tilt",
            "Y — Forward/back tilt",
            "Z — Vertical movement (gravity ≈ 9.8 m/s²)"
        ),
        useCases = listOf(
            "Tilt-based games",
            "Step detection & pedometers",
            "Screen rotation detection"
        ),
        extra = "The accelerometer includes the gravity component. For motion without gravity, use TYPE_LINEAR_ACCELERATION.",
        docsUrl = "https://developer.android.com/develop/sensors-and-location/sensors/sensors_motion#sensors-motion-accel"
    ),
    "gyroscope" to SensorInfo(
        title = "Gyroscope",
        measures = "Measures the rate of rotation in rad/s around each of the device's three physical axes.",
        axes = listOf(
            "X — Rotation around the X axis",
            "Y — Rotation around the Y axis",
            "Z — Rotation around the Z axis"
        ),
        useCases = listOf(
            "Motion tracking",
            "Gaming with precision control",
            "AR/VR head tracking"
        ),
        extra = "The gyroscope is more precise than the accelerometer for detecting rotation. It measures angular velocity, not absolute orientation.",
        docsUrl = "https://developer.android.com/develop/sensors-and-location/sensors/sensors_motion#sensors-motion-gyro"
    ),
    "light" to SensorInfo(
        title = "Light Sensor",
        measures = "Measures ambient light levels in lux (lx). Values range from near 0 in a dark room to over 100,000 in direct sunlight.",
        axes = emptyList(),
        useCases = listOf(
            "Automatic screen brightness adjustment",
            "Camera exposure metering",
            "Battery optimization based on environment"
        ),
        extra = "Light sensor values can vary widely depending on environment. Typical indoor lighting is 100–500 lux, while direct sunlight exceeds 100,000 lux.",
        docsUrl = "https://developer.android.com/develop/sensors-and-location/sensors/sensors_environment#sensors-environment-light"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorInfoScreen(
    sensorType: String,
    navController: NavController
) {
    val info = sensorInfoMap[sensorType]
    val context = LocalContext.current

    Scaffold(
        containerColor = SurfaceColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = info?.title ?: "Sensor Info",
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
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
        if (info == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Unknown sensor type", color = TextMuted)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            InfoSection(title = "What it measures") {
                Text(
                    text = info.measures,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextMain,
                        lineHeight = 26.sp
                    )
                )
            }

            if (info.axes.isNotEmpty()) {
                InfoSection(title = "Axes") {
                    info.axes.forEach { axis ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("•", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = axis,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextMain,
                                    lineHeight = 22.sp
                                )
                            )
                        }
                    }
                }
            }

            InfoSection(title = "Use cases") {
                info.useCases.forEach { useCase ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("•", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = useCase,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextMain,
                                lineHeight = 22.sp
                            )
                        )
                    }
                }
            }

            InfoSection(title = "Additional notes") {
                Text(
                    text = info.extra,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextMuted,
                        lineHeight = 22.sp
                    )
                )
            }

            val gradientBackground = Brush.linearGradient(
                colors = listOf(PrimaryDark, PrimaryGreen)
            )
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.docsUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = androidx.compose.foundation.layout.PaddingValues()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(gradientBackground),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "View Official Documentation",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceLowest)
            .padding(24.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                color = PrimaryDark,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}
