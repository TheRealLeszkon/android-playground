package com.example.androidplayground.sandbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SandboxBg = Color(0xFF0D0D0D)
private val SandboxAccent = Color(0xFFFFD600)
private val SandboxSurface = Color(0xFF1A1A1A)
private val SandboxText = Color(0xFFE0E0E0)
private val SandboxMuted = Color(0xFF616161)

/**
 * Minimal developer sandbox screen.
 *
 * ─────────────────────────────────────────
 *  HOW TO ADD EXPERIMENTS
 *  Drop your composables inside the "Sandbox Area" section below.
 *  They are never shipped — this whole file lives in src/debug.
 * ─────────────────────────────────────────
 */
@Composable
fun DevSandboxScreen(onBack: () -> Unit) {
    Scaffold(containerColor = SandboxBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header banner ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SandboxAccent)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Outlined.BugReport,
                        contentDescription = null,
                        tint = SandboxBg,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "DEV SANDBOX",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = SandboxBg,
                            letterSpacing = 3.sp
                        )
                    )
                    Text(
                        text = "DEBUG BUILD ONLY — not visible in release",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = SandboxBg.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            // ── Sandbox area ───────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SandboxSectionLabel("SANDBOX AREA")

                // ┌──────────────────────────────────────────────────────┐
                // │  Drop your experimental composables below this line  │
                // └──────────────────────────────────────────────────────┘

                PlaceholderCard("Your experiments go here")

                // ┌──────────────────────────────────────────────────────┐
                // │  End of experiment zone                              │
                // └──────────────────────────────────────────────────────┘
            }

            HorizontalDivider(color = SandboxMuted, modifier = Modifier.padding(horizontal = 24.dp))

            // ── Back button ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SandboxAccent,
                        contentColor = SandboxBg
                    )
                ) {
                    Text(
                        text = "← Back",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SandboxSectionLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            color = SandboxMuted,
            letterSpacing = 2.sp
        )
    )
}

@Composable
private fun PlaceholderCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SandboxSurface, shape = MaterialTheme.shapes.medium)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "[ ]",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = SandboxAccent.copy(alpha = 0.4f)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = SandboxText.copy(alpha = 0.5f)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
