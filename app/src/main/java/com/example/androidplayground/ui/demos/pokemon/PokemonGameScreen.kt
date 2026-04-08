package com.example.androidplayground.ui.demos.pokemon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

// ── Design tokens ──

private val ScreenBg = Color(0xFFF9F9F9)
private val CardBg = Color(0xFFFFFFFF)
private val SilhouetteBg = Color(0xFFE8E0D0)   // warm neutral for silhouette contrast
private val DarkSurface = Color(0xFF1C1B1F)
private val AccentGreen = Color(0xFF3CDA84)
private val AccentGreenDark = Color(0xFF006D3B)
private val SubtitleColor = Color(0xFF49454F)
private val ErrorColor = Color(0xFFEF5350)
private val HintColor = Color(0xFFFFB74D)
private val StreakColor = Color(0xFFFF7043)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonGameScreen(
    navController: NavController,
    viewModel: PokemonGameViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Haptic feedback on guess result
    LaunchedEffect(state.lastGuessResult) {
        when (state.lastGuessResult) {
            GuessResult.CORRECT -> playAcceptHaptic(context)
            GuessResult.WRONG -> playRejectHaptic(context)
            GuessResult.NONE -> {}
        }
    }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Who's That Pokémon?", fontWeight = FontWeight.Bold) },
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

            // Streak counter + Gen 1 toggle
            StreakBadge(
                streak = state.streak,
                gen1Only = state.gen1Only,
                onToggleGen1 = viewModel::toggleGen1Only
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pokémon image (silhouette or revealed)
            SpriteCard(
                spriteUrl = state.spriteUrl,
                isSilhouette = !state.revealed && !state.guessedCorrectly,
                isLoading = state.isLoading,
                networkError = state.networkError,
                onRetry = viewModel::retry
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Revealed name
            AnimatedVisibility(
                visible = state.revealed || state.guessedCorrectly,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                val isCorrect = state.guessedCorrectly
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isCorrect) AccentGreen.copy(alpha = 0.1f) else HintColor.copy(alpha = 0.1f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isCorrect) "Correct!" else "It was...",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isCorrect) AccentGreenDark else SubtitleColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.pokemonName.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) AccentGreenDark else DarkSurface
                            )
                        )
                    }
                }
            }

            // Hint display
            if (state.hintText.isNotEmpty() && !state.revealed && !state.guessedCorrectly) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(HintColor.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = HintColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.hintText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SubtitleColor,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Error message
            if (state.errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ErrorColor,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Guess input + buttons (hidden when revealed/correct)
            if (!state.revealed && !state.guessedCorrectly && !state.isLoading && !state.networkError) {
                GuessInput(
                    guess = state.userGuess,
                    onGuessChange = viewModel::updateGuess,
                    onSubmit = viewModel::submitGuess
                )

                Spacer(modifier = Modifier.height(12.dp))

                ActionButtons(
                    hintLevel = state.hintLevel,
                    onHint = viewModel::requestHint,
                    onReveal = viewModel::reveal
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Streak Badge ──

@Composable
private fun StreakBadge(
    streak: Int,
    gen1Only: Boolean,
    onToggleGen1: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (streak > 0) StreakColor else SubtitleColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "STREAK",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SubtitleColor,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                )
            }
            Text(
                "$streak",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (streak > 0) StreakColor else SubtitleColor
                )
            )
        }

        // Gen 1 toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Gen 1 Only (1–151)",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SubtitleColor,
                    fontWeight = FontWeight.Medium
                )
            )
            Switch(
                checked = gen1Only,
                onCheckedChange = { onToggleGen1() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFDDDDDD)
                )
            )
        }
    }
}

// ── Sprite Card ──

@Composable
private fun SpriteCard(
    spriteUrl: String,
    isSilhouette: Boolean,
    isLoading: Boolean,
    networkError: Boolean,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSilhouette) SilhouetteBg else DarkSurface),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = AccentGreen,
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Loading Pokémon...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            networkError -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Failed to load Pokémon",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ErrorColor
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            spriteUrl.isNotEmpty() -> {
                AsyncImage(
                    model = spriteUrl,
                    contentDescription = "Pokémon sprite",
                    modifier = Modifier.size(200.dp),
                    contentScale = ContentScale.Fit,
                    // Black silhouette: tint the entire image black, preserving alpha
                    colorFilter = if (isSilhouette) {
                        ColorFilter.tint(Color.Black, BlendMode.SrcAtop)
                    } else null
                )
            }
        }
    }
}

// ── Guess Input ──

@Composable
private fun GuessInput(
    guess: String,
    onGuessChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .padding(20.dp)
    ) {
        OutlinedTextField(
            value = guess,
            onValueChange = onGuessChange,
            placeholder = {
                Text("Enter Pokémon name...", color = SubtitleColor.copy(alpha = 0.5f))
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGreen,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                cursorColor = AccentGreen
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
        ) {
            Text("Submit Guess", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Action Buttons ──

@Composable
private fun ActionButtons(
    hintLevel: Int,
    onHint: () -> Unit,
    onReveal: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onHint,
            enabled = hintLevel < 3,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = HintColor
            )
        ) {
            Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Hint (${3 - hintLevel})",
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        }

        OutlinedButton(
            onClick = onReveal,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ErrorColor
            )
        ) {
            Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reveal", fontWeight = FontWeight.Medium, fontSize = 13.sp)
        }
    }
}

// ── Haptic helpers ──

private fun getVibrator(context: Context): Vibrator {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
}

/** Strong satisfying double-pulse for correct guess */
private fun playAcceptHaptic(context: Context) {
    try {
        val vibrator = getVibrator(context)
        val timings = longArrayOf(0, 60, 60, 80)
        val amplitudes = intArrayOf(0, 200, 0, 255)
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    } catch (_: Exception) {}
}

/** Short sharp single buzz for wrong guess */
private fun playRejectHaptic(context: Context) {
    try {
        val vibrator = getVibrator(context)
        vibrator.vibrate(VibrationEffect.createOneShot(40, 180))
    } catch (_: Exception) {}
}
