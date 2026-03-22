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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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

// ── Default waveform pattern for category D ──
private val DEFAULT_TIMINGS = longArrayOf(0, 100, 50, 100, 50, 200)
private val DEFAULT_AMPLITUDES = intArrayOf(0, 180, 0, 255, 0, 120)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HapticLabScreen(
    navController: NavController,
    viewModel: HapticLabViewModel = viewModel()
) {
    val savedPatterns by viewModel.savedPatterns.collectAsState()
    val primitiveScale by viewModel.primitiveScale.collectAsState()

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

            // ── Category A: System Constants ──
            item { SystemConstantsSection(viewModel) }

            // ── Category B: Predefined Effects ──
            item { PredefinedEffectsSection(viewModel) }

            // ── Category C: Composition Builder ──
            item { CompositionSection(viewModel, primitiveScale) }

            // ── Category D: Waveform & Saved Patterns ──
            item { WaveformSection(viewModel) }

            // ── Saved Patterns List ──
            if (savedPatterns.isNotEmpty()) {
                item { SectionLabel("SAVED PATTERNS") }
                itemsIndexed(savedPatterns) { index, pattern ->
                    SavedPatternItem(
                        pattern = pattern,
                        onPlay = { viewModel.playWaveform(it, pattern.timings, pattern.amplitudes) },
                        onDelete = { viewModel.deletePattern(index) }
                    )
                }
            }

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
private fun SdkGate(requiredSdk: Int, label: String, content: @Composable () -> Unit) {
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

// ── Category A ──

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

// ── Category B ──

private data class PredefinedItem(val label: String, val effectId: Int)

@Composable
private fun PredefinedEffectsSection(viewModel: HapticLabViewModel) {
    val context = LocalContext.current

    CategoryCard(label = "PREDEFINED EFFECTS") {
        SdkGate(requiredSdk = Build.VERSION_CODES.Q, label = "Predefined Effects") {
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

// ── Category C ──

@Composable
private fun CompositionSection(
    viewModel: HapticLabViewModel,
    primitiveScale: Float
) {
    val context = LocalContext.current

    CategoryCard(label = "COMPOSITION BUILDER") {
        SdkGate(requiredSdk = Build.VERSION_CODES.R, label = "Composition") {
            Text(
                text = "VibrationEffect.Composition — Android 11+",
                style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Play sequence button
            Button(
                onClick = { viewModel.playSequence(context) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play Sequence (Spin → Thud)", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tinker slider
            Text(
                text = "Test Primitive: TICK",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Slider(
                    value = primitiveScale,
                    onValueChange = { viewModel.updatePrimitiveScale(it) },
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = AccentGreen,
                        activeTrackColor = AccentGreen
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = String.format("%.2f", primitiveScale),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AccentGreenDark
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            FilledTonalButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        viewModel.playComposition(
                            context,
                            VibrationEffect.Composition.PRIMITIVE_TICK,
                            primitiveScale
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = AccentGreen.copy(alpha = 0.12f)
                )
            ) {
                Text("Test Tick at ${String.format("%.0f", primitiveScale * 100)}%",
                    color = AccentGreenDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Category D ──

@Composable
private fun WaveformSection(viewModel: HapticLabViewModel) {
    val context = LocalContext.current
    var showSaveDialog by remember { mutableStateOf(false) }

    CategoryCard(label = "WAVEFORM PATTERNS") {
        Text(
            text = "VibrationEffect.createWaveform() — custom timing patterns",
            style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Pattern: [0, 100, 50, 100, 50, 200] ms",
            style = MaterialTheme.typography.labelMedium.copy(color = SubtitleColor)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { viewModel.playWaveform(context, DEFAULT_TIMINGS, DEFAULT_AMPLITUDES) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Play", fontWeight = FontWeight.SemiBold)
            }

            FilledTonalButton(
                onClick = { showSaveDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = AccentGreen.copy(alpha = 0.12f)
                )
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, tint = AccentGreenDark, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save", color = AccentGreenDark, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showSaveDialog) {
        SavePatternDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                viewModel.savePattern(name, DEFAULT_TIMINGS, DEFAULT_AMPLITUDES)
                showSaveDialog = false
            }
        )
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

@Composable
private fun SavedPatternItem(
    pattern: SavedVibration,
    onPlay: (context: android.content.Context) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        ListItem(
            headlineContent = {
                Text(pattern.name, fontWeight = FontWeight.SemiBold)
            },
            supportingContent = {
                Text(
                    "${pattern.timings.size} steps",
                    style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor)
                )
            },
            trailingContent = {
                Row {
                    IconButton(onClick = { onPlay(context) }) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = AccentGreen
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFBA1A1A)
                        )
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
private fun SavePatternDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Pattern", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Pattern name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Save", color = if (name.isNotBlank()) AccentGreenDark else SubtitleColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SubtitleColor)
            }
        }
    )
}
