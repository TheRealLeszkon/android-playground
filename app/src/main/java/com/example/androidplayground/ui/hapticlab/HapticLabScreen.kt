package com.example.androidplayground.ui.hapticlab

import android.os.Build
import android.os.VibrationEffect
import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
private val GhostBorder = Color(0xFFBBCABC).copy(alpha = 0.15f)
private val WarningColor = Color(0xFFE65100)

private val SignatureGradient = Brush.linearGradient(
    colors = listOf(AccentGreenDark, AccentGreen)
)

// Composition primitive definitions (API 30+)
private data class PrimitiveInfo(val id: Int, val label: String)

private val availablePrimitives: List<PrimitiveInfo> by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        listOf(
            PrimitiveInfo(VibrationEffect.Composition.PRIMITIVE_CLICK, "Click"),
            PrimitiveInfo(VibrationEffect.Composition.PRIMITIVE_TICK, "Tick"),
            PrimitiveInfo(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, "Low Tick"),
            PrimitiveInfo(VibrationEffect.Composition.PRIMITIVE_THUD, "Thud"),
            PrimitiveInfo(VibrationEffect.Composition.PRIMITIVE_SPIN, "Spin"),
            PrimitiveInfo(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, "Quick Rise"),
            PrimitiveInfo(VibrationEffect.Composition.PRIMITIVE_SLOW_RISE, "Slow Rise"),
            PrimitiveInfo(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, "Quick Fall"),
        )
    } else {
        emptyList()
    }
}

// ═══════════════════════════════════════════
// Main Screen
// ═══════════════════════════════════════════

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

            item { OneShotBuilderSection(viewModel) }

            item { WaveformSequencerSection(viewModel) }

            item { CompositionStudioSection(viewModel) }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ═══════════════════════════════════════════
// Section: System Constants (existing)
// ═══════════════════════════════════════════

private data class HapticConstant(val label: String, val constant: Int, val description: String)

private val systemConstants = listOf(
    HapticConstant("Confirm", HapticFeedbackConstants.CONFIRM, "Positive confirmation feedback"),
    HapticConstant("Reject", HapticFeedbackConstants.REJECT, "Negative error feedback"),
    HapticConstant("Clock Tick", HapticFeedbackConstants.CLOCK_TICK, "Subtle tick for scrolling/pickers"),
    HapticConstant("Long Press", HapticFeedbackConstants.LONG_PRESS, "Feedback for long press actions"),
    HapticConstant("Virtual Key", HapticFeedbackConstants.VIRTUAL_KEY, "Feedback for button/key press"),
    HapticConstant("Keyboard Tap", HapticFeedbackConstants.KEYBOARD_TAP, "Typing feedback")
)

@Composable
private fun SystemConstantsSection(viewModel: HapticLabViewModel) {
    val view = LocalView.current

    CategoryCard(label = "SYSTEM CONSTANTS") {
        Text(
            text = "Represent UI interaction feedback. Provide consistent system-level haptics across apps.",
            style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            systemConstants.forEach { item ->
                Column {
                    HapticChipButton(
                        label = item.label,
                        onClick = { viewModel.playSystemConstant(view, item.constant) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// Section: Predefined Effects (existing)
// ═══════════════════════════════════════════

private data class PredefinedItem(val label: String, val effectId: Int, val description: String)

@Composable
private fun PredefinedEffectsSection(viewModel: HapticLabViewModel) {
    val context = LocalContext.current

    CategoryCard(label = "PREDEFINED EFFECTS") {
        SdkGate(requiredSdk = Build.VERSION_CODES.Q) {
            Text(
                text = "VibrationEffect.createPredefined() — Android 10+",
                style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            val effects = remember {
                listOf(
                    PredefinedItem("Click", VibrationEffect.EFFECT_CLICK, "Standard short click (baseline feedback)"),
                    PredefinedItem("Double Click", VibrationEffect.EFFECT_DOUBLE_CLICK, "Two quick consecutive clicks"),
                    PredefinedItem("Heavy Click", VibrationEffect.EFFECT_HEAVY_CLICK, "Stronger, more forceful click"),
                    PredefinedItem("Tick", VibrationEffect.EFFECT_TICK, "Very light, subtle tick feedback"),
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                effects.forEach { item ->
                    Column {
                        FilledTonalButton(
                            onClick = { viewModel.playPredefinedEffect(context, item.effectId) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AccentGreen.copy(alpha = 0.12f)
                            )
                        ) {
                            Text(item.label, color = AccentGreenDark, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// Section: One-Shot Builder
// ═══════════════════════════════════════════

@Composable
private fun OneShotBuilderSection(viewModel: HapticLabViewModel) {
    val context = LocalContext.current
    val duration by viewModel.oneShotDuration.collectAsState()
    val amplitude by viewModel.oneShotAmplitude.collectAsState()

    CategoryCard(label = "ONE-SHOT BUILDER") {
        Text(
            text = "VibrationEffect.createOneShot() — configure a single vibration pulse with custom duration and amplitude.",
            style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        StyledSlider(
            label = "Duration",
            value = duration,
            valueRange = 10f..1000f,
            valueText = "${duration.toInt()} ms",
            onValueChange = { viewModel.updateOneShotDuration(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        StyledSlider(
            label = "Amplitude",
            value = amplitude,
            valueRange = 1f..255f,
            valueText = "${amplitude.toInt()}",
            onValueChange = { viewModel.updateOneShotAmplitude(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        GradientButton(
            text = "Test One-Shot",
            onClick = { viewModel.playOneShot(context) }
        )
    }
}

// ═══════════════════════════════════════════
// Section: Waveform Sequencer
// ═══════════════════════════════════════════

@Composable
private fun WaveformSequencerSection(viewModel: HapticLabViewModel) {
    val context = LocalContext.current
    val steps by viewModel.waveformSteps.collectAsState()
    val loop by viewModel.waveformLoop.collectAsState()

    CategoryCard(label = "WAVEFORM SEQUENCER") {
        Text(
            text = "VibrationEffect.createWaveform() — build a multi-step vibration pattern with rests, amplitude control, and optional looping.",
            style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Column(
            modifier = Modifier.animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            steps.forEachIndexed { index, step ->
                WaveformStepRow(
                    index = index,
                    step = step,
                    onUpdate = { viewModel.updateWaveformStep(index, it) },
                    onDelete = { viewModel.removeWaveformStep(index) },
                    canDelete = steps.size > 1
                )
                if (index < steps.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SecondaryButton(
            text = "Add Step",
            icon = Icons.Default.Add,
            onClick = { viewModel.addWaveformStep() }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Loop Sequence",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            Switch(
                checked = loop,
                onCheckedChange = { viewModel.updateWaveformLoop(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFDDDDDD)
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GradientButton(
                text = "Test Waveform",
                onClick = { viewModel.playWaveform(context) },
                modifier = Modifier.weight(1f)
            )
            if (loop) {
                StopButton(onClick = { viewModel.stopVibration(context) })
            }
        }
    }
}

@Composable
private fun WaveformStepRow(
    index: Int,
    step: WaveformStep,
    onUpdate: (WaveformStep) -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Step ${index + 1}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Rest / Vibrate toggle chips
                    ModeChip(
                        label = "Vibrate",
                        selected = !step.isRest,
                        onClick = { onUpdate(step.copy(isRest = false)) }
                    )
                    ModeChip(
                        label = "Rest",
                        selected = step.isRest,
                        onClick = { onUpdate(step.copy(isRest = true)) }
                    )

                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove step",
                                tint = SubtitleColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            StyledSlider(
                label = "Duration",
                value = step.durationMs.toFloat(),
                valueRange = 30f..1000f,
                valueText = "${step.durationMs} ms",
                onValueChange = { onUpdate(step.copy(durationMs = it.toLong())) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            StyledSlider(
                label = "Amplitude",
                value = step.amplitude.toFloat(),
                valueRange = 0f..255f,
                valueText = if (step.isRest) "Off" else "${step.amplitude}",
                onValueChange = { onUpdate(step.copy(amplitude = it.toInt())) },
                enabled = !step.isRest
            )
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (selected) AccentGreen.copy(alpha = 0.15f) else Color(0xFFE8E8E8)
        ),
        modifier = Modifier.height(32.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (selected) AccentGreenDark else SubtitleColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}

// ═══════════════════════════════════════════
// Section: Composition Studio
// ═══════════════════════════════════════════

@Composable
private fun CompositionStudioSection(viewModel: HapticLabViewModel) {
    val context = LocalContext.current
    val nodes by viewModel.compositionNodes.collectAsState()
    val error by viewModel.compositionError.collectAsState()

    CategoryCard(label = "COMPOSITION STUDIO") {
        SdkGate(requiredSdk = Build.VERSION_CODES.R) {
            Text(
                text = "VibrationEffect.Composition — chain haptic primitives into rich, expressive patterns. API 30+",
                style = MaterialTheme.typography.bodySmall.copy(color = SubtitleColor),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Refresh support check whenever nodes change
            LaunchedEffect(nodes) {
                viewModel.refreshCompositionSupport(context)
            }

            // Warning banner
            if (error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(WarningColor.copy(alpha = 0.08f))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = WarningColor,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier.animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                nodes.forEachIndexed { index, node ->
                    PrimitiveNodeRow(
                        index = index,
                        node = node,
                        onUpdate = { viewModel.updatePrimitiveNode(index, it) },
                        onDelete = { viewModel.removePrimitiveNode(index) },
                        canDelete = nodes.size > 1
                    )
                    if (index < nodes.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SecondaryButton(
                text = "Add Primitive",
                icon = Icons.Default.Add,
                onClick = { viewModel.addPrimitiveNode() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            GradientButton(
                text = "Test Composition",
                onClick = { viewModel.playComposition(context) },
                enabled = error == null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrimitiveNodeRow(
    index: Int,
    node: PrimitiveNode,
    onUpdate: (PrimitiveNode) -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    val currentPrimitive = availablePrimitives.find { it.id == node.primitiveId }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Primitive ${index + 1}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove primitive",
                            tint = SubtitleColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primitive type dropdown
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = currentPrimitive?.label ?: "Select",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = GhostBorder,
                        focusedLabelColor = AccentGreenDark
                    )
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    availablePrimitives.forEach { primitive ->
                        DropdownMenuItem(
                            text = { Text(primitive.label) },
                            onClick = {
                                onUpdate(node.copy(primitiveId = primitive.id))
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            StyledSlider(
                label = "Scale",
                value = node.scale,
                valueRange = 0f..1f,
                valueText = "%.2f".format(node.scale),
                onValueChange = { onUpdate(node.copy(scale = it)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            StyledSlider(
                label = "Delay",
                value = node.delayMs.toFloat(),
                valueRange = 0f..500f,
                valueText = "${node.delayMs} ms",
                onValueChange = { onUpdate(node.copy(delayMs = it.toInt())) }
            )
        }
    }
}

// ═══════════════════════════════════════════
// Shared / Reusable Components
// ═══════════════════════════════════════════

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
private fun StyledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit,
    enabled: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (enabled) SubtitleColor else SubtitleColor.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (enabled) AccentGreenDark else SubtitleColor.copy(alpha = 0.4f),
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = AccentGreen,
                activeTrackColor = AccentGreen,
                inactiveTrackColor = AccentGreen.copy(alpha = 0.12f),
                disabledThumbColor = Color(0xFFCCCCCC),
                disabledActiveTrackColor = Color(0xFFDDDDDD),
                disabledInactiveTrackColor = Color(0xFFEEEEEE)
            )
        )
    }
}

@Composable
private fun GradientButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled) SignatureGradient
                    else Brush.linearGradient(
                        colors = listOf(Color(0xFFBBBBBB), Color(0xFFDDDDDD))
                    ),
                    shape = RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun SecondaryButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color(0xFFEEEEEE)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = AccentGreenDark,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = AccentGreenDark,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StopButton(onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color(0xFFFFEBEE)
        ),
        modifier = Modifier
            .height(48.dp)
            .width(48.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            Icons.Default.Stop,
            contentDescription = "Stop vibration",
            tint = Color(0xFFC62828),
            modifier = Modifier.size(22.dp)
        )
    }
}
