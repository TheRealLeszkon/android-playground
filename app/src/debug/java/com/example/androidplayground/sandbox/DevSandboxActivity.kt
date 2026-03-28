package com.example.androidplayground.sandbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.androidplayground.ui.theme.AndroidPlaygroundTheme

/**
 * Developer-only sandbox activity. Lives in src/debug so it is stripped
 * entirely from release builds — no ProGuard rules required.
 *
 * Launch via the hidden long-press on the HomeScreen title, or directly
 * from Android Studio's "Run" configuration / adb am start.
 */
class DevSandboxActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidPlaygroundTheme {
                DevSandboxScreen(onBack = { finish() })
            }
        }
    }
}
