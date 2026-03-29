package com.example.androidplayground.ui.notifications

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androidplayground.ui.theme.AndroidPlaygroundTheme

class FeatureDescriptionActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val featureId = intent.getStringExtra(EXTRA_FEATURE_ID) ?: ""
        val feature = FeatureDescriptions.map[featureId]

        setContent {
            AndroidPlaygroundTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(feature?.title ?: "Unknown Feature") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        if (feature == null) {
                            Text("Feature description not found.")
                            return@Scaffold
                        }
                        
                        Text(
                            text = feature.description,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(onClick = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(feature.documentationUrl))
                            startActivity(browserIntent)
                        }) {
                            Text("View Official Documentation")
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_FEATURE_ID = "extra_feature_id"
    }
}
