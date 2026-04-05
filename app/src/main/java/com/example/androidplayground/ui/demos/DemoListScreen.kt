package com.example.androidplayground.ui.demos

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.ScreenRotation
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.androidplayground.navigation.Screen

private val AccentGreen = Color(0xFF3CDA84)
private val AccentGreenDark = Color(0xFF006D3B)
private val CardBackground = Color(0xFFFFFFFF)
private val ScreenBackground = Color(0xFFF9F9F9)

private data class DemoItem(
    val name: String,
    val icon: ImageVector,
    val description: String
)

private val demos = listOf(
    DemoItem("Safe Cracker", Icons.Outlined.Lock, "Crack the code using haptic feedback"),
    DemoItem("Tilt Pong", Icons.Outlined.SportsEsports, "Classic pong controlled by gyroscope"),
    DemoItem("Light Sensor Game", Icons.Outlined.LightMode, "Gameplay driven by ambient light"),
    DemoItem("Device Orientation", Icons.Outlined.ScreenRotation, "Accelerometer & gyroscope demo")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoListScreen(navController: NavController) {
    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Demo Games",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            itemsIndexed(demos) { index, demo ->
                DemoCard(
                    index = index + 1,
                    demo = demo,
                    onClick = {
                        navController.navigate(Screen.DemoDetail.createRoute(demo.name))
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun DemoCard(
    index: Int,
    demo: DemoItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = String.format("%02d", index),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.size(16.dp))
                Column {
                    Text(
                        text = demo.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = demo.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF49454F)
                        )
                    )
                }
            }
            Icon(
                imageVector = demo.icon,
                contentDescription = demo.name,
                tint = AccentGreenDark,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
