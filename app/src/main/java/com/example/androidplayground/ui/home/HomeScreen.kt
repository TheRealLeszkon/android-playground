package com.example.androidplayground.ui.home

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.androidplayground.navigation.Screen

// Accent color from DESIGN.md
private val AccentGreen = Color(0xFF3CDA84)
private val AccentGreenDark = Color(0xFF006D3B)
private val CardBackground = Color(0xFFFFFFFF)
private val ScreenBackground = Color(0xFFF9F9F9)
private val SubtitleColor = Color(0xFF49454F)

@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        containerColor = ScreenBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            // ── Hero title ──
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.Black)) {
                        append("Android\n")
                    }
                    withStyle(SpanStyle(color = AccentGreen)) {
                        append("Playground")
                    }
                },
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp,
                    lineHeight = 52.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Subtitle ──
            Text(
                text = "A hands-on laboratory for testing device capabilities, sensors, and real-time interactive demos.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = SubtitleColor,
                    lineHeight = 26.sp
                )
            )

            Spacer(modifier = Modifier.height(64.dp))

            // ── Cards ──
            MenuCard(
                categoryLabel = "DISCOVERY",
                title = "Explore Features",
                icon = Icons.Outlined.Explore,
                iconTint = Color.White,
                iconBackground = AccentGreen,
                onClick = { navController.navigate(Screen.FeatureList.route) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuCard(
                categoryLabel = "SANDBOX",
                title = "Interactive\nDemonstrations",
                icon = Icons.Outlined.FileDownload,
                iconTint = Color.Black,
                iconBackground = Color.Transparent,
                onClick = { navController.navigate(Screen.DemoList.route) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            val context = LocalContext.current
            MenuCard(
                categoryLabel = "REFERENCE",
                title = "Android Docs",
                icon = Icons.Outlined.Description,
                iconTint = Color.Black,
                iconBackground = Color.Transparent,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.android.com"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun MenuCard(
    categoryLabel: String,
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = AccentGreenDark,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
