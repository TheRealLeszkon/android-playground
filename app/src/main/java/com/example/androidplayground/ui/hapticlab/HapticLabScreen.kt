package com.example.androidplayground.ui.hapticlab

import android.os.Build
import android.os.VibrationEffect
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

private val AccentGreen = Color(0xFF3CDA84)
private val AccentGreenDark = Color(0xFF006D3B)
private val CardBg = Color(0xFFFFFFFF)
private val ScreenBg = Color(0xFFF9F9F9)
private val SubtitleColor = Color(0xFF49454F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HapticLabScreen(
    navController: NavController,
    viewModel: HapticLabViewModel = viewModel()
) {
    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Haptic Lab", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item { SystemConstantsSection(viewModel) }

            item { PredefinedEffectsSection(viewModel) }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Section composables
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            color = AccentGreenDark,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp
        )
    )
}

@Composable
private fun SdkGate(requiredSdk: Int, content: @Composable () -> Unit) {
    if (Build.VERSION.SDK_INT >= requiredSdk) {
        content()
    } else {
        Text(
            text = "Requires Android ${sdkToVersion(requiredSdk)}+",
            style = MaterialTheme.typography.bodyMedium.copy(color = SubtitleColor),
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

private fun sdkToVersion(sdk: Int): String = when (sdk) {
    Build.VERSION_CODES.Q -> "10"
    Build.VERSION_CODES.R -> "11"
    Build.VERSION_CODES.S -> "12"
    else -> sdk.toString()
}

// ── System Constants ──

private data class HapticConstant(val label: String, val constant: Int)

private val systemConstants = listOf(
    HapticConstant("Confirm", HapticFeedbackConstants.CONFIRM),
    HapticConstant("Reject", HapticFeedbackConstants.REJECT),
    HapticConstant("Clock Tick", HapticFeedbackConstants.CLOCK_TICK),
    HapticConstant("Long Press", HapticFeedbackConstants.LONG_PRESS),
    HapticConstant("Keyboard Tap", HapticFeedbackConstants.KEYBOARD_TAP),
    HapticConstant("Virtual Key", HapticFeedbackConstants.VIRTUAL_KEY),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SystemConstantsSection(viewModel: HapticLabViewModel) {
    val view = LocalView.current

    CategoryCard(label = "SYSTEM CONSTANTS") {
        Text(
            text = "Standard haptic feedback via HapticFeedbackConstants",
            style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            systemConstants.forEach { item ->
                HapticChipButton(
                    label = item.label,
                    onClick = { viewModel.playSystemConstant(view, item.constant) }
                )
            }
        }
    }
}

// ── Predefined Effects ──

private data class PredefinedItem(val label: String, val effectId: Int)

@Composable
private fun PredefinedEffectsSection(viewModel: HapticLabViewModel) {
    val context = LocalContext.current

    CategoryCard(label = "PREDEFINED EFFECTS") {
        SdkGate(requiredSdk = Build.VERSION_CODES.Q) {
            Text(
                text = "VibrationEffect.createPredefined() — Android 10+",
                style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            val effects = remember {
                listOf(
                    PredefinedItem("Click", VibrationEffect.EFFECT_CLICK),
                    PredefinedItem("Heavy Click", VibrationEffect.EFFECT_HEAVY_CLICK),
                    PredefinedItem("Double Click", VibrationEffect.EFFECT_DOUBLE_CLICK),
                    PredefinedItem("Tick", VibrationEffect.EFFECT_TICK),
                )
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                items(effects.size) { index ->
                    val item = effects[index]
                    FilledTonalButton(
                        onClick = { viewModel.playPredefinedEffect(context, item.effectId) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AccentGreen.copy(alpha = 0.12f)
                        )
                    ) {
                        Text(item.label, color = AccentGreenDark, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Reusable components
// ═══════════════════════════════════════════════════════════════

@Composable
private fun CategoryCard(label: String, content: @Composable () -> Unit) {
    Column {
        SectionLabel(label)
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardBg)
                .padding(20.dp)
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun HapticChipButton(label: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color(0xFFEEEEEE)
        )
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(color = Color.Black))
    }
}
