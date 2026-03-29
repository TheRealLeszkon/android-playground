package com.example.androidplayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.example.androidplayground.navigation.NavGraph
import com.example.androidplayground.ui.theme.AndroidPlaygroundTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidPlaygroundTheme {
                val navController = rememberNavController()

                val route = intent.getStringExtra("route")
                LaunchedEffect(route) {
                    route?.let {
                        navController.navigate(it)
                    }
                }

                NavGraph(navController = navController)
            }
        }
    }
}