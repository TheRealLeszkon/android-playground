package com.example.androidplayground.ui.features

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.androidplayground.ui.hapticlab.HapticLabScreen
import com.example.androidplayground.ui.notifications.NotificationDashboardScreen
import com.example.androidplayground.ui.notifications.NotificationInfoScreen
import com.example.androidplayground.ui.sensors.SensorDashboardScreen
import com.example.androidplayground.ui.sensors.SensorInfoScreen
import com.example.androidplayground.ui.apidashboard.ApiDashboardScreen

private val ScreenBackground = Color(0xFFF9F9F9)
private val AccentGreen = Color(0xFF3CDA84)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureDetailScreen(
    featureName: String,
    navController: NavController
) {
    when {
        featureName == "sensor_dashboard" -> SensorDashboardScreen(navController = navController)
        featureName == "Haptics" -> HapticLabScreen(navController = navController)
        featureName == "Notifications" -> NotificationDashboardScreen(navController = navController)
        featureName == "api_dashboard" -> ApiDashboardScreen(navController = navController)

        featureName.startsWith("sensor_info:") -> {
            val type = featureName.substringAfter(":")
            SensorInfoScreen(sensorType = type, navController = navController)
        }

        featureName.startsWith("notification_info:") -> {
            val type = featureName.substringAfter(":")
            NotificationInfoScreen(featureId = type, navController = navController)
        }

        else -> FeatureDetailPlaceholder(featureName = featureName, navController = navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeatureDetailPlaceholder(
    featureName: String,
    navController: NavController
) {
    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        featureName,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ScreenBackground
                )
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
                Text(
                    text = featureName,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Coming soon",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF49454F)
                    )
                )
            }
        }
    }
}
